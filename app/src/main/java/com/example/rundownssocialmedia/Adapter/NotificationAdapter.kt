package com.example.rundownssocialmedia.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rundownssocialmedia.Fragments.PostDetailsFragment
import com.example.rundownssocialmedia.Fragments.ProfileFragment
import com.example.rundownssocialmedia.Model.Notification
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.NotificationsItemLayoutBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class NotificationAdapter(private var mContext : Context, private val mNotification : List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationHolder>(){
    class NotificationHolder(val binding: NotificationsItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding = NotificationsItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotificationHolder(binding)
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        val notification = mNotification[position]

        if (notification.getText().equals("Started following you. ")){
            holder.binding.commentNotification.text = "Started following you. "
        }else if (notification.getText().equals("Like your post")){
            holder.binding.commentNotification.text = "Liked your post"
        }else if (notification.getText().contains("Commented : ")){
            holder.binding.commentNotification.text = notification.getText().replace("Commented: ","Commented: ")
        }else{
            holder.binding.commentNotification.text = notification.getText()
        }
        userInfo(holder.binding.notificationProfileImage, holder.binding.usernameNotification,notification.getUserId())

        if (notification.getIsPost()){
            holder.binding.notificationPostImage.visibility = View.VISIBLE
            getPostImage(holder.binding.notificationPostImage,notification.getPostId())
        }else{
            holder.binding.notificationPostImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (notification.getIsPost()){
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

                editor.putString("postId", notification.getPostId())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()

            }else{

                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

                editor.putString("profileId", notification.getUserId())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()

            }
        }
    }

    private fun userInfo(imageView: ImageView, userName : TextView , publisherId : String){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageView)
                    userName.text = user.getUsername()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun getPostImage(imageView: ImageView, postID : String){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postID)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val post = snapshot.getValue(Post::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile).into(imageView)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}