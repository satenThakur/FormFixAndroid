package com.fittracker.ui.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.fittracker.R
import com.fittracker.databinding.FragmentPushupBinding
import com.fittracker.model.LandMarkModel
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.timerInterval
import com.fittracker.utilits.ConstantsSquats.timerLimit
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.MainViewModel
import com.fittracker.viewmodel.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PushUpFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener {
    companion object {
        private const val TAG = "Form Fit"
    }

    private var _fragmentpushupsBinding: FragmentPushupBinding? = null
    private val fragmentpushupsBinding get() = _fragmentpushupsBinding!!
    private lateinit var poseLandmarkHelper: PoseLandmarkerHelper
    private val viewModelPoseLan: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private var landMarkList = ArrayList<LandMarkModel>()
    private var worldLandMarkList = ArrayList<LandMarkModel>()
    private var isTimerCompleted = false
    private var windowHeight = 0
    private var windowWidth = 0
    private var isPlaying=true
    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService
    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_pushUp_to_permissions)
        }
        if (this::backgroundExecutor.isInitialized) {
            backgroundExecutor.execute {
                if (this::poseLandmarkHelper.isInitialized) {
                    if (poseLandmarkHelper.isClose()) {
                        poseLandmarkHelper.setupPoseLandmarker()
                    }
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::poseLandmarkHelper.isInitialized) {
            viewModelPoseLan.setMinPoseDetectionConfidence(poseLandmarkHelper.minPoseDetectionConfidence)
            viewModelPoseLan.setMinPoseTrackingConfidence(poseLandmarkHelper.minPoseTrackingConfidence)
            viewModelPoseLan.setMinPosePresenceConfidence(poseLandmarkHelper.minPosePresenceConfidence)
            viewModelPoseLan.setDelegate(poseLandmarkHelper.currentDelegate)
            backgroundExecutor.execute { poseLandmarkHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentpushupsBinding = null
        super.onDestroyView()
        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        //fragmentCameraBinding.overlay.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentpushupsBinding = FragmentPushupBinding.inflate(inflater, container, false)
        return fragmentpushupsBinding.root
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseScreenWidthAndHeight()
        try {
            startTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()
        fragmentpushupsBinding.cameraButton.setOnClickListener {
            if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
                setUpCamera(CameraSelector.LENS_FACING_BACK)
            } else {
                setUpCamera(CameraSelector.LENS_FACING_FRONT)
            }
        }
        fragmentpushupsBinding.btnPlaypause.setOnClickListener {
            if(isTimerCompleted) {
                if (isPlaying) {
                    isPlaying = false
                    fragmentpushupsBinding.btnPlaypause.setImageDrawable(resources.getDrawable(R.drawable.iv_play))
                }else{
                    fragmentpushupsBinding.btnPlaypause.setImageDrawable(resources.getDrawable(R.drawable.iv_pause))
                    isPlaying = true
                }
            }else{
                Utility.showErrorSnackBar(
                    fragmentpushupsBinding.root,
                    resources.getString(R.string.timer_not_completed)
                )
            }
        }


        backgroundExecutor.execute {
            poseLandmarkHelper = PoseLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = viewModelPoseLan.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = viewModelPoseLan.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = viewModelPoseLan.currentMinPosePresenceConfidence,
                currentDelegate = viewModelPoseLan.currentDelegate,
                poseLandmarkerHelperListener = this
            )


        }

        // Wait for the views to be properly laid out
        fragmentpushupsBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera(cameraFacing)
        }
    }

    private fun startTimer() {
        object : CountDownTimer(timerLimit, timerInterval) {
            override fun onTick(millisUntilFinished: Long) {
                fragmentpushupsBinding?.lblCounter?.visibility = View.VISIBLE
                fragmentpushupsBinding?.lblCounter?.text = "${millisUntilFinished / timerInterval}"
                isTimerCompleted = false
            }

            override fun onFinish() {
                fragmentpushupsBinding.lblCounter.visibility = View.GONE
                "".also { fragmentpushupsBinding.lblCounter.text = it }
                isTimerCompleted = true
            }
        }.start()
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera(cameraFacing: Int) {
        this.cameraFacing = cameraFacing
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentpushupsBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentpushupsBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {

                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectPose(image)
                    }
                }
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
            /*  camera = cameraProvider.bindToLifecycle(
                  this, cameraSelector, preview, imageAnalyzer2
              )
  */
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentpushupsBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if (this::poseLandmarkHelper.isInitialized) {
            poseLandmarkHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentpushupsBinding.viewFinder.display.rotation
    }

    // Update UI after pose have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    @SuppressLint("SuspiciousIndentation")
    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            landMarkList.clear()
            worldLandMarkList.clear()
            if (resultBundle.results.first().landmarks() != null && resultBundle.results.first()
                    .landmarks().size > 0 && resultBundle.results.first().landmarks()[0] != null
            ) {
                for (landmark in 0 until resultBundle.results.first().landmarks()[0].size) {
                    val land = resultBundle.results.first().landmarks()[0][landmark]
                    landMarkList.add(LandMarkModel(land.x(), land.y(), land.z()))
                }

                for (landmark in 0 until resultBundle.results.first().worldLandmarks()[0].size) {
                    val land = resultBundle.results.first().landmarks()[0][landmark]
                    worldLandMarkList.add(LandMarkModel(land.x(), land.y(), land.z()))
                    landMarkList = worldLandMarkList
                }
            }


            if (landMarkList.size > 0) {
                var userFaceType = Utility.getFaceType(landMarkList[11].x, landMarkList[12].x, landMarkList[0].x)

                val cordHip: Int
                val cordKnee: Int
                val cordAnkle: Int
                val cordShoulder: Int
                val cordElbow:Int
                val wrist:Int
                val toe:Int


                if (userFaceType == ConstantsSquats.LEFT_FACE) {
                    cordHip = 23;cordKnee = 25;cordAnkle = 27; cordShoulder = 11;cordElbow=13;wrist=15;toe=31
                } else {
                    cordHip = 24;cordKnee = 26;cordAnkle = 28;cordShoulder = 12;cordElbow=14;wrist=16;toe=32
                }
                val hipPoint = doubleArrayOf(
                    landMarkList[cordHip].x.toDouble(),
                    landMarkList[cordHip].y.toDouble(),
                    landMarkList[cordHip].z.toDouble()
                )
                val kneePoint = doubleArrayOf(
                    landMarkList[cordKnee].x.toDouble(),
                    landMarkList[cordKnee].y.toDouble(),
                    landMarkList[cordKnee].z.toDouble()
                )
                val anklePoint = doubleArrayOf(
                    landMarkList[cordAnkle].x.toDouble(),
                    landMarkList[cordAnkle].y.toDouble(),
                    landMarkList[cordAnkle].z.toDouble()
                )
                val shoulderPoint = doubleArrayOf(
                    landMarkList[cordShoulder].x.toDouble(),
                    landMarkList[cordShoulder].y.toDouble(),
                    landMarkList[cordShoulder].z.toDouble()
                )

                val elbowPoint = doubleArrayOf(
                    landMarkList[cordElbow].x.toDouble(),
                    landMarkList[cordElbow].y.toDouble(),
                    landMarkList[cordElbow].z.toDouble()
                )

                val wristPoint = doubleArrayOf(
                    landMarkList[wrist].x.toDouble(),
                    landMarkList[wrist].y.toDouble(),
                    landMarkList[wrist].z.toDouble()
                )

                var kneeAngle =
                    Utility.angleBetweenPoints(hipPoint, kneePoint, anklePoint).toFloat()
                var hipAngle =
                    Utility.angleBetweenPoints(shoulderPoint, hipPoint, kneePoint).toFloat()


                var shoulderAngle = Utility.angleBetweenPoints(hipPoint, shoulderPoint, elbowPoint).toFloat()
                var elbowAngle = Utility.angleBetweenPoints(shoulderPoint, elbowPoint, wristPoint).toFloat()

                var xKnee = landMarkList[cordKnee].x
                var yKnee = landMarkList[cordKnee].y
                var xHip = landMarkList[cordHip].x
                var yHip = landMarkList[cordHip].y
                var shoulderX = landMarkList[cordShoulder].x
                var shoulderY  = landMarkList[cordShoulder].y
                var elbowX=landMarkList[cordElbow].x
                var elbowY=landMarkList[cordElbow].y
                var wristY=landMarkList[wrist].y
                var toeY=landMarkList[toe].y


                if (_fragmentpushupsBinding != null) {
                    Utility.Log("PushUPS","setResults called")
                    fragmentpushupsBinding.overlay.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                        cameraFacing, userFaceType,isTimerCompleted,kneeAngle, hipAngle, shoulderAngle, elbowAngle,xKnee,yKnee,xHip,yHip,shoulderX,shoulderY,elbowX,elbowY,isPlaying,wristY,toeY
                    )
                    fragmentpushupsBinding.overlay.invalidate()
                }

            }


        }
    }


    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == PoseLandmarkerHelper.GPU_ERROR) {
                println("error")
            }
        }
    }


    fun initialiseScreenWidthAndHeight() {
        val window: Window? = activity?.window
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        windowHeight = displayMetrics.heightPixels
        windowWidth = displayMetrics.widthPixels

    }
}