package com.example.rundownssocialmedia.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.Adapter.PostAdapter
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.FragmentPostDetailsBinding
import com.example.rundownssocialmedia.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailsFragment : Fragment() {

    private var postAdapter : PostAdapter? = null
    private var postList : MutableList<Post>? = null
    private var postId : String = ""

    private var _binding : FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container,false)
        val view = binding.root

        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (preferences != null){
            postId = preferences.getString("postId","none").toString()
        }

        binding.recyclerViewPostDetail.setHasFixedSize(true)
        binding.recyclerViewPostDetail.layoutManager = LinearLayoutManager(context)
        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>)}
        binding.recyclerViewPostDetail.adapter = postAdapter

        retrievePosts()


        return view
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()

                val post = snapshot.getValue(Post::class.java)

                postList!!.add(post!!)

                postAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}