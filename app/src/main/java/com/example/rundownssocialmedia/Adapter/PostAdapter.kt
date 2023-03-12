package com.example.rundownssocialmedia.Adapter

import android.content.Context
import android.content.Intent
import android.renderscript.Sampler.Value
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rundownssocialmedia.CommentsActivity
import com.example.rundownssocialmedia.Fragments.PostDetailsFragment
import com.example.rundownssocialmedia.Fragments.ProfileFragment
import com.example.rundownssocialmedia.MainActivity
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.ShowUsersActivity
import com.example.rundownssocialmedia.databinding.PostsLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext : Context, private val mPost : List<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>() {
    private var firebaseUser : FirebaseUser? = null
    class PostHolder(val binding : PostsLayoutBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = PostsLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.binding.postImageHome)

        if (post.getDescription().equals("")){
            holder.binding.description.visibility = View.GONE
        }else{
            holder.binding.description.visibility = View.VISIBLE
            holder.binding.description.text = post.getDescription()
        }

        publisherInfo(holder.binding.userProfileImageSearch, holder.binding.userNameSearch, holder.binding.publisher, post.getPublisher())
        isLike(post.getPostid(), holder.binding.postImageLikeBtn)
        numberOfLikes(holder.binding.likes, post.getPostid())
        getTotalComments(holder.binding.comments, post.getPostid())
        checkSaveStatus(post.getPostid(),holder.binding.postSaveCommentBtn)

        holder.binding.postImageLikeBtn.setOnClickListener {
            if (holder.binding.postImageLikeBtn.tag == "Like"){
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.getPublisher(),post.getPostid())
            }else{
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)

            }
        }

            holder.binding.publisher.setOnClickListener() {
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

                editor.putString("profileId", post.getPublisher())

                editor.apply()

                val intentMain = Intent(mContext,MainActivity::class.java)
                intentMain.putExtra("profileFragment","profileFragment")
                holder.itemView.context.startActivity(intentMain)

            }

            holder.binding.userNameSearch.setOnClickListener {
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

                editor.putString("profileId", post.getPublisher())

                editor.apply()

                val intentMain = Intent(mContext,MainActivity::class.java)
                intentMain.putExtra("profileFragment","profileFragment")
                holder.itemView.context.startActivity(intentMain)
            }

            holder.binding.userProfileImageSearch.setOnClickListener{
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

                editor.putString("profileId", post.getPublisher())

                editor.apply()

                val intentMain = Intent(mContext,MainActivity::class.java)
                intentMain.putExtra("profileFragment","profileFragment")
                holder.itemView.context.startActivity(intentMain)
            }

            holder.binding.postImageHome.setOnClickListener {
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

                editor.putString("postId", post.getPostid())

                editor.apply()

                val intentMain = Intent(mContext,MainActivity::class.java)
                intentMain.putExtra("profileFragment","postDetailFragment")
                holder.itemView.context.startActivity(intentMain)
            }
        holder.binding.postImageCommentBtn.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }
        holder.binding.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.binding.postSaveCommentBtn.setOnClickListener {
            if (holder.binding.postSaveCommentBtn.tag == "Save"){
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).setValue(true)
            }else{
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).removeValue()
            }
        }

        holder.binding.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id" , post.getPostid())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

    }

    private fun numberOfLikes(likes: TextView, postid: String) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    likes.text = snapshot.childrenCount.toString() + " Likes"
                }else{


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun getTotalComments(comments: TextView, postid: String) {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postid)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    comments.text = "View all " + snapshot.childrenCount.toString() + " comments"
                }else{


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun isLike(postid: String, postImageLikeBtn: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()){
                    postImageLikeBtn.setImageResource(R.drawable.heart_clicked)
                    postImageLikeBtn.tag = "Liked"
                }else{
                    postImageLikeBtn.setImageResource(R.drawable.heart_not_clicked)
                    postImageLikeBtn.tag = "Like"

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun publisherInfo(userProfileImageSearch: CircleImageView, userNameSearch: TextView, publisher: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(userProfileImageSearch)
                    userNameSearch.text = user!!.getUsername()
                    publisher.text = user.getFullname()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun checkSaveStatus(postid: String, imageView: ImageView){
        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
        savesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                }else{
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addNotification(userId : String,postId: String){
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "Like your post"
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}