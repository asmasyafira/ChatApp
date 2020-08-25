package com.example.chatapp.fragment


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.chatapp.R
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var user: List<Users>? = null
    private var edtSearch: EditText? = null
    private var rvSearch: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        rvSearch = view.findViewById(R.id.rv_search)
        rvSearch!!.setHasFixedSize(true)
        rvSearch!!.layoutManager = LinearLayoutManager(context)

        user = ArrayList()
        getAllUser()

        edtSearch = view.findViewById(R.id.edt_search)
        edtSearch!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            //ketika diubah, karena lg ngesearch
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUser(s.toString().toLowerCase())
            }

        })
        return view
    }

    private fun searchForUser(str: String) {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        //u ngefilter sesuai yg kita cari -> request kita
        val queryUser = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("search").startAt(str)
            .endAt(str + "\uf8ff") //->ini port stringny

        //respon dr requestny
        queryUser.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshots: DataSnapshot) {
                (user as ArrayList<Users>).clear()
                for (snapshot in snapshots.children){
                    val users: Users? = snapshot.getValue(Users::class.java)
                    if ((users!!.getUID()).equals(firebaseUserID)){
                        (user as ArrayList<Users>).add(users)
                    }
                }
                userAdapter = UserAdapter(context!!, user!!, false)
                rvSearch!!.adapter = userAdapter
            }

        })
    }

    private fun getAllUser() {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid //untuk mengetahui aku itu login pake akun siapa
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users") //untuk ngeget table Users beserta isinya

        refUsers.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshots: DataSnapshot) {
                (user as ArrayList<Users>).clear()
                if (edtSearch!!.text.toString() == "")
                    for (snapshot in snapshots.children){
                        val users : Users? = snapshot.getValue(Users::class.java)
                        if (!(users!!.getUID()).equals(firebaseUserID)){
                            (user as ArrayList<Users>).add(users)
                        }
                    }
                userAdapter = UserAdapter(context!!, user!!, false)
                rvSearch!!.adapter = userAdapter
            }

        })
    }


}
