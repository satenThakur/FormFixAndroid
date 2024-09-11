package com.fittracker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fittracker.R
import com.fittracker.activity.VideoPlayerActivity
import com.fittracker.application.FormfitApplication
import com.fittracker.database.MediaData
import com.fittracker.interfaces.MediaItemListner
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.FILE_NAME
import com.fittracker.utilits.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MediaAdapter (private val itemList: List<MediaData>, private var context: Context,private  var mediaItemListner: MediaItemListner) :
    RecyclerView.Adapter<MediaAdapter.ModelViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaAdapter.ModelViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_media, parent, false)
        return ModelViewHolder(v)
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: MediaAdapter.ModelViewHolder, position: Int) {
        holder.tvExercise.setText(itemList[position].exerciseType)
        holder.tvTime.setText(itemList[position].time)
        holder.tvDate.setText(itemList[position].date)
        holder.ConstraintLayt.setOnClickListener{
            if(Utility.checkIfFileExist(itemList[position].filename)) {
                var intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra(FILE_NAME, itemList[position].filename)
                intent.putExtra(ConstantsSquats.FILE_TYPE,0)
                context.startActivity(intent)
            }else{
                Utility.showErrorSnackBar(holder.itemView,context.resources.getString(R.string.file_not_exist))
                GlobalScope.launch (Dispatchers.IO) {
                    FormfitApplication.database.userDao().deletebyId(itemList[position].id)
                    mediaItemListner.onItemDeleted(position)
                }


                Utility.deleteMediaFile(itemList[position].filename)
            }
        }
        holder.btnDetele.setOnClickListener{
           GlobalScope.launch (Dispatchers.IO) {
               FormfitApplication.database.userDao().deletebyId(itemList[position].id)
               mediaItemListner.onItemDeleted(position)
           }

            Utility.deleteMediaFile(itemList[position].filename)

        }
        holder.btnShare.setOnClickListener{
            try{
          var myFilePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/"+ ConstantsSquats.FOLDER_NAME+"/"+itemList[position].filename+".mp4"
                val intent = Intent(ACTION_SEND)

                // putting uri of image to be shared

                // putting uri of image to be shared
                intent.putExtra(EXTRA_STREAM, Uri.parse(myFilePath))

                // adding text to share

                // adding text to share
                intent.putExtra(EXTRA_TEXT, "Shared By FormFIx")

                // Add subject Here

                // Add subject Here
                intent.putExtra(EXTRA_SUBJECT, "Subject Here")

                // setting type to image

                // setting type to image
                intent.type = "image/png"

                // calling startactivity() to share

                // calling startactivity() to share
                context.startActivity(createChooser(intent, "Share Via"))


            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ConstraintLayt: CardView
        var ItemImage: ImageView
        var btnDetele: ImageView
        var btnShare:ImageView
        var tvExercise: TextView
        var tvDate: TextView
        var tvTime: TextView


        init {
            ConstraintLayt = itemView.findViewById(R.id.errormessage_row) as CardView
            ItemImage = itemView.findViewById(R.id.video) as ImageView
            btnDetele= itemView.findViewById(R.id.btn_delete) as ImageView
            btnShare=itemView.findViewById(R.id.btn_share)
            tvExercise=itemView.findViewById(R.id.tv_exercise) as TextView
            tvDate = itemView.findViewById(R.id.tv_date) as TextView
            tvTime = itemView.findViewById(R.id.tv_time) as TextView


        }

    }


}




