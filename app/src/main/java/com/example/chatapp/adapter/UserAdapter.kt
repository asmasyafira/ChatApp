package com.example.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.ChatActivity
import com.example.chatapp.R
import com.example.chatapp.VisitProfileActivity
import com.example.chatapp.model.Chat
import com.example.chatapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(mContext: Context, mUser: List<Users>, isChatcheck: Boolean) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val mContext: Context
    private val mUser: List<Users>
    private var isChatcheck: Boolean
    var lastMessage: String = ""

    init {
        this.mUser = mUser
        this.mContext = mContext
        this.isChatcheck = isChatcheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_search, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users = mUser[position]
        holder.username.text = user!!.getUsername()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profile)

        //ini dibuat kalo messagenya udh
        if (isChatcheck) {
            retrieveLastMessage(user.getUID(), holder.lastMessage)
        } else {

        }

        if (isChatcheck) {
            if (user.getStatus() == "online") {
                holder.online.visibility = View.VISIBLE
                holder.offline.visibility = View.GONE
            }
        } else {
            holder.online.visibility = View.GONE
            holder.offline.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>("Sent Message", "Visit Profile")
            val builder : AlertDialog.Builder = AlertDialog.Builder(mContext)
            //opsi ketika si itemaView di klik
            builder.setItems(options, DialogInterface.OnClickListener() { dialog, position ->
                if (position == 0){ //indexnya "Sent Message
                    val intent = Intent(mContext, ChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
                if (position == 1){ //indexnya "Visit Profile"
                    val intent = Intent(mContext, VisitProfileActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }
    }

    private fun retrieveLastMessage(uid: String?, lastMessages: TextView) {
        lastMessage = "defaultMessage"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver() == firebaseUser!!.uid &&
                            chat.getSender() == uid ||
                            chat.getReceiver() == uid &&
                            chat.getSender() == firebaseUser!!.uid
                        ) {
                            lastMessage = chat.getMessage()!!
                        }
                    }
                }
                when (lastMessage) {
                    "defaultMessage" -> lastMessages.text = mContext.getString(R.string.no_message)
                    "sent you an image" -> lastMessages.text = mContext.getString(R.string.sent_message)
                }
                lastMessage = "defaultMessage"
            }

        })
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var profile: CircleImageView
        var online: CircleImageView
        var offline: CircleImageView
        var lastMessage: TextView

        init {
            username = itemView.findViewById(R.id.tv_user_search)
            profile = itemView.findViewById(R.id.iv_profile_search)
            online = itemView.findViewById(R.id.iv_online_search)
            offline = itemView.findViewById(R.id.iv_offline_search)
            lastMessage = itemView.findViewById(R.id.tv_last_message_search)
        }
    }
}