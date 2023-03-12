package com.example.rundownssocialmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.Adapter.CommentsAdapter
import com.example.rundownssocialmedia.Model.Comment
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.databinding.ActivityCommentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCommentsBinding
    private var postId : String? = null
    private var publisherId : String? = null
    private var firebaseUser : FirebaseUser? = null
    private var commentAdapter : CommentsAdapter? = null
    private var commentList : MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val linearLayoutManager = LinearLayoutManager(this@CommentsActivity)
        linearLayoutManager.reverseLayout = true
        binding.recyclerViewComments.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList as ArrayList<Comment>)
        binding.recyclerViewComments.adapter = commentAdapter


        userInfo()
        readComments()
        getPostImage()

        binding.postComment.setOnClickListener{
            if(binding.addComment.text.toString() == ""){
                Toast.makeText(this@CommentsActivity,"Please write comment first.",Toast.LENGTH_SHORT).show()
            }else{
                addComment()
            }
        }
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId!!)

        val commentsMap = HashMap <String,Any>()
        commentsMap["comment"] = binding.addComment!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        addNotification()

        binding.addComment!!.text.clear()
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.profileImageComment)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getPostImage(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val image = snapshot.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(binding.postImageComment)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun readComments(){
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId!!)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    commentList!!.clear()

                    for (snapshot in snapshot.children){
                        val comment = snapshot.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(publisherId!!)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "Commented : " + binding.addComment.text.toString()
        notiMap["postid"] = postId!!
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}