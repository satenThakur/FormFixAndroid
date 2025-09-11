package com.fittracker.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
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
import com.fittracker.R
import com.fittracker.databinding.FragmentDeadliftBinding
import com.fittracker.model.LandMarkModel
import com.fittracker.ui.activity.ExoPlayerActivity
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.MESSAGE_TYPE
import com.fittracker.utilits.ConstantsSquats.timerInterval
import com.fittracker.utilits.ConstantsSquats.timerLimit
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.FormFixSharedPreferences
import com.fittracker.utilits.FormFixUtility
import com.fittracker.viewmodel.MainViewModel
import com.fittracker.viewmodel.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import androidx.navigation.findNavController
import com.fittracker.landmarkModels.Point3D
import com.fittracker.model.ErrorMessage

class DeadLiftFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener {
    companion object {
        private const val TAG = "Form Fit"
    }
    private var _fragmentDeadLiftBinding: FragmentDeadliftBinding? = null
    private val fragmentDeadLiftBinding get() = _fragmentDeadLiftBinding!!
    private lateinit var poseLandmarkHelper: PoseLandmarkerHelper
    private val viewModelPoseLan: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private var landMarkList = ArrayList<LandMarkModel>()
    private var worldLandMarkList = ArrayList<LandMarkModel>()
    private var userFaceType = 0
    private var xKnee = -100F
    private var yKnee = -100F
    private var xHip = -100F
    private var yHip = -100F
    private var toeX = -100F
    private var xHeel = -100F
    private var yHeel = -100F
    private var rightToeX=-100F
    private var leftToeX=-100F
    private var xofRightHip=-100F
    private var xofLeftHip=-100F
    private var yofRightShoulder=-100F
    private var yofLeftShoulder=-100F
    private var xofLeftKnee=-100F
    private var xofRightKnee=-100F
    private var xofLeftToe=-100F
    private var xofRightToe=-100F
    private var yOfToe=-100f
    private var yOfRightHeel=-100F
    private var yOfLeftHeel=-100F
    private var yOfRightToe=-100F
    private var yOfLeftToe=-100F
    private var yoFShoulder=-100f;
    private var yForNose=-100f;
    private var shulderY=-100f
    private var shoulderx=-100f
    private var ankleX=-100f
    private var ankleY=-100f
    private var isTimerCompleted = false
    private var windowHeight=0
    private var windowWidth=0
    private var isPlaying=true
    private var height=0

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService
    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            requireActivity().findNavController(R.id.fragment_container).navigate(R.id.action_squat_to_permissions)
        }
        // Start the PoseLandmarkHelper again when users come back to the foreground.
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
            // Close the PoseLandmarkHelper and release resources
            backgroundExecutor.execute { poseLandmarkHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentDeadLiftBinding = null
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
        _fragmentDeadLiftBinding = FragmentDeadliftBinding.inflate(inflater, container, false)
        return fragmentDeadLiftBinding.root
    }
    @SuppressLint("SuspiciousIndentation")
    private fun errorMessageClick(msg:String){
        var intent = Intent(context, ExoPlayerActivity::class.java)
        intent.putExtra(ConstantsSquats.FILE_NAME, "hipcorrection")
        intent.putExtra(ConstantsSquats.FILE_TYPE, 1)
        intent.putExtra(MESSAGE_TYPE,msg)
        startActivity(intent)
    }
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseScreenWidthAndHeight()
        try{
            startTimer()
        }catch (e:Exception){
            e.printStackTrace()
        }
        var heightString= FormFixSharedPreferences.getSharedPrefStringValue(requireContext(), FormFixConstants.HEIGHT)
        height=heightString!!.toInt()
        backgroundExecutor = Executors.newSingleThreadExecutor()
        fragmentDeadLiftBinding.cameraButton.setOnClickListener {
            if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
                setUpCamera(CameraSelector.LENS_FACING_BACK)
            } else {
                setUpCamera(CameraSelector.LENS_FACING_FRONT)
            }
        }
        fragmentDeadLiftBinding.btnPlaypause.setOnClickListener {
            if(isTimerCompleted) {
                if (isPlaying) {
                    isPlaying = false
                    fragmentDeadLiftBinding.btnPlaypause.setImageDrawable(resources.getDrawable(R.drawable.iv_play))
                }else{
                    fragmentDeadLiftBinding.btnPlaypause.setImageDrawable(resources.getDrawable(R.drawable.iv_pause))
                    isPlaying = true
                }
            }else{
                FormFixUtility.showErrorSnackBar(
                    fragmentDeadLiftBinding.root,
                    resources.getString(R.string.timer_not_completed)
                )
            }
        }

        fragmentDeadLiftBinding.relay1.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage1.text.toString())
        }
        fragmentDeadLiftBinding.relay2.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage2.text.toString())
        }
        fragmentDeadLiftBinding.relay3.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage3.text.toString())
        }
        fragmentDeadLiftBinding.relay4.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage4.text.toString())
        }
        fragmentDeadLiftBinding.relay5.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage5.text.toString())
        }
        fragmentDeadLiftBinding.relay6.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage6.text.toString())
        }
        fragmentDeadLiftBinding.relay7.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage7.text.toString())
        }
        fragmentDeadLiftBinding.relay8.setOnClickListener {
            errorMessageClick(fragmentDeadLiftBinding.tvMessage8.text.toString())
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
        fragmentDeadLiftBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera(cameraFacing)
        }
    }
    private fun startTimer() {
        object : CountDownTimer(timerLimit, timerInterval) {
            override fun onTick(millisUntilFinished: Long) {
                fragmentDeadLiftBinding?.lblCounter?.visibility = View.VISIBLE
                fragmentDeadLiftBinding?.lblCounter?.text = "${millisUntilFinished / timerInterval}"
                isTimerCompleted = false
            }

            override fun onFinish() {
                fragmentDeadLiftBinding.lblCounter.visibility = View.GONE
                "".also { fragmentDeadLiftBinding.lblCounter.text = it }
                isTimerCompleted = true
            }
        }.start()
    }
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
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentDeadLiftBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentDeadLiftBinding.viewFinder.display.rotation)
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
            preview?.setSurfaceProvider(fragmentDeadLiftBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            FormFixUtility.Log(TAG, "Use case binding failed$exc")
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
            fragmentDeadLiftBinding.viewFinder.display.rotation
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            landMarkList.clear()
            worldLandMarkList.clear()
            var shoulderAngle=0.0f
            var kneeAngle = 0.0f
            var hipAngle = 0.0f
            var heelAngle = 0.0f
            var leftParlelAngle=0
            var rightParaleleAngle=0
            var  lAnkle3D = Point3D(0f, 0f, 0f)
            var rAnkle3D = Point3D(0f, 0f, 0f)
            var lWrist3D =Point3D(0f, 0f, 0f)
            var rWrist3D = Point3D(0f, 0f, 0f)
            var leftFootIndex= Point3D(0f, 0f, 0f)
            var rightFootIndex= Point3D(0f, 0f, 0f)
            var leftHeel= Point3D(0f, 0f, 0f)
            var rightHeel= Point3D(0f, 0f, 0f)
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
                userFaceType = 0
                val leftShoulder = landMarkList[11].x
                val rightShoulder = landMarkList[12].x
                val leftShoulder_Y = landMarkList[11].y
                val rightShoulder_Y = landMarkList[12].y

                val nose = landMarkList[0].x
                val leftSdoulder_node_distance = leftShoulder - nose
                val rightSdoulder_node_distance = rightShoulder - nose
                var shouldersDiff = abs(leftShoulder - rightShoulder)
                if (shouldersDiff > ConstantsSquats.SHOULDERSDIFF_CONSTANT) {
                    userFaceType = ConstantsSquats.FRONT_FACE
                } else {
                    if (leftSdoulder_node_distance > 0 && rightSdoulder_node_distance > 0) {
                        userFaceType = ConstantsSquats.LEFT_FACE
                    } else {
                        userFaceType = ConstantsSquats.RIGHT_FACE
                    }
                }


                val cordHip: Int
                val cordKnee: Int
                val cordAnkle: Int
                val cordShoulder: Int
                val cordElbow: Int
                val cordToe: Int
                val rHeelCord = 30
                val lHeelCord = 29
                val lfiCord = 31
                val rfICord = 32


                if (userFaceType == ConstantsSquats.LEFT_FACE) {
                    cordElbow = 13; cordHip = 23;cordKnee = 25;cordAnkle = 27;cordShoulder =
                        11;cordToe = 31
                } else {
                    cordElbow = 14;cordHip = 24;cordKnee = 26;cordAnkle = 28;cordShoulder =
                        12;cordToe = 32
                }
                val elbowPoint = doubleArrayOf(
                    landMarkList[cordElbow].x.toDouble(),
                    landMarkList[cordElbow].y.toDouble(),
                    landMarkList[cordElbow].z.toDouble()
                )
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
                val leftFootPoint = doubleArrayOf(
                    landMarkList[lfiCord].x.toDouble(),
                    landMarkList[lfiCord].y.toDouble(),
                    landMarkList[lfiCord].z.toDouble()
                )
                val heelPoint = doubleArrayOf(
                    (landMarkList[lHeelCord].x.toDouble() + landMarkList[lHeelCord].x.toDouble()) / 2,
                    (landMarkList[lHeelCord].y.toDouble() + landMarkList[lHeelCord].y.toDouble()) / 2,
                    (landMarkList[lHeelCord].z.toDouble() + landMarkList[lHeelCord].z.toDouble()) / 2,
                )
                val rightFootPoint = doubleArrayOf(
                    landMarkList[rfICord].x.toDouble(),
                    landMarkList[rfICord].y.toDouble(),
                    landMarkList[rfICord].z.toDouble()
                )
                var leftAnklePoint= doubleArrayOf(
                    landMarkList[27].x.toDouble(),
                    landMarkList[27].y.toDouble(),
                    landMarkList[27].z.toDouble()
                )
                var rightAnklePoint= doubleArrayOf(
                    landMarkList[28].x.toDouble(),
                    landMarkList[28].y.toDouble(),
                    landMarkList[28].z.toDouble()
                )
                var leftWristPoint= doubleArrayOf(
                    landMarkList[15].x.toDouble(),
                    landMarkList[15].y.toDouble(),
                    landMarkList[15].z.toDouble()
                )
                var rightWristPoint= doubleArrayOf(
                    landMarkList[16].x.toDouble(),
                    landMarkList[16].y.toDouble(),
                    landMarkList[16].z.toDouble()
                )


                leftParlelAngle=FormFixUtility.angleBetweenPoints(rightAnklePoint, leftAnklePoint, rightWristPoint).toInt()
                rightParaleleAngle=FormFixUtility.angleBetweenX(leftAnklePoint, rightAnklePoint, leftWristPoint).toInt()
                kneeAngle = FormFixUtility.angleBetweenPoints(hipPoint, kneePoint, anklePoint).toFloat()
                hipAngle = FormFixUtility.angleBetweenPoints(shoulderPoint, hipPoint, kneePoint).toFloat()
                 heelAngle = FormFixUtility.angleBetweenPoints(leftFootPoint, heelPoint, rightFootPoint).toFloat()
                 shoulderAngle = FormFixUtility.angleBetweenPoints(hipPoint, shoulderPoint, elbowPoint).toFloat()
                 lAnkle3D= Point3D(landMarkList[27].x,landMarkList[27].y,landMarkList[27].z)
                 rAnkle3D=Point3D(landMarkList[28].x,landMarkList[28].y,landMarkList[28].z)
                 lWrist3D=Point3D(landMarkList[15].x,landMarkList[15].y,landMarkList[15].z)
                 rWrist3D=Point3D(landMarkList[16].x,landMarkList[16].y,landMarkList[16].z)

                leftFootIndex=Point3D(landMarkList[31].x,landMarkList[31].y,landMarkList[31].z)
                rightFootIndex=Point3D(landMarkList[32].x,landMarkList[32].y,landMarkList[32].z)
                leftHeel=Point3D(landMarkList[29].x,landMarkList[29].y,landMarkList[29].z)
                rightHeel=Point3D(landMarkList[30].x,landMarkList[30].y,landMarkList[30].z)


                 xHip = landMarkList[cordHip].x
                yHip = landMarkList[cordHip].y
                xKnee = landMarkList[cordKnee].x
                yKnee = landMarkList[cordKnee].y
                shoulderx = landMarkList[cordShoulder].x
                shulderY = landMarkList[cordShoulder].y
                ankleX = landMarkList[cordAnkle].x
                ankleY = landMarkList[cordAnkle].y
                toeX = landMarkList[cordToe].x
                xHeel = (landMarkList[lHeelCord].x + landMarkList[rHeelCord].x) / 2
                yHeel = landMarkList[lHeelCord].y
                xofLeftKnee = landMarkList[25].x
                xofRightKnee = landMarkList[26].x
                xofLeftToe = landMarkList[29].x
                xofRightToe = landMarkList[30].x
                leftToeX = landMarkList[31].x
                rightToeX = landMarkList[32].x
                yOfToe = landMarkList[32].y
                yoFShoulder = landMarkList[11].y
                yForNose = landMarkList[0].y
                xofLeftHip = landMarkList[23].x
                xofRightHip = landMarkList[24].x
                yofLeftShoulder = landMarkList[11].y
                yofRightShoulder = landMarkList[12].y
                yOfLeftHeel = landMarkList[29].y
                yOfRightHeel = landMarkList[30].y
                yOfLeftToe = landMarkList[31].y
                yOfRightToe = landMarkList[32].y
                showBottomLayoutValues(
                    userFaceType,
                    kneeAngle,
                    hipAngle,
                    isTimerCompleted,
                    xofLeftToe,
                    xofRightToe,
                    xofLeftHip,
                    xofRightHip,
                    leftShoulder,
                    rightShoulder,
                    leftShoulder_Y,
                    rightShoulder_Y
                )
            }

            if (_fragmentDeadLiftBinding != null) {
                // Pass necessary information to OverlayView for drawing on the canvas
                var faceType="NOT_DECIDED"
                if (userFaceType == 1)
                    faceType="LEFT_FACE"
                else if (userFaceType == 2) {
                    faceType="FRONT_FACE"
                } else if (userFaceType == 3) {
                    faceType="RIGHT_FACE"
                }
                //DeadLiftUtility.Log("Angels=>", "faceType="+faceType+" shldrA=" + shoulderAngle+" kneeA="+kneeAngle+" hipA="+hipAngle+" heelA="+heelAngle)

                fragmentDeadLiftBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM,
                    cameraFacing,
                    userFaceType,
                    kneeAngle,
                    hipAngle,
                    heelAngle,
                    shoulderAngle,
                     leftParlelAngle,
                     rightParaleleAngle,
                    lAnkle3D,
                    rAnkle3D,
                    lWrist3D,
                    rWrist3D,
                    leftFootIndex,
                    rightFootIndex,
                    leftHeel,
                    rightHeel,
                    xHeel,
                    yHeel,
                    xHip, yHip, xKnee, yKnee, toeX, isTimerCompleted,xofLeftKnee,xofRightKnee,xofLeftToe,xofRightToe,
                    yOfToe,yoFShoulder,yForNose,shoulderx,shulderY,ankleX,ankleY,windowWidth,windowHeight,isPlaying,leftToeX,rightToeX,xofLeftHip,xofRightHip,yofLeftShoulder,yofRightShoulder,yOfLeftHeel,yOfRightHeel,yOfLeftToe,yOfRightToe)
                setAdapterData(fragmentDeadLiftBinding.overlay.errorMessageList)
                fragmentDeadLiftBinding.overlay.invalidate()
            }
        }
    }
    private fun setAdapterData(errorMessageList: List<ErrorMessage>) {
        activity?.runOnUiThread(Runnable {
            if(errorMessageList.isEmpty()){
                fragmentDeadLiftBinding.relayMessage.visibility=View.GONE
                fragmentDeadLiftBinding.relay1.visibility=View.GONE
                fragmentDeadLiftBinding.relay2.visibility=View.GONE
                fragmentDeadLiftBinding.relay3.visibility=View.GONE
                fragmentDeadLiftBinding.relay4.visibility=View.GONE
                fragmentDeadLiftBinding.relay5.visibility=View.GONE
                fragmentDeadLiftBinding.relay6.visibility=View.GONE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
            }else if(errorMessageList.size==1){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.GONE
                fragmentDeadLiftBinding.relay3.visibility=View.GONE
                fragmentDeadLiftBinding.relay4.visibility=View.GONE
                fragmentDeadLiftBinding.relay5.visibility=View.GONE
                fragmentDeadLiftBinding.relay6.visibility=View.GONE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
            }else if(errorMessageList.size==2){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.GONE
                fragmentDeadLiftBinding.relay4.visibility=View.GONE
                fragmentDeadLiftBinding.relay5.visibility=View.GONE
                fragmentDeadLiftBinding.relay6.visibility=View.GONE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text= errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
            }else if(errorMessageList.size==3){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay4.visibility=View.GONE
                fragmentDeadLiftBinding.relay5.visibility=View.GONE
                fragmentDeadLiftBinding.relay6.visibility=View.GONE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text= errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
                fragmentDeadLiftBinding.tvMessage3.text= errorMessageList[2].message
                fragmentDeadLiftBinding.tvCount3.text=""+ errorMessageList[2].count
            }
            else if(errorMessageList.size==4){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay4.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay5.visibility=View.GONE
                fragmentDeadLiftBinding.relay6.visibility=View.GONE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text=errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
                fragmentDeadLiftBinding.tvMessage3.text= errorMessageList[2].message
                fragmentDeadLiftBinding.tvCount3.text=""+ errorMessageList[2].count
                fragmentDeadLiftBinding.tvMessage4.text= errorMessageList[3].message
                fragmentDeadLiftBinding.tvCount4.text=""+ errorMessageList[3].count
            }else if(errorMessageList.size==5){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay4.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay5.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay6.visibility=View.GONE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text=errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
                fragmentDeadLiftBinding.tvMessage3.text= errorMessageList[2].message
                fragmentDeadLiftBinding.tvCount3.text=""+ errorMessageList[2].count
                fragmentDeadLiftBinding.tvMessage4.text= errorMessageList[3].message
                fragmentDeadLiftBinding.tvCount4.text=""+ errorMessageList[3].count
                fragmentDeadLiftBinding.tvMessage5.text= errorMessageList[4].message
                fragmentDeadLiftBinding.tvCount5.text=""+ errorMessageList[4].count
            }else if(errorMessageList.size==6){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay4.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay5.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay6.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay7.visibility=View.GONE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text=errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
                fragmentDeadLiftBinding.tvMessage3.text= errorMessageList[2].message
                fragmentDeadLiftBinding.tvCount3.text=""+ errorMessageList[2].count
                fragmentDeadLiftBinding.tvMessage4.text= errorMessageList[3].message
                fragmentDeadLiftBinding.tvCount4.text=""+ errorMessageList[3].count
                fragmentDeadLiftBinding.tvMessage5.text= errorMessageList[4].message
                fragmentDeadLiftBinding.tvCount5.text=""+ errorMessageList[4].count
                fragmentDeadLiftBinding.tvMessage6.text= errorMessageList[5].message
                fragmentDeadLiftBinding.tvCount6.text=""+ errorMessageList[5].count
            }else if(errorMessageList.size==7){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay4.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay5.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay6.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay7.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay8.visibility=View.GONE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text=errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
                fragmentDeadLiftBinding.tvMessage3.text= errorMessageList[2].message
                fragmentDeadLiftBinding.tvCount3.text=""+ errorMessageList[2].count
                fragmentDeadLiftBinding.tvMessage4.text= errorMessageList[3].message
                fragmentDeadLiftBinding.tvCount4.text=""+ errorMessageList[3].count
                fragmentDeadLiftBinding.tvMessage5.text= errorMessageList[4].message
                fragmentDeadLiftBinding.tvCount5.text=""+ errorMessageList[4].count
                fragmentDeadLiftBinding.tvMessage6.text= errorMessageList[5].message
                fragmentDeadLiftBinding.tvCount6.text=""+ errorMessageList[5].count
                fragmentDeadLiftBinding.tvMessage7.text= errorMessageList[6].message
                fragmentDeadLiftBinding.tvCount7.text=""+ errorMessageList[6].count
            }else if(errorMessageList.size==8){
                fragmentDeadLiftBinding.relayMessage.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay1.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay2.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay3.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay4.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay5.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay6.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay7.visibility=View.VISIBLE
                fragmentDeadLiftBinding.relay8.visibility=View.VISIBLE
                fragmentDeadLiftBinding.tvMessage1.text= errorMessageList[0].message
                fragmentDeadLiftBinding.tvCount1.text=""+ errorMessageList[0].count
                fragmentDeadLiftBinding.tvMessage2.text=errorMessageList[1].message
                fragmentDeadLiftBinding.tvCount2.text=""+ errorMessageList[1].count
                fragmentDeadLiftBinding.tvMessage3.text= errorMessageList[2].message
                fragmentDeadLiftBinding.tvCount3.text=""+ errorMessageList[2].count
                fragmentDeadLiftBinding.tvMessage4.text= errorMessageList[3].message
                fragmentDeadLiftBinding.tvCount4.text=""+ errorMessageList[3].count
                fragmentDeadLiftBinding.tvMessage5.text= errorMessageList[4].message
                fragmentDeadLiftBinding.tvCount5.text=""+ errorMessageList[4].count
                fragmentDeadLiftBinding.tvMessage6.text= errorMessageList[5].message
                fragmentDeadLiftBinding.tvCount6.text=""+ errorMessageList[5].count
                fragmentDeadLiftBinding.tvMessage7.text= errorMessageList[6].message
                fragmentDeadLiftBinding.tvCount7.text=""+ errorMessageList[6].count
                fragmentDeadLiftBinding.tvCount8.text=""+ errorMessageList[7].count
                fragmentDeadLiftBinding.tvMessage8.text=""+ errorMessageList[7].count
            }
        })
    }
    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == PoseLandmarkerHelper.GPU_ERROR) {
                println("error")
            }
        }
    }
    private fun initialiseScreenWidthAndHeight() {
        val window: Window? = activity?.window
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        windowHeight = displayMetrics.heightPixels
        windowWidth = displayMetrics.widthPixels
        FormFixConstants.SCREEN_HEIGHT=windowHeight
        FormFixConstants.SCREEN_WIDTH=windowWidth

    }
    private fun showBottomLayoutValues(userFaceType:Int, kneesAngle: Float, hipAngle: Float, isTimerCompleted:Boolean, toe1_X: Float,
                                       toe2_X: Float,
                                       hip1_X: Float,
                                       hip2_X: Float, sholder1_X: Float,
                                       shoulder2_x: Float, sholder1_Y: Float,
                                       shoulder2_Y: Float){
        if(isTimerCompleted) {
            fragmentDeadLiftBinding.bottomLayout.visibility = View.VISIBLE
            /*val kneeAngles = (kneesAngle * 10).roundToInt() / 10

            if (userFaceType == ConstantsSquats.FRONT_FACE) {
                fragmentDeadLiftBinding.valueThighAngle.text = "" + kneeAngles+" deg"
                fragmentDeadLiftBinding.valueSquatDepth.text = ""+ FormFixUtility.getSquatPercentage(kneesAngle.toInt(),hipAngle.toInt(),userFaceType)+"%"
                fragmentDeadLiftBinding.valueHipShift.text =
                    "" + FormFixUtility.hipShift( toe1_X,
                        toe2_X,
                        hip1_X,
                        hip2_X,
                        height)+" in"
                fragmentDeadLiftBinding.valueShoulderShift.text =
                    "" + FormFixUtility.shoulderShift(
                        sholder1_Y,
                        shoulder2_Y,
                        height,
                        FormFixConstants.SCREEN_HEIGHT,
                        yOfLeftHeel,
                        yOfRightHeel,
                        yForNose)+" in"
                fragmentDeadLiftBinding.lblShoulderShift.text="Shoulders Tilt:"
                fragmentDeadLiftBinding.lblHipShift.text="Hips Shift:"
            } else {
                fragmentDeadLiftBinding.valueThighAngle.text = "" + kneeAngles+"°"
                fragmentDeadLiftBinding.valueSquatDepth.text = ""+ FormFixUtility.getSquatPercentage(kneesAngle.toInt(), hipAngle.toInt(),userFaceType)+"%"
                fragmentDeadLiftBinding.valueHipShift.text = ""+FormFixUtility.kneesCrossToesShift(toeX,xKnee,height)+" in"
                fragmentDeadLiftBinding.lblHipShift.text="Knees Shift:"
                fragmentDeadLiftBinding.valueShoulderShift.text =""+FormFixUtility.heelsShift(yOfLeftHeel, yOfRightHeel, yOfLeftToe, yOfRightToe, userFaceType,height)+" in"
                fragmentDeadLiftBinding.lblShoulderShift.text="Heels Shift:"
            }*/
        }

    }
}