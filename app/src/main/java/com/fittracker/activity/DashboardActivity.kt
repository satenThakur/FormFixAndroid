package com.fittracker.activity


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import com.google.android.material.navigation.NavigationView
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.cunoraz.tagview.Tag
import com.fittracker.R
import com.fittracker.databinding.ActivityDashboardBinding
import com.fittracker.utilits.ConstantsPushUps.PUSh_UPS
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.EXERCISE_TYPE
import com.fittracker.utilits.ConstantsSquats.PERMISSION_REQ_ID_RECORD_AUDIO
import com.fittracker.utilits.ConstantsSquats.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE
import com.fittracker.utilits.ConstantsSquats.PERMISSION_REQ_POST_NOTIFICATIONS
import com.fittracker.utilits.ConstantsSquats.SELECT_TAG_1
import com.fittracker.utilits.Utility

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var activityMainBinding: ActivityDashboardBinding
    private var selectedTag = -SELECT_TAG_1
    private lateinit var tags: ArrayList<Tag>
    private var isTablet: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)


        setSupportActionBar(activityMainBinding.toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            activityMainBinding.drawerLayout,
            activityMainBinding.toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        activityMainBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        isTablet = Utility.isTablet(this)

        activityMainBinding.btnStartCamera.setOnClickListener {
            if (selectedTag != -1) {
                if (selectedTag == 0 || selectedTag == 1) {
                    moveToCameraActivity(tags[selectedTag].text.toString())
                } else {
                    Utility.onSNACK(
                        activityMainBinding.root,
                        resources.getString(R.string.coming_soon)
                    )
                }

            } else {
                Utility.onSNACK(
                    activityMainBinding.root,
                    resources.getString(R.string.select_exercise)
                )
            }

        }
        activityMainBinding.tagGroup.setOnTagClickListener { _, position ->
            setTags(position)
        }

        setTags(selectedTag)
        /*Grant Storage Permissions */
        requestForStoragePermissions()
        /*Request Other Permissions*/
        requestPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
            ),
            ConstantsSquats.REQUEST_MULTIPLE_PERMISSIONS
        )


    }


    fun moveToCameraActivity(exerciseType: String) {
        var intent: Intent? =null
        if(exerciseType.equals(PUSh_UPS)){
            intent = Intent(this, CamLandscapeActivity::class.java)
        }else{
            intent = Intent(this, CamPortraitActivity::class.java)
        }
        intent.putExtra(EXERCISE_TYPE, exerciseType)
        startActivity(intent)
    }

    private fun setTags(selected: Int) {
        selectedTag = selected
        tags = ArrayList()
        var tag: Tag

        val tagList = resources.getStringArray(R.array.tags)
        for (i in tagList.indices) {
            tag = Tag(tagList[i])
            if (isTablet) {
                tag.radius = ConstantsSquats.tagRadiousTab
                tag.layoutBorderSize = ConstantsSquats.tagBorderTab
                tag.tagTextSize = ConstantsSquats.tagTextTab
            } else {
                tag.radius = ConstantsSquats.tagRadiousPhone
                tag.layoutBorderSize = ConstantsSquats.tagBorderPhone
                tag.tagTextSize = ConstantsSquats.tagTextPhone
            }
            tag.layoutColor = Color.parseColor("#FFFFFF")
            tag.tagTextColor = Color.parseColor("#FF3700B3")
            tag.layoutBorderColor = Color.parseColor("#FF3700B3")
            if (selected == i) {
                tag.layoutColor = Color.parseColor("#8692f7")
                tag.tagTextColor = Color.parseColor("#FFFFFF")
                tag.layoutBorderColor = Color.parseColor("#8692f7")
            }

            tags.add(tag)
        }
        activityMainBinding.tagGroup.addTags(tags)
    }


    private fun requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager())
                return
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Below android 11
            if (ContextCompat.checkSelfPermission(
                    this@DashboardActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
                return

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Notification Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@DashboardActivity,
                    "Notification Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == PERMISSION_REQ_ID_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Audio Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@DashboardActivity,
                    "Audio Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (read && write) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Storage Permissions Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Storage Permissions Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android is 11 (R) or above
                if (Environment.isExternalStorageManager()) {
                    // Manage External Storage Permissions Granted
                    Log.d("TAG", "onActivityResult: Manage External Storage Permissions Granted")
                } else {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Storage Permissions Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Below android 11
            }
        }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {

            }

            R.id.nav_files -> {
                val intent = Intent(this, RecordedVideoListActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
        activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}