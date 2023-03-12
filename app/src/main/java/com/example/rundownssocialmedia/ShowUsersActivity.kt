package com.example.rundownssocialmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.Adapter.UserAdapter
import com.example.rundownssocialmedia.databinding.ActivityShowUsersBinding
import com.firebase.ui.auth.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {
    private lateinit var binding : ActivityShowUsersBinding

    var id : String = ""
    var title : String = ""
    var userAdapter : UserAdapter? = null
    var userList : List<com.example.rundownssocialmedia.Model.User>? = null
    var idList : List<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<com.example.rundownssocialmedia.Model.User>, false)
        binding.recyclerView.adapter = userAdapter

        idList = ArrayList()

        when(title){
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story").child(id)
            .child(intent.getStringExtra("storyid")!!).child("views")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(snapshot in snapshot.children){
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(snapshot in snapshot.children){
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getFollowing() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(snapshot in snapshot.children){
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(id!!)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    (idList as ArrayList<String>).clear()

                    for(snapshot in snapshot.children){
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                (userList as ArrayList<com.example.rundownssocialmedia.Model.User>).clear()

                for (snapshot in dataSnapshot.children){

                    val user = snapshot.getValue(com.example.rundownssocialmedia.Model.User::class.java)
                    for (id in idList!!){
                        if (user!!.getUID() == id){
                            (userList as ArrayList<com.example.rundownssocialmedia.Model.User>).add(user)
                        }
                    }

                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}















