package com.example.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.adapter.ChatAdapter
import com.example.chatapp.model.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
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

    var seenListener: ValueEventListener? = null
    private fun seenMessage(userIdVisit: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //bikin index untuk si snapshotnya
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) &&
                        chat!!.getSender().equals(userIdVisit)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

        })

    }


    private fun sendMessage(senderId: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey

        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference.child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatListReference.child("id").setValue(userIdVisit)
                            }
                            val chatListReceiverRef = FirebaseDatabase.getInstance().reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)

                            chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                    })
                }
            }


        //bikin fcm
        val userReference = FirebaseDatabase.getInstance().reference.child("Users")
            .child(firebaseUser!!.uid)
        userReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (notify){
                    sendNotification(receiverId, user!!.getUsername(), message)
                }
                notify = false
            }

        })
    }


    private fun sendNotification(receiverId: String?, username: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val token : Token? = dataSnapshot.getValue(Token::class.java)
                    val data = Data(firebaseUser!!.uid, R.mipmap.ic_launcher, "$username: $message",
                        "New Message", userIdVisit)

                    //gak muncul token response
//                    val sender = Sender(data!!.token!!.getToken().toString())

                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data!=null && data!!.data!=null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is Uploading, please wait..")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            //upload
            var uploadTask : StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image"
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                progressBar.dismiss()
                                //implementasikan push notif
                                val reference = FirebaseDatabase.getInstance().reference.child("Users")
                                    .child(firebaseUser!!.uid)
                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        //user akan menangkap data dr snapshot
                                        val user = snapshot.getValue(Users::class.java)
                                        if (notify){
                                            sendNotification(userIdVisit, user!!.getUsername(), "sent you an image")
                                        }
                                        notify = false
                                    }

                                })
                            }
                        }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
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
