package com.example.rundownssocialmedia.Adapter

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Profile
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.rundownssocialmedia.Fragments.HomeFragment
import com.example.rundownssocialmedia.Fragments.ProfileFragment
import com.example.rundownssocialmedia.MainActivity
import com.example.rundownssocialmedia.Model.Comment
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.CommentsItemLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class CommentsAdapter (private var mContext : Context, private val mComment : MutableList<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentHolder>(){
    private var firebaseUser : FirebaseUser? = null
    class CommentHolder(val binding : CommentsItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val binding = CommentsItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CommentHolder(binding)
    }


    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment!![position]
        holder.binding.commentText.text = comment.getComment()
        getUserInfo(holder.binding.userProfileImageComment, holder.binding.userNameComment, comment.getPublisher())

        holder.binding.userProfileImageComment.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

            editor.putString("profileId", comment.getPublisher())

            editor.apply()

            val intentMain = Intent(mContext,MainActivity::class.java)
            intentMain.putExtra("profileFragment","profileFragment")
            intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            holder.itemView.context.startActivity(intentMain)
        }
    }




    override fun getItemCount(): Int {
        return mComment.size
    }

    private fun getUserInfo(userProfileImageComment: CircleImageView, userNameComment: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(com.example.rundownssocialmedia.R.drawable.profile).into(userProfileImageComment)

                    userNameComment.text = user!!.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}