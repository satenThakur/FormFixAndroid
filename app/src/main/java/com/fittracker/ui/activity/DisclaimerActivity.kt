package com.fittracker.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityDisclaimerBinding
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisclaimerActivity : AppCompatActivity() {
    private lateinit var activityDisclaimerBinding: ActivityDisclaimerBinding
    private val onBoardingViewModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityDisclaimerBinding = ActivityDisclaimerBinding.inflate(layoutInflater)
        setContentView(activityDisclaimerBinding.root)
        activityDisclaimerBinding.btnContinue.setOnClickListener {
            if(activityDisclaimerBinding.checkboxAgree.isChecked) {
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }else{
                Utility.showErrorSnackBar(activityDisclaimerBinding.root,getString(R.string.agree_with_disclaimer))
            }
        }

        activityDisclaimerBinding.checkboxAgree.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                activityDisclaimerBinding.btnContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_button_purple))
            }else{
                activityDisclaimerBinding.btnContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.round_button_disable))

            }
        }




    }
}