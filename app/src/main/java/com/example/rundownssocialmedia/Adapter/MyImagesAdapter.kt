package com.example.rundownssocialmedia.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rundownssocialmedia.Fragments.PostDetailsFragment
import com.example.rundownssocialmedia.Model.Post
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.ImagesItemLayoutBinding
import com.squareup.picasso.Picasso

class MyImagesAdapter(private val mContext : Context, private val mPost : List<Post>) : RecyclerView.Adapter<MyImagesAdapter.ImageHolder>() {
    class ImageHolder(val binding: ImagesItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val binding = ImagesItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ImageHolder(binding)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val post : Post = mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.binding.postImage)

        holder.binding.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }
    }
}