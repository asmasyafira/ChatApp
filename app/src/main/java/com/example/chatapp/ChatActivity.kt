package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.adapter.ChatAdapter
import com.example.chatapp.model.Chat
import com.example.chatapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatAdapter: ChatAdapter? = null
    lateinit var recycler_view: RecyclerView
    var chatList: List<Chat>? = null
    var reference: DatabaseReference? = null

    var notify = false
    var api: ApiService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(toolbar_chat)
        supportActionBar!!.title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_chat.setNavigationOnClickListener {
            finish()
        }

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view = findViewById(R.id.rv_chat_message)
        recycler_view.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd =
            true //stackfrmEnd buat nentuin itu posisi chat sebelah mana

        recycler_view.layoutManager = linearLayoutManager
        reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                tv_username_chat.text = user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(iv_profile_chat)
                retrieveMessage(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

        })

        iv_send_message_chat.setOnClickListener {
            notify = true
            val message = edt_message_chat.text.toString()
            if (message == "") {
                Toast.makeText(this, getString(R.string.message_notif), Toast.LENGTH_LONG).show()
            } else {
                sendMessage(firebaseUser!!.uid, userIdVisit, message)
            }
            edt_message_chat.setText("")
        }
        iv_attach_file_chat.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Pick Image"
                ), 438
            )
        }
        seenMessage(userIdVisit)

    }

    private fun seenMessage(userIdVisit: String?) {

    }

    private fun sendMessage(uid: String, userIdVisit: String?, message: String) {

    }

    private fun retrieveMessage(senderId: String, receiverId: String?, profile: String?) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshots: DataSnapshot) {
                (chatList as ArrayList<Chat>).clear()
                for (snapshot in snapshots.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId) ||
                        chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)
                    )
                        (chatList as ArrayList<Chat>).add(chat)

                }
                chatAdapter =
                    ChatAdapter(this@ChatActivity, (chatList as ArrayList<Chat>), profile!!)
                recycler_view.adapter = chatAdapter
            }

        })
    }
}
