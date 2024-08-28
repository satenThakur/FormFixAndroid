package com.fittracker.activity

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable.Orientation
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.fittracker.R
import com.fittracker.application.FormfitApplication
import com.fittracker.database.MediaData
import com.fittracker.databinding.ActivityCamLandscapeBinding
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.FormFixSharedPreferences
import com.fittracker.utilits.Utility
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderCodecInfo
import com.hbisoft.hbrecorder.HBRecorderListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class CamLandscapeActivity : AppCompatActivity() , HBRecorderListener {
    private lateinit var activityCamLandscapeBinding: ActivityCamLandscapeBinding
    /*Screen Recording Variables*/
    private lateinit var hbRecorder: HBRecorder
    private var resolver: ContentResolver? = null
    private var contentValues: ContentValues? = null
    private var mUri: Uri? = null
    private var hasPermissions = false
    private var wasHDSelected = true
    private var isAudioEnabled = true
     var exerciseType="";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCamLandscapeBinding = ActivityCamLandscapeBinding.inflate(layoutInflater)
        setContentView(activityCamLandscapeBinding.root)
        exerciseType= intent?.getStringExtra(ConstantsSquats.EXERCISE_TYPE).toString()
        val navHostFragment = supportFragmentManager.findFragmentById(com.fittracker.R.id.fragment_container) as NavHostFragment
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Init HBRecorder
            hbRecorder = HBRecorder(this, this)
            //When the user returns to the application, some UI changes might be necessary,
            //check if recording is in progress and make changes accordingly
            if (hbRecorder.isBusyRecording) {
                activityCamLandscapeBinding.btnRecord.setImageDrawable(resources.getDrawable(R.drawable.iv_record))
            }
            hbRecorder.setOrientationHint(90)
        }

        // Examples of how to use the HBRecorderCodecInfo class to get codec info

        // Examples of how to use the HBRecorderCodecInfo class to get codec info
        val hbRecorderCodecInfo = HBRecorderCodecInfo()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mWidth = hbRecorder.defaultWidth
            val mHeight = hbRecorder.defaultHeight
            val mMimeType = "video/avc"
            val mFPS = 30
            if (hbRecorderCodecInfo.isMimeTypeSupported(mMimeType)) {
                val defaultVideoEncoder = hbRecorderCodecInfo.getDefaultVideoEncoderName(mMimeType)
                val isSizeAndFramerateSupported = hbRecorderCodecInfo.isSizeAndFramerateSupported(
                    mWidth,
                    mHeight,
                    mFPS,
                    mMimeType,
                    Configuration.ORIENTATION_PORTRAIT
                )


                val supportedVideoMimeTypes: Map<String, String> =
                    hbRecorderCodecInfo.getSupportedVideoMimeTypes()
                supportedVideoMimeTypes.forEach { (key, value) ->
                    Log.e(
                        "HBRecorderCodecInfo",
                        "Supported VIDEO encoders and mime types : $key -> $value"
                    )
                }


                val supportedAudioMimeTypes = hbRecorderCodecInfo.supportedAudioMimeTypes
                supportedAudioMimeTypes.forEach { (key, value) ->
                    Log.e(
                        "HBRecorderCodecInfo",
                        "Supported AUDIO encoders and mime types : $key -> $value"
                    )
                }
                val supportedVideoFormats = hbRecorderCodecInfo.supportedVideoFormats
                supportedVideoFormats.forEach { format ->
                    Log.e("HBRecorderCodecInfo", "Available Video Formats : $format")
                }

            } else {
                Log.e("HBRecorderCodecInfo", "MimeType not supported")
            }
        }
        activityCamLandscapeBinding.btnRecord.bringToFront()
        activityCamLandscapeBinding.btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@CamLandscapeActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                hasPermissions=true;
            }
            if (hasPermissions) {
                if (hbRecorder.isBusyRecording) {
                    hbRecorder.stopScreenRecording()
                    activityCamLandscapeBinding.btnRecord.setImageDrawable(resources.getDrawable(R.drawable.iv_start_record))
                    //activityCameraBinding.start.text = getString(R.string.start_recording)
                } else {
                    startRecordingScreen()
                }
            } else {
                Toast.makeText(applicationContext, "Please Grant Required permissions", Toast.LENGTH_LONG).show();

            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun startRecordingScreen() {

        //WHEN SETTING CUSTOM SETTINGS YOU MUST SET THIS!!!
        hbRecorder.enableCustomSettings()
        customSettings()
       /* if(FormFixSharedPreferences.getSharedPrefBooleanValue(this@CameraActivity,Constants.PREF_KEY_VIDEO_RECORDER_PERMISSION)){
            startRecordingIntent()
        }else {*/
            val mediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val permissionIntent = mediaProjectionManager?.createScreenCaptureIntent()
            startActivityForResult(permissionIntent!!, ConstantsSquats.SCREEN_RECORD_REQUEST_CODE)
     //   }
    }

/*    private fun startRecordingIntent(){
        //Set file path or Uri depending on SDK version
        setOutputPath()
        //Start screen recording
        hbRecorder.startScreenRecording(data, resultCode)
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == ConstantsSquats.SCREEN_RECORD_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    //Set file path or Uri depending on SDK version
                    setOutputPath()
                    //Start screen recording
                    hbRecorder.startScreenRecording(data, resultCode)
                    FormFixSharedPreferences.saveSharedPreferencesValue(this@CamLandscapeActivity,
                        ConstantsSquats.PREF_KEY_VIDEO_RECORDER_PERMISSION,true)
                }
            }
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) // Example of how to set custom settings
    private fun customSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        //Is audio enabled
        val audio_enabled = prefs.getBoolean("key_record_audio", true)
        hbRecorder.isAudioEnabled(audio_enabled)

        //Audio Source
        val audio_source = prefs.getString("key_audio_source", null)
        if (audio_source != null) {
            when (audio_source) {
                "0" -> hbRecorder.setAudioSource("DEFAULT")
                "1" -> hbRecorder.setAudioSource("CAMCODER")
                "2" -> hbRecorder.setAudioSource("MIC")
            }
        }

        //Video Encoder
        val video_encoder = prefs.getString("key_video_encoder", null)
        if (video_encoder != null) {
            when (video_encoder) {
                "0" -> hbRecorder.setVideoEncoder("DEFAULT")
                "1" -> hbRecorder.setVideoEncoder("H264")
                "2" -> hbRecorder.setVideoEncoder("H263")
                "3" -> hbRecorder.setVideoEncoder("HEVC")
                "4" -> hbRecorder.setVideoEncoder("MPEG_4_SP")
                "5" -> hbRecorder.setVideoEncoder("VP8")
            }
        }

        //NOTE - THIS MIGHT NOT BE SUPPORTED SIZES FOR YOUR DEVICE
        //Video Dimensions
        val video_resolution = prefs.getString("key_video_resolution", null)
        if (video_resolution != null) {
            when (video_resolution) {
                "0" -> hbRecorder.setScreenDimensions(426, 240)
                "1" -> hbRecorder.setScreenDimensions(640, 360)
                "2" -> hbRecorder.setScreenDimensions(854, 480)
                "3" -> hbRecorder.setScreenDimensions(1280, 720)
                "4" -> hbRecorder.setScreenDimensions(1920, 1080)
            }
        }

        //Video Frame Rate
        val video_frame_rate = prefs.getString("key_video_fps", null)
        if (video_frame_rate != null) {
            when (video_frame_rate) {
                "0" -> hbRecorder.setVideoFrameRate(60)
                "1" -> hbRecorder.setVideoFrameRate(50)
                "2" -> hbRecorder.setVideoFrameRate(48)
                "3" -> hbRecorder.setVideoFrameRate(30)
                "4" -> hbRecorder.setVideoFrameRate(25)
                "5" -> hbRecorder.setVideoFrameRate(24)
            }
        }

        //Video Bitrate
        val video_bit_rate = prefs.getString("key_video_bitrate", null)
        if (video_bit_rate != null) {
            when (video_bit_rate) {
                "1" -> hbRecorder.setVideoBitrate(12000000)
                "2" -> hbRecorder.setVideoBitrate(8000000)
                "3" -> hbRecorder.setVideoBitrate(7500000)
                "4" -> hbRecorder.setVideoBitrate(5000000)
                "5" -> hbRecorder.setVideoBitrate(4000000)
                "6" -> hbRecorder.setVideoBitrate(2500000)
                "7" -> hbRecorder.setVideoBitrate(1500000)
                "8" -> hbRecorder.setVideoBitrate(1000000)
            }
        }

        //Output Format
        val output_format = prefs.getString("key_output_format", null)
        if (output_format != null) {
            when (output_format) {
                "0" -> hbRecorder.setOutputFormat("DEFAULT")
                "1" -> hbRecorder.setOutputFormat("MPEG_4")
                "2" -> hbRecorder.setOutputFormat("THREE_GPP")
                "3" -> hbRecorder.setOutputFormat("WEBM")
            }
        }
    }

    //Get/Set the selected settings
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun quickSettings() {
        hbRecorder.setAudioBitrate(128000)
        hbRecorder.setAudioSamplingRate(44100)
        hbRecorder.recordHDVideo(wasHDSelected)
        hbRecorder.isAudioEnabled(isAudioEnabled)
        //Customise Notification
        hbRecorder.setNotificationSmallIcon(R.drawable.ic_cameraswitch)
        //hbRecorder.setNotificationSmallIconVector(R.drawable.ic_baseline_videocam_24);
        hbRecorder.setNotificationTitle(getString(R.string.stop_recording_notification_title))
        hbRecorder.setNotificationDescription(getString(R.string.stop_recording_notification_message))
    }
    override fun onBackPressed() {
        if (hbRecorder.isBusyRecording()){
            hbRecorder.stopScreenRecording()
        }
        finish()
    }


    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun setOutputPath() {
        activityCamLandscapeBinding.btnRecord.setImageDrawable(resources.getDrawable(R.drawable.iv_record))
        val filename: String = Utility.generateFileName()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = contentResolver
            contentValues = ContentValues()
            contentValues!!.put(MediaStore.Video.Media.RELATIVE_PATH, "${ConstantsSquats.FOLDER_DIRECTORY}/${ConstantsSquats.FOLDER_NAME}")
            contentValues!!.put(MediaStore.Video.Media.TITLE, filename)
            contentValues!!.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            contentValues!!.put(MediaStore.MediaColumns.MIME_TYPE, ConstantsSquats.VIDEO_TYPE)
            mUri = resolver?.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            //FILE NAME SHOULD BE THE SAME
            hbRecorder.fileName = filename
            Log.e("OUTPUT_FILE","mUri="+mUri)
            Log.e("OUTPUT_FILE","filename="+filename)
            val mediaData = MediaData(exerciseType=exerciseType, uri=""+mUri,filename=filename,
             date=Utility.getCurrentDate(), time=Utility.getCurrentTime())
            GlobalScope.launch (Dispatchers.IO) {
                FormfitApplication.database.userDao().insertMediaData(mediaData)
            }

            hbRecorder.setOutputUri(mUri)
        } else {
            createFolder()
            hbRecorder.setOutputPath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    .toString() + "/"+ ConstantsSquats.FOLDER_NAME
            )
        }
    }



    private fun createFolder() {
        val f1 = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            ConstantsSquats.FOLDER_NAME
        )
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created")
            }
        }
    }

    override fun HBRecorderOnStart() {
        Log.e("HBRECORDER","HBRecorderOnStart")
    }

    override fun HBRecorderOnComplete() {
        Log.e("HBRECORDER","HBRecorderOnComplete")
    }

    override fun HBRecorderOnError(errorCode: Int, reason: String?) {
        Log.e("HBRECORDER","HBRecorderOnError")
    }

    override fun HBRecorderOnPause() {
        Log.e("HBRECORDER","HBRecorderOnPause")
    }

    override fun HBRecorderOnResume() {
        Log.e("HBRECORDER","HBRecorderOnResume")
    }

}
