package com.example.chatapp.fragment


import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.chatapp.R
import com.example.chatapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*


class ProfileFragment : Fragment() {

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private var RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userReference = FirebaseDatabase.getInstance().reference.child("Users")
            .child(firebaseUser!!.uid)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        storageRef = FirebaseStorage.getInstance().reference
            .child("User Images")
        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (context != null) {
                        view.tv_username_profile_setting.text = user!!.getUsername()
                        Picasso.get().load(user.getProfile()).into(view.iv_profile_setting)
                        Picasso.get().load(user.getCover()).into(view.iv_cover_profile_setting)
                    }
                }
            }

        })

        //untuk mengganti cover + foto profile
        view.iv_profile_setting.setOnClickListener { pickImage() }
        view.iv_cover_profile_setting.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }
        return view
    }

    private fun pickImage() {

    }


}
