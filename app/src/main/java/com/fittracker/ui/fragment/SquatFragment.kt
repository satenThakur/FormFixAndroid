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
import androidx.navigation.Navigation
import com.fittracker.R
import com.fittracker.ui.activity.VideoPlayerActivity
import com.fittracker.databinding.FragmentSquatsBinding
import com.fittracker.model.ErrorMessage
import com.fittracker.model.LandMarkModel
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.MESSAGE_TYPE
import com.fittracker.utilits.ConstantsSquats.timerInterval
import com.fittracker.utilits.ConstantsSquats.timerLimit
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.MainViewModel
import com.fittracker.viewmodel.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SquatFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener {
    companion object {
        private const val TAG = "Form Fit"
    }

    private var _fragmentSquatsBinding: FragmentSquatsBinding? = null
    private val fragmentSquatsBinding get() = _fragmentSquatsBinding!!
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
    private var xofLeftKnee=-100F
    private var xofRightKnee=-100F
    private var xofLeftToe=-100F
    private var xofRightToe=-100F
    private var yOfToe=-100f
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

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService
    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_squat_to_permissions)
        }

        // Start the PoseLandmarkHelper again when users come back
        // to the foreground.
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
        _fragmentSquatsBinding = null
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
        _fragmentSquatsBinding = FragmentSquatsBinding.inflate(inflater, container, false)
        return fragmentSquatsBinding.root
    }

    private fun setAdapterData(errorMessageList: List<ErrorMessage>) {
        activity?.runOnUiThread(Runnable {
        if(errorMessageList.isEmpty()){
            fragmentSquatsBinding.relay1.visibility=View.GONE
            fragmentSquatsBinding.relay2.visibility=View.GONE
            fragmentSquatsBinding.relay3.visibility=View.GONE
            fragmentSquatsBinding.relay4.visibility=View.GONE
            fragmentSquatsBinding.relay5.visibility=View.GONE
            fragmentSquatsBinding.relay6.visibility=View.GONE
            fragmentSquatsBinding.relay7.visibility=View.GONE
        }else if(errorMessageList.size==1){
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.GONE
            fragmentSquatsBinding.relay3.visibility=View.GONE
            fragmentSquatsBinding.relay4.visibility=View.GONE
            fragmentSquatsBinding.relay5.visibility=View.GONE
            fragmentSquatsBinding.relay6.visibility=View.GONE
            fragmentSquatsBinding.relay7.visibility=View.GONE
        }else if(errorMessageList.size==2){
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.VISIBLE
            fragmentSquatsBinding.relay3.visibility=View.GONE
            fragmentSquatsBinding.relay4.visibility=View.GONE
            fragmentSquatsBinding.relay5.visibility=View.GONE
            fragmentSquatsBinding.relay6.visibility=View.GONE
            fragmentSquatsBinding.relay7.visibility=View.GONE
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.tvMessage2.text= errorMessageList[1].message
            fragmentSquatsBinding.tvCount2.text=""+ errorMessageList[1].count
        }else if(errorMessageList.size==3){
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.VISIBLE
            fragmentSquatsBinding.relay3.visibility=View.VISIBLE
            fragmentSquatsBinding.relay4.visibility=View.GONE
            fragmentSquatsBinding.relay5.visibility=View.GONE
            fragmentSquatsBinding.relay6.visibility=View.GONE
            fragmentSquatsBinding.relay7.visibility=View.GONE
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.tvMessage2.text= errorMessageList[1].message
            fragmentSquatsBinding.tvCount2.text=""+ errorMessageList[1].count
            fragmentSquatsBinding.tvMessage3.text= errorMessageList[2].message
            fragmentSquatsBinding.tvCount3.text=""+ errorMessageList[2].count
        }
        else if(errorMessageList.size==4){
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.VISIBLE
            fragmentSquatsBinding.relay3.visibility=View.VISIBLE
            fragmentSquatsBinding.relay4.visibility=View.VISIBLE
            fragmentSquatsBinding.relay5.visibility=View.GONE
            fragmentSquatsBinding.relay6.visibility=View.GONE
            fragmentSquatsBinding.relay7.visibility=View.GONE
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.tvMessage2.text=errorMessageList[1].message
            fragmentSquatsBinding.tvCount2.text=""+ errorMessageList[1].count
            fragmentSquatsBinding.tvMessage3.text= errorMessageList[2].message
            fragmentSquatsBinding.tvCount3.text=""+ errorMessageList[2].count
            fragmentSquatsBinding.tvMessage4.text= errorMessageList[3].message
            fragmentSquatsBinding.tvCount4.text=""+ errorMessageList[3].count
        }else if(errorMessageList.size==5){
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.VISIBLE
            fragmentSquatsBinding.relay3.visibility=View.VISIBLE
            fragmentSquatsBinding.relay4.visibility=View.VISIBLE
            fragmentSquatsBinding.relay5.visibility=View.VISIBLE
            fragmentSquatsBinding.relay6.visibility=View.GONE
            fragmentSquatsBinding.relay7.visibility=View.GONE
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.tvMessage2.text=errorMessageList[1].message
            fragmentSquatsBinding.tvCount2.text=""+ errorMessageList[1].count
            fragmentSquatsBinding.tvMessage3.text= errorMessageList[2].message
            fragmentSquatsBinding.tvCount3.text=""+ errorMessageList[2].count
            fragmentSquatsBinding.tvMessage4.text= errorMessageList[3].message
            fragmentSquatsBinding.tvCount4.text=""+ errorMessageList[3].count
            fragmentSquatsBinding.tvMessage5.text= errorMessageList[4].message
            fragmentSquatsBinding.tvCount5.text=""+ errorMessageList[4].count
        }else if(errorMessageList.size==6){
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.VISIBLE
            fragmentSquatsBinding.relay3.visibility=View.VISIBLE
            fragmentSquatsBinding.relay4.visibility=View.VISIBLE
            fragmentSquatsBinding.relay5.visibility=View.VISIBLE
            fragmentSquatsBinding.relay6.visibility=View.VISIBLE
            fragmentSquatsBinding.relay7.visibility=View.GONE
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.tvMessage2.text=errorMessageList[1].message
            fragmentSquatsBinding.tvCount2.text=""+ errorMessageList[1].count
            fragmentSquatsBinding.tvMessage3.text= errorMessageList[2].message
            fragmentSquatsBinding.tvCount3.text=""+ errorMessageList[2].count
            fragmentSquatsBinding.tvMessage4.text= errorMessageList[3].message
            fragmentSquatsBinding.tvCount4.text=""+ errorMessageList[3].count
            fragmentSquatsBinding.tvMessage5.text= errorMessageList[4].message
            fragmentSquatsBinding.tvCount5.text=""+ errorMessageList[4].count
            fragmentSquatsBinding.tvMessage6.text= errorMessageList[5].message
            fragmentSquatsBinding.tvCount6.text=""+ errorMessageList[5].count
        }else if(errorMessageList.size==7){
            fragmentSquatsBinding.relay1.visibility=View.VISIBLE
            fragmentSquatsBinding.relay2.visibility=View.VISIBLE
            fragmentSquatsBinding.relay3.visibility=View.VISIBLE
            fragmentSquatsBinding.relay4.visibility=View.VISIBLE
            fragmentSquatsBinding.relay5.visibility=View.VISIBLE
            fragmentSquatsBinding.relay6.visibility=View.VISIBLE
            fragmentSquatsBinding.relay7.visibility=View.VISIBLE
            fragmentSquatsBinding.tvMessage1.text= errorMessageList[0].message
            fragmentSquatsBinding.tvCount1.text=""+ errorMessageList[0].count
            fragmentSquatsBinding.tvMessage2.text=errorMessageList[1].message
            fragmentSquatsBinding.tvCount2.text=""+ errorMessageList[1].count
            fragmentSquatsBinding.tvMessage3.text= errorMessageList[2].message
            fragmentSquatsBinding.tvCount3.text=""+ errorMessageList[2].count
            fragmentSquatsBinding.tvMessage4.text= errorMessageList[3].message
            fragmentSquatsBinding.tvCount4.text=""+ errorMessageList[3].count
            fragmentSquatsBinding.tvMessage5.text= errorMessageList[4].message
            fragmentSquatsBinding.tvCount5.text=""+ errorMessageList[4].count
            fragmentSquatsBinding.tvMessage6.text= errorMessageList[5].message
            fragmentSquatsBinding.tvCount6.text=""+ errorMessageList[5].count
            fragmentSquatsBinding.tvMessage6.text= errorMessageList[6].message
            fragmentSquatsBinding.tvCount6.text=""+ errorMessageList[6].count
        }
        })
    }
    @SuppressLint("SuspiciousIndentation")
    private fun errorMessageClick(msg:String){
         var intent = Intent(context, VideoPlayerActivity::class.java)
              intent.putExtra(ConstantsSquats.FILE_NAME, "hipcorrection")
              intent.putExtra(ConstantsSquats.FILE_TYPE, 1)
              intent.putExtra(MESSAGE_TYPE,msg)
              startActivity(intent)
             // activity?.finish()
/*      var intent = Intent(context, ImageActivity::class.java)
         intent.putExtra(Constants.FILE_NAME, "hipcorrection")
         intent.putExtra(Constants.FILE_TYPE, 1)
         intent.putExtra(MESSAGE_TYPE,msg)
         activity?.startActivity(intent)*/
      //  activity?.finish()

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

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()
        fragmentSquatsBinding.cameraButton.setOnClickListener {
            if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
                setUpCamera(CameraSelector.LENS_FACING_BACK)
            } else {
                setUpCamera(CameraSelector.LENS_FACING_FRONT)
            }
        }
        fragmentSquatsBinding.btnPlaypause.setOnClickListener {
            if(isTimerCompleted) {
                if (isPlaying) {
                    isPlaying = false
                    fragmentSquatsBinding.btnPlaypause.setImageDrawable(resources.getDrawable(R.drawable.iv_play))
                }else{
                    fragmentSquatsBinding.btnPlaypause.setImageDrawable(resources.getDrawable(R.drawable.iv_pause))
                    isPlaying = true
                }
            }else{
                Utility.showErrorSnackBar(
                    fragmentSquatsBinding.root,
                    resources.getString(R.string.timer_not_completed)
                )
            }
        }

        fragmentSquatsBinding.relay1.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage1.text.toString())
        }
        fragmentSquatsBinding.relay2.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage2.text.toString())
        }
        fragmentSquatsBinding.relay3.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage3.text.toString())
        }
        fragmentSquatsBinding.relay4.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage4.text.toString())
        }
        fragmentSquatsBinding.relay5.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage5.text.toString())
        }
        fragmentSquatsBinding.relay6.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage6.text.toString())
        }
        fragmentSquatsBinding.relay7.setOnClickListener {
            errorMessageClick(fragmentSquatsBinding.tvMessage6.text.toString())
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
        fragmentSquatsBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera(cameraFacing)
        }
    }

    private fun startTimer() {
        object : CountDownTimer(timerLimit, timerInterval) {
            override fun onTick(millisUntilFinished: Long) {
                fragmentSquatsBinding?.lblCounter?.visibility = View.VISIBLE
                fragmentSquatsBinding?.lblCounter?.text = "${millisUntilFinished / timerInterval}"
                isTimerCompleted = false
            }

            override fun onFinish() {
                fragmentSquatsBinding.lblCounter.visibility = View.GONE
                "".also { fragmentSquatsBinding.lblCounter.text = it }
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
            .setTargetRotation(fragmentSquatsBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentSquatsBinding.viewFinder.display.rotation)
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
            preview?.setSurfaceProvider(fragmentSquatsBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Utility.Log(TAG, "Use case binding failed$exc")
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
            fragmentSquatsBinding.viewFinder.display.rotation
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
            var kneeAngle = 0.0f
            var hipAngle = 0.0f
            var heelAngle = 0.0f
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
                val nose = landMarkList[0].x
                val leftSdoulder_node_distance = leftShoulder - nose
                val rightSdoulder_node_distance = rightShoulder - nose
                val leftNoseDistance = nose - leftShoulder
                val rightNoseDistance = nose - rightShoulder
                Utility.Log("FACETYPE","leftSdoulder_node_distance="+leftSdoulder_node_distance)
                Utility.Log("FACETYPE","rightSdoulder_node_distance="+rightSdoulder_node_distance)
                Utility.Log("FACETYPE","leftNoseDistance="+leftNoseDistance)
                Utility.Log("FACETYPE","rightNoseDistance="+rightNoseDistance)
                Utility.Log("FACETYPE","leftShoulder="+leftShoulder+", rightShoulde="+rightShoulder)
                Utility.Log("FACETYPE","shouldersDiff="+Math.abs(leftShoulder-rightShoulder))
               var shouldersDiff= abs(leftShoulder-rightShoulder)
              /*  userFaceType = if (leftSdoulder_node_distance > 0 && rightSdoulder_node_distance > 0) {
                    Constants.LEFT_FACE
                }
                else if (leftNoseDistance > 0 && rightNoseDistance > 0) {
                    Constants.RIGHT_FACE
                }
                else {
                    Constants.FRONT_FACE
                }*/

                if (shouldersDiff > ConstantsSquats.SHOULDERSDIFF_CONSTANT) {
                   userFaceType= ConstantsSquats.FRONT_FACE
                }
                else{
                    if(leftSdoulder_node_distance > 0 && rightSdoulder_node_distance > 0) {
                        userFaceType= ConstantsSquats.LEFT_FACE
                    }
                    else  {
                        userFaceType= ConstantsSquats.RIGHT_FACE
                    }
                }

                if(userFaceType==1)
                Utility.Log("FACETYPE","LEFT_FACE")
                else if(userFaceType==2){
                    Utility.Log("FACETYPE","FRONT_FACE")
                }else if(userFaceType==3){
                    Utility.Log("FACETYPE","RIGHT_FACE")
                }else{
                    Utility.Log("FACETYPE","NOT_DECIDED")
                }

                val cordHip: Int
                val cordKnee: Int
                val cordAnkle: Int
                val cordShoulder: Int
                val cordToe: Int
                val rHeelCord = 30
                val lHeelCord = 29
                val lfiCord = 31
                val rfICord = 32


                if (userFaceType == ConstantsSquats.LEFT_FACE) {
                    cordHip = 23;cordKnee = 25;cordAnkle = 27;cordShoulder = 11;cordToe = 31
                } else {
                    cordHip = 24;cordKnee = 26;cordAnkle = 28;cordShoulder = 12;cordToe = 32
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

                kneeAngle = Utility.angleBetweenPoints(hipPoint, kneePoint, anklePoint).toFloat()
                hipAngle = Utility.angleBetweenPoints(shoulderPoint, hipPoint, kneePoint).toFloat()
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

                heelAngle =
                    Utility.angleBetweenPoints(leftFootPoint, heelPoint, rightFootPoint).toFloat()
                /*    heelAngle=  Utility.calculateAngles(
                          landMarkList[lfiCord].x, landMarkList[lfiCord].y,
                          landMarkList[0].x, landMarkList[0].y,
                          landMarkList[rfICord].x, landMarkList[rfICord].y
                      )*/
                xHip = landMarkList[cordHip].x
                yHip = landMarkList[cordHip].y
                xKnee = landMarkList[cordKnee].x
                yKnee = landMarkList[cordKnee].y
                shoulderx = landMarkList[cordShoulder].x
                shulderY=landMarkList[cordShoulder].y
                ankleX = landMarkList[cordAnkle].x
                ankleY=landMarkList[cordAnkle].y
                toeX = landMarkList[cordToe].x
                xHeel = (landMarkList[lHeelCord].x + landMarkList[rHeelCord].x) / 2
                yHeel = landMarkList[lHeelCord].y
                xofLeftKnee=landMarkList[25].x
                xofRightKnee=landMarkList[26].x
                xofLeftToe=landMarkList[29].x
                xofRightToe=landMarkList[30].x
                yOfToe=landMarkList[32].y
                yoFShoulder=landMarkList[11].y
                yForNose=landMarkList[0].y
                leftToeX=landMarkList[31].x
                rightToeX=landMarkList[32].x
                xofLeftHip=landMarkList[23].x
                xofRightHip=landMarkList[24].x

            }

            if (_fragmentSquatsBinding != null) {
                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentSquatsBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM,
                    cameraFacing,
                    kneeAngle,
                    hipAngle,
                    heelAngle,
                    xHeel,
                    yHeel,
                    userFaceType,
                    xHip, yHip, xKnee, yKnee, toeX, isTimerCompleted,xofLeftKnee,xofRightKnee,xofLeftToe,xofRightToe,
                        yOfToe,yoFShoulder,yForNose,shoulderx,shulderY,ankleX,ankleY,windowWidth,windowHeight,isPlaying,leftToeX,rightToeX,xofLeftHip,xofRightHip)
                setAdapterData(fragmentSquatsBinding.overlay.errorMessageList)
                fragmentSquatsBinding.overlay.invalidate()
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