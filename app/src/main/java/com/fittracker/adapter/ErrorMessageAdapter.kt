package com.fittracker.adapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fittracker.R
import com.fittracker.activity.VideoPlayerActivity
import com.fittracker.model.ErrorMessage
import com.fittracker.utilits.Constants

class ErrorMessageAdapter (private val itemList: List<ErrorMessage>, private var context: Context) :
    RecyclerView.Adapter<ErrorMessageAdapter.ModelViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ErrorMessageAdapter.ModelViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_message, parent, false)
        return ModelViewHolder(v)
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: ErrorMessageAdapter.ModelViewHolder, position: Int) {
      holder.tvErrorMessage.setText(itemList[position].message)
        holder.tvCount.setText(""+itemList[position].count)
        holder.cardview.setOnClickListener{
            Log.e("cardviewError","Cclicked")
            var intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra(Constants.FILE_NAME, "hipcorrection")
            intent.putExtra(Constants.FILE_TYPE,itemList[position].messageType)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardview: CardView
        var tvErrorMessage: TextView
        var tvCount: TextView



        init {
            cardview = itemView.findViewById(R.id.errormessage_row) as CardView
            tvErrorMessage=itemView.findViewById(R.id.tv_message) as TextView
            tvCount = itemView.findViewById(R.id.tv_count) as TextView


        }

    }


}




