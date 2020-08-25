package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.chatapp.fragment.ChatFragment
import com.example.chatapp.fragment.ProfileFragment
import com.example.chatapp.fragment.SearchFragment
import com.example.chatapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var refUsers : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        setSupportActionBar(toolbar_main)
        supportActionBar!!.title=""
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessage = 0
                for (dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue()//chating class
                    //function chat

                }
                if (countUnreadMessage == 0){
                    viewPagerAdapter.addFragment(ChatFragment(), getString(R.string.txt_chat))
                } else {
                    viewPagerAdapter.addFragment(ChatFragment(), "($countUnreadMessage) Chats")
                }
                viewPagerAdapter.addFragment(SearchFragment(), "Search")
                viewPagerAdapter.addFragment(ProfileFragment(), "Profile")
                view_pager_main.adapter = viewPagerAdapter
                tab_layout_main.setupWithViewPager(view_pager_main)
            }

        })

        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    tv_username_main.text = user!!.getUsername()
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_profile).into(iv_image_main)
                }
            }

        })
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager){
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()
        }
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_logout ->{
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }

    private fun updateStatus(status: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        val hash = HashMap<String, Any>()
        hash["status"] = status
        ref!!.updateChildren(hash)
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }
}

