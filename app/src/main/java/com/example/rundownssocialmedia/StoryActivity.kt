package com.example.rundownssocialmedia


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.rundownssocialmedia.Fragments.ProfileFragment
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.Model.Story
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.databinding.ActivityStoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView


class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    private lateinit var binding : ActivityStoryBinding
    var currentUserId : String = ""
    var userId : String = ""
    var counter  : Int = 0
    var imagesList : List<String>? = null
    var storyIdsList : List<String>? = null
    var pressTime = 0L
    var limit = 500L


    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { v, event ->
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                binding.storiesProgress.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                binding.storiesProgress.pause()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        userId = intent.getStringExtra("userId")!!

        binding.layoutSeen.visibility = View.GONE
        binding.storyDelete.visibility = View.GONE

        if (userId == currentUserId){
            binding.layoutSeen.visibility = View.VISIBLE
            binding.storyDelete.visibility = View.VISIBLE
        }
        getStories(userId)
        userInfo(userId)

        binding.reverse.setOnClickListener { binding.storiesProgress.reverse() }
        binding.reverse.setOnTouchListener(onTouchListener)

        binding.skip.setOnClickListener { binding.storiesProgress.skip() }
        binding.skip.setOnTouchListener(onTouchListener)


        binding.layoutSeen.setOnClickListener {
            val intent = Intent(this@StoryActivity,ShowUsersActivity::class.java)
            intent.putExtra("id",userId)
            intent.putExtra("storyid", storyIdsList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }
        binding.storyDelete.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId).child(storyIdsList!![counter])

            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this@StoryActivity,"Deleted...",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getStories(userId: String){
            imagesList = ArrayList()
            storyIdsList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)


        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (imagesList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for (snapshot in dataSnapshot.children){
                    val story : Story? = snapshot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()){
                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryId())
                    }
                    binding.storiesProgress.setStoriesCount((imagesList as ArrayList<String>).size)

                    binding.storiesProgress.setStoryDuration(6000L)

                    binding.storiesProgress.setStoriesListener(this@StoryActivity)

                    binding.storiesProgress.startStories(counter)

                    Picasso.get().load(imagesList!![counter]).placeholder(R.drawable.profile).into(binding.imageStory)

                    addViewToStory(storyIdsList!![counter])
                    seenNumber(storyIdsList!![counter])

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun userInfo(userId : String){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.storyProfileImage)
                    binding.storyUsername.text = user.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun addViewToStory(storyId: String){
        val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)
            .child(storyId)
            .child("views")
            .child(currentUserId).setValue(true)
    }

    private fun seenNumber(storyId : String){
        val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId).child(storyId).child("views")

        ref.addValueEventListener(object : ValueEventListener{
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.seenNumber.text = "" + snapshot.childrenCount
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onNext() {
        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile).into(binding.imageStory)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])
    }

    override fun onPrev() {
        if(counter - 1 < 0) return
        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile).into(binding.imageStory)
        seenNumber(storyIdsList!![counter])
    }

    override fun onComplete() {
        finish()
    }

    override fun onDestroy() {
        binding.storiesProgress.destroy()
        super.onDestroy()
    }

    override fun onResume() {
        binding.storiesProgress.resume()
        super.onResume()
    }

    override fun onPause() {
        binding.storiesProgress.pause()
        super.onPause()
    }
}