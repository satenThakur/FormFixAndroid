package com.fittracker.ui.activity


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.fittracker.ui.adapter.MediaAdapter
import com.fittracker.application.FormfitApplication
import com.fittracker.database.MediaData
import com.fittracker.databinding.ActivityVideoListBinding
import com.fittracker.interfaces.MediaItemListner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RecordedVideoListActivity : AppCompatActivity(), MediaItemListner {
    private lateinit var activityVideoListBinding: ActivityVideoListBinding

    private lateinit var allmedia: ArrayList<MediaData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVideoListBinding = ActivityVideoListBinding.inflate(layoutInflater)
        setContentView(activityVideoListBinding.root)

        activityVideoListBinding.recyclerviewMedia.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
        GlobalScope.launch (Dispatchers.IO) {
            allmedia = FormfitApplication.database.userDao().getAllMediaData() as ArrayList<MediaData>
            setAdapterData()
        }

        activityVideoListBinding.backBtn.setOnClickListener {
            finish()
        }

    }

private fun setAdapterData() {
    runOnUiThread(Runnable {
        if (allmedia.isNotEmpty()) {
            activityVideoListBinding.lblNoDataFound.visibility = View.GONE
            val adapter = MediaAdapter(allmedia, this@RecordedVideoListActivity, this)
            activityVideoListBinding.recyclerviewMedia.adapter = adapter
        } else {
            activityVideoListBinding.lblNoDataFound.visibility = View.VISIBLE
        }
        })
}
    override fun onItemDeleted(position:Int) {
        allmedia.removeAt(position)
        setAdapterData()

    }
}

