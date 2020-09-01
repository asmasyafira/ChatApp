package com.example.chatapp.fragment


import android.media.session.MediaSession
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.chatapp.R
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.model.Chat
import com.example.chatapp.model.ChatList
import com.example.chatapp.model.Token
import com.example.chatapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId

class ChatFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUser: List<Users>? = null
    private var usersChatlist: List<ChatList>? = null
    lateinit var recyclerView: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.rv_chat_fragment)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatlist = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("ChatList")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatlist as ArrayList).clear()
                for (dataSnapshot in snapshot.children) {
                    val chatlist = dataSnapshot.getValue(ChatList::class.java)
                    (usersChatlist as ArrayList).add(chatlist!!)
                }
                retrieveDataChatList()
            }

        })
        updateToken(FirebaseInstanceId.getInstance().token)
        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val newToken = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(newToken)
        //setiap user punya token yg berbeda2
    }

    private fun retrieveDataChatList() {
        mUser = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUser as ArrayList).clear()

                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(Users::class.java)
                    //setiap chat list punya id user yg berbeda2
                    for (eachChatList in usersChatlist!!) {
                        if (user!!.getUID().equals(eachChatList.getId())) {
                            (mUser as ArrayList).add(user!!)
                        }
                    }
                    userAdapter = UserAdapter(context!!, (mUser as ArrayList<Users>), true)
                    recyclerView.adapter = userAdapter
                }
            }

        })
    }


}
