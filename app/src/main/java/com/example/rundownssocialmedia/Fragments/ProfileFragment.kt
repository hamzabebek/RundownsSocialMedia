package com.example.rundownssocialmedia.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.AccountSettingActivity
import com.example.rundownssocialmedia.Adapter.MyImagesAdapter
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.ShowUsersActivity
import com.example.rundownssocialmedia.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {
    private lateinit var profileId:  String
    private lateinit var firebaseUser : FirebaseUser
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    var postList : List<Post>? = null
    var myImagesAdapter : MyImagesAdapter? = null
    var myImagesAdapterSavedImg : MyImagesAdapter? = null
    var postListSaved : List<Post>? = null
    var mySavesImg : List<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container,false)
        val view = binding.root

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null){
            this.profileId = pref.getString("profileId","None").toString()
        }
        if (profileId == firebaseUser.uid){
            binding.editAccountSettingsBtn.text = "Edit Profile"
        }else if (profileId != firebaseUser.uid){
            checkFollowAndFollowingButtonStatus()
        }

        //RecyclerView for Uploaded Pictures
         binding.recyclerViewUploadPic.setHasFixedSize(true)
        val linearLayoutManager : LinearLayoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewUploadPic.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<Post>) }
        binding.recyclerViewUploadPic.adapter = myImagesAdapter



        //RecyclerView for Saved Pictures
        binding.recyclerViewSavedPic.setHasFixedSize(true)
        val linearLayoutManager2 : LinearLayoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewSavedPic.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSavedImg = context?.let { MyImagesAdapter(it, postListSaved as ArrayList<Post>) }
        binding.recyclerViewSavedPic.adapter = myImagesAdapterSavedImg



        binding.imagesGridViewBtn.setOnClickListener{
            binding.recyclerViewSavedPic.visibility = View.GONE
            binding.recyclerViewUploadPic.visibility = View.VISIBLE
        }
        binding.imagesSaveBtn.setOnClickListener{
            binding.recyclerViewSavedPic.visibility = View.VISIBLE
            binding.recyclerViewUploadPic.visibility = View.GONE
        }


        binding.totalFollowers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","followers")
            startActivity(intent)
        }
        binding.totalFollowing.setOnClickListener {
            val intent = Intent(context , ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        binding.editAccountSettingsBtn.setOnClickListener {
            val getButtonText = binding.editAccountSettingsBtn.text.toString()
            when{
                getButtonText == "Edit Profile" -> startActivity(Intent(context,AccountSettingActivity::class.java))
                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }

                    addNotification()
                }
                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }
        }
        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPost()
        mySaves()
        return view
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(profileId).exists()){
                    binding.editAccountSettingsBtn.text = "Following"
                }else{
                    binding.editAccountSettingsBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    binding.totalFollowers.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun getFollowings(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    binding.totalFollowing.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun myPhotos(){
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (postList as ArrayList<Post>).clear()
                    for (snapshot in snapshot.children){
                        val post = snapshot.getValue(Post::class.java)
                        if (post!!.getPublisher().equals(profileId)){
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.proImageProfileFrag)

                    binding.profileFragmentUsername.text = user!!.getUsername()

                    binding.fullNameProfileFrag.text = user.getFullname()

                    binding.bioProfileFrag.text = user.getBio()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    override fun onStop() {
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
        super.onStop()
    }

    override fun onPause() {
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
        super.onPause()
    }

    override fun onDestroy() {
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
        super.onDestroy()
    }

    private fun getTotalNumberOfPost(){
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    var postCounter = 0
                    for (snapShot in dataSnapshot.children){
                        val post = snapShot.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId){
                            postCounter++
                        }
                    }
                    binding.totalPosts.text = " " + postCounter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun mySaves(){
        mySavesImg = ArrayList()
        val savedRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        savedRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children){
                        (mySavesImg as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImgData()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun readSavedImgData() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    (postListSaved as ArrayList<Post>).clear()
                    for (snapshot in dataSnapshot.children){
                        val post = snapshot.getValue(Post::class.java)
                        for (key in mySavesImg!!){
                            if (post!!.getPostid() == key){
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(profileId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "Started following you. "
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }
}