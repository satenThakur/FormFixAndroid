package com.fittracker.activity


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.R.*
import com.fittracker.databinding.ActivityImageBinding
import com.fittracker.utilits.ConstantsSquats


class ImageActivity : AppCompatActivity() {
    private lateinit var activityImageBinding: ActivityImageBinding


    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityImageBinding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(activityImageBinding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = getWindow()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

        }
        activityImageBinding.backBtn.setOnClickListener{
            finish()
        }
        if (intent.getStringExtra(ConstantsSquats.MESSAGE_TYPE).equals(getString(string.knee_crossing_toes))) {

            activityImageBinding.imageHolder.setImageResource(R.drawable.img_knees_crossing_toes)

        } else if (intent.getStringExtra(ConstantsSquats.MESSAGE_TYPE).equals(getString(string.tuck_hips))) {

            activityImageBinding.imageHolder.setImageResource(R.drawable.img_hip_correction_img)

        } else if (intent.getStringExtra(ConstantsSquats.MESSAGE_TYPE).equals(getString(string.externally_rotate_feet))) {

            activityImageBinding.imageHolder.setImageResource(R.drawable.img_rotate_feet_externally)

        } else if (intent.getStringExtra(ConstantsSquats.MESSAGE_TYPE).equals(getString(string.knees_going_inwards))) {

            activityImageBinding.imageHolder.setImageResource(R.drawable.img_rotate_feet_externally)
        }


    }

    override fun onBackPressed() {
        Log.e("onBackPressed", "called")
        finish()
    }
}