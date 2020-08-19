package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_welcome.*

class RegisterActivity : AppCompatActivity() {

    //kalo misalny masukin alamat email yg g valid dia bakal nolak
    private lateinit var mAuth: FirebaseAuth
    //Ini realtime db
    private lateinit var refUsers : DatabaseReference
    //Firebase uid -> bentuknysa bukan db tapi token jd string
    private var firebaseUID : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        setSupportActionBar(toolbar_register)
        supportActionBar!!.title = getString(R.string.txt_register)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_register.setNavigationOnClickListener {
            val intentToWelcome = Intent(this, WelcomeActivity::class.java)
            startActivity(intentToWelcome)
            finish()
        }

        btn_submit_register.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: String = edt_username.text.toString()
        val email : String = edt_email.text.toString()
        val password : String = edt_password.text.toString()
        //Kalo user nggak inputin semua field dia gak akan ngeeksekusi data ke firebase

        if (username == ""){
            Toast.makeText(this, getString(R.string.txt_username_error), Toast.LENGTH_LONG).show()
        }

        if (email == ""){
            Toast.makeText(this, getString(R.string.txt_email_error), Toast.LENGTH_LONG).show()
        }

        if (password == ""){
            Toast.makeText(this, getString(R.string.txt_password_error), Toast.LENGTH_LONG).show()
        } else{
            //Isinya nnt authentication and post data ke firebase
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->
                if (task.isSuccessful){
                    firebaseUID = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUID)
                    // table nya g kyk table di sql, tapi kek pohon bilangan prima yg satu table bisa bercabang
                    // collection map punya key(udh pasti string) sama value(bisa tipe data apa aja) jd pake Any

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseUID
                    userHashMap["username"] = username
                    //ini mksdnya img profile
                    userHashMap["profile"] = ""
                    userHashMap["cover"] = ""
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.toLowerCase()
                    //bisa masukin sosmed apa aja
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["website"] = "https://www.google.com"

                    refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error Message : " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
