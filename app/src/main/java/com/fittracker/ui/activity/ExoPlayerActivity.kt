package com.fittracker.ui.activity

import android.R
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.fittracker.databinding.ActivityExoPlayerBinding
import com.fittracker.utilits.ConstantsSquats


class ExoPlayerActivity : AppCompatActivity() {

    private lateinit var activityExoPlayerBinding: ActivityExoPlayerBinding
    private var player: ExoPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityExoPlayerBinding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(activityExoPlayerBinding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = getWindow()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

        }
        activityExoPlayerBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        activityExoPlayerBinding.playerView?.setPlayer(player)

       var videoUrl="";

        if (intent.getIntExtra(ConstantsSquats.FILE_TYPE, 0) == 0) {
            var filename = intent?.getStringExtra(ConstantsSquats.FILE_NAME);
            var fileToPlay =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    .toString() + "/" + ConstantsSquats.FOLDER_NAME + "/" + filename + ".mp4"
            //uriToPlay = Uri.parse("file://$fileToPlay")
        } else {
            var filename = "bend_the_knees"

            when (intent.getStringExtra(ConstantsSquats.MESSAGE_TYPE)) {
                getString(com.fittracker.R.string.knee_crossing_toes) -> {
                    filename = "knees_crossing_toes"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/knees_crossing_toes.mp4"
                }
                getString(com.fittracker.R.string.tuck_hips) -> {
                    filename = "tuck_hips"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/tuck_hips.mp4"
                }
                getString(com.fittracker.R.string.bend_at_the_knees) ->
                {
                    filename = "bend_the_knees"
                    videoUrl ="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/bend_the_knees.mp4"
                }
                getString(com.fittracker.R.string.knees_going_inwards) -> {
                    filename = "knees_inwards"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/knees_inwards.mp4"
                }
                getString(com.fittracker.R.string.shoulders_not_balanced) -> {
                    filename = "shoulders_not_balanced"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/shoulders_not_balanced.mp4"
                }
                getString(com.fittracker.R.string.heels_not_balanced) ->  {
                    filename = "heels_not_balanced"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/heels_not_balanced.mp4"
                }
                getString(com.fittracker.R.string.externally_rotate_feet) -> {

                    filename = "externally_rotate_feet"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/externally_rotate_feet.mp4"
                }

                getString(com.fittracker.R.string.hips_not_in_centre) -> {
                    filename = "hips_not_centered"
                    videoUrl="https://formfixbucket.s3.us-west-1.amazonaws.com/Videos/hips_not_centered.mp4"
                }


            }


        }


        // Build the MediaItem


          //  "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"
        val uri = Uri.parse(videoUrl)
        val mediaItem = MediaItem.fromUri(uri)

        // Prepare the player with the media item
        player!!.setMediaItem(mediaItem)
        player!!.prepare()
        player!!.playWhenReady = true // Start playing when ready
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.release()
            player = null
        }
    }
    override fun onBackPressed() {
        Log.e("onBackPressed", "called")
        if (activityExoPlayerBinding.playerView.player?.isPlaying!!) {
            activityExoPlayerBinding.playerView.player?.stop()
        }
        finish()
    }
}