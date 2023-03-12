package com.example.rundownssocialmedia.Fragments

import android.os.Bundle
import android.renderscript.Sampler.Value
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.Adapter.PostAdapter
import com.example.rundownssocialmedia.Adapter.StoryAdapter
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.Model.Story
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.FragmentHomeBinding
import com.example.rundownssocialmedia.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var postAdapter : PostAdapter? = null
    private var postList : MutableList<Post>? = null
    private var followingList : MutableList<String>? = null
    private var storyAdapter : StoryAdapter? = null
    private var storyList : MutableList<Story>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container,false)
        val view = binding.root

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.recyclerViewHome.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>)}
        binding.recyclerViewHome.adapter = postAdapter



        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewStory.layoutManager = linearLayoutManager2
        binding.recyclerViewHome.setHasFixedSize(true)

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it,storyList as ArrayList<Story>) }
        binding.recyclerViewStory.adapter = storyAdapter

        checkFollowings()

        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (followingList as ArrayList<String>).clear()
                    for (snapshot in snapshot.children){
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                    retrieveStories()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        storyRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()


                (storyList as ArrayList<Story>).add(Story("",0,0,"",FirebaseAuth.getInstance().currentUser!!.uid))

                for (id in followingList!!){
                    var countStory = 0

                    var story : Story? = null
                    for (snapshot in dataSnapshot.child(id).children){
                        story = snapshot.getValue(Story::class.java)

                        if (timeCurrent - 100 >=  story!!.getTimeStart() && timeCurrent + 100 <= story.getTimeEnd()){
                            countStory++
                        } else if (timeCurrent > story.getTimeEnd()){
                            val expiredRef = FirebaseDatabase.getInstance().reference.child("Story").child(story.getUserId())
                                .child(story.getStoryId())
                            expiredRef.removeValue()
                        }
                    }
                    if (countStory>0){
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()

                for (snapshot in snapshot.children){
                    val post = snapshot.getValue(Post::class.java)

                    for (id in (followingList as ArrayList<String>) ){
                        if (post!!.getPublisher() == id){
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}