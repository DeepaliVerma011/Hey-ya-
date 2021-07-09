package com.DeepaliVerma.heyya

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item.view.*

class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(item: Inbox, onClick:(name:String, photo:String, id:String)->Unit)=
        with(itemView){
            countTv.isVisible = item.count > 0
            countTv.text = item.count.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                timeTv.text=item.time.formatAsListItem(context)
            }

            titleTv.text = item.name
            subTitleTv.text = item.msg
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            setOnClickListener {
                onClick.invoke(item.name, item.image, item.from)
            }
        }
}