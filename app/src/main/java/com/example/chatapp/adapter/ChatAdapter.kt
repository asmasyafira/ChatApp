package com.example.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.FullViewImageActivity
import com.example.chatapp.R
import com.example.chatapp.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.left_chat_item.view.*

class ChatAdapter(mContext: Context, mChatList: List<Chat>, imageUrl: String) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder?>() {

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]
        Picasso.get().load(imageUrl).into(holder.profile_image)

        //untuk pesan yang berupa image
        if (chat.getMessage()!!.equals(R.string.sent_message) &&
                !chat.getUrl().equals(""))

            //imagenya by right side
            if (chat.getSender().equals(firebaseUser!!.uid)){
                holder.show_text_message!!.visibility = View.GONE
                holder.right_image_view!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.right_image_view)

                holder.right_image_view!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )

                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle(mContext.getString(R.string.what_do_you_want))
                    builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                        if (which == 0){
                            val intent = Intent(mContext, FullViewImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        } else if (which == 1){
                            deleteSentMessage(position, holder)
                        }
                    })
                }
            }
    }

    private fun deleteSentMessage(position: Int, holder: ViewHolder) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).getMessageId()!!)
            .removeValue()
            .addOnCompleteListener {
                task ->
                if (task.isSuccessful){
                    Toast.makeText(holder.itemView.context, mContext.getString(R.string.text_delete), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(holder.itemView.context, mContext.getString(R.string.failed), Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1){
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.right_chat_item, parent, false)
            ViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.left_chat_item, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: CircleImageView? = null
        var show_text_message: TextView? = null
        var left_image_view: ImageView? = null
        var text_seen: TextView? = null
        var right_image_view: ImageView? = null

        init {
            profile_image = itemView.findViewById(R.id.iv_profile_left_chat)
            show_text_message = itemView.findViewById(R.id.tv_text_message_left)
            left_image_view = itemView.findViewById(R.id.iv_left_message)
            right_image_view = itemView.findViewById(R.id.iv_right_message)
            text_seen = itemView.findViewById(R.id.tv_last_seen_left)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid)) {
            1
        } else {
            0
        }
    }
}