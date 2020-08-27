package com.example.chatapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_profile.*

class VisitProfileActivity : AppCompatActivity() {
    private var userVisitId: String = ""
    var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_profile)

        userVisitId = intent.getStringExtra("visit_id")
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(Users::class.java)

                    tv_username_visit_profile.text = user!!.getUsername()
                    Picasso.get().load(user!!.getProfile()).into(iv_profile_visit)
                    Picasso.get().load(user!!.getCover()).into(iv_cover_visit_profile)
                }
            }

        })

        iv_fb_visit_profile.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook()) //uri parse -> mau ngeparse data nya
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        iv_cover_visit_profile.setOnClickListener {
            val uri = Uri.parse(user!!.getCover())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        iv_website_visit_profile.setOnClickListener {
            val uri = Uri.parse(user!!.getWebsite())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        iv_ig_visit_profile.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        btn_send_message_visit_profile.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("visit_id", user!!.getUID())
            startActivity(intent)
        }
    }
}

