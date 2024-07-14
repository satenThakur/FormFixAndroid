package com.fittracker.activity


import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityVideoPlayerBinding
import com.fittracker.utilits.Constants


class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var activityVideoPlayerBinding: ActivityVideoPlayerBinding


    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVideoPlayerBinding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(activityVideoPlayerBinding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = getWindow()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

        }
        activityVideoPlayerBinding.backBtn.setOnClickListener{
            onBackPressed()
        }

  var uriToPlay:Uri
        if(intent.getIntExtra(Constants.FILE_TYPE,0)==0){
            var filename=intent?.getStringExtra(Constants.FILE_NAME);
            var fileToPlay= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/"+ Constants.FOLDER_NAME+"/"+filename+".mp4"
            uriToPlay=Uri.parse("file://$fileToPlay")
        }else{
            var filename=""
            if (intent.getStringExtra(Constants.MESSAGE_TYPE)
                    .equals(getString(R.string.knee_crossing_toes))
            ) {
                filename="kneescrossingtoes"

            } else if (intent.getStringExtra(Constants.MESSAGE_TYPE)
                    .equals(getString(R.string.tuck_hips))
            ) {
                filename="tuck_hips"
            } else if (intent.getStringExtra(Constants.MESSAGE_TYPE)
                    .equals(getString(R.string.externally_rotate_feet))
            ) {
                filename="rotatefeet"
            } else if (intent.getStringExtra(Constants.MESSAGE_TYPE)
                    .equals(getString(R.string.knees_going_inwards))
            ) {
                filename="keeninwards"
            }



            var path = "android.resource://$packageName/raw/$filename"
            uriToPlay=Uri.parse(path)
        }

        if (mediaControls == null) {
            // creating an object of media controller class
            mediaControls = MediaController(this)

            // set the anchor view for the video view
            mediaControls!!.setAnchorView(activityVideoPlayerBinding.videoView)
        }

        // set the media controller for video view
        activityVideoPlayerBinding.videoView!!.setMediaController(mediaControls)

        // set the absolute path of the video file which is going to be played
       /* activityVideoPlayerBinding.videoView!!.setVideoURI(
            Uri.parse("android.resource://"
                + packageName + "/" + R.raw.test))*/
        var video_name="video"
        val path = "android.resource://$packageName/raw/$video_name"
        var rawId = resources.getIdentifier("video.mp4",  "raw", packageName);
        //val path = "android.resource://$packageName/$rawId"



        Log.e("VideoPlayer","path="+path)

        System.out.println("uriToPlay "+uriToPlay);

        activityVideoPlayerBinding.videoView!!.setVideoURI(uriToPlay)



        activityVideoPlayerBinding.videoView!!.requestFocus()

        // starting the video
        activityVideoPlayerBinding.videoView!!.start()

        // display a toast message
        // after the video is completed
        activityVideoPlayerBinding.videoView!!.setOnCompletionListener {
          /*  Toast.makeText(applicationContext, "Video completed",
                Toast.LENGTH_LONG).show()*/
            Log.e("Video","completed")
            true
        }

        // display a toast message if any
        // error occurs while playing the video
        activityVideoPlayerBinding.videoView!!.setOnErrorListener { mp, what, extra ->
            /*Toast.makeText(applicationContext, "An Error Occurred " +
                    "While Playing Video !!!", Toast.LENGTH_LONG).show()*/
            Log.e("Video", "An Error Occurred " +
                    "While Playing Video !!!")
            false
        }
    }

    override fun onBackPressed() {
        Log.e("onBackPressed","called")
        if (activityVideoPlayerBinding.videoView.isPlaying()){
            activityVideoPlayerBinding.videoView.stopPlayback()
        }
        finish()
    }
}