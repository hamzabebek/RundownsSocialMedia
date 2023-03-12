package com.example.rundownssocialmedia.Adapter


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rundownssocialmedia.Fragments.ProfileFragment
import com.example.rundownssocialmedia.MainActivity
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.UserItemLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(private var mContext : Context?, private var mUser:List<User>,private var isFragment : Boolean = false) : RecyclerView.Adapter<UserAdapter.UserHolder>() {
    private var firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser
    class UserHolder(val binding : UserItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val binding = UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserHolder(binding)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {

        val user = mUser[position]

        holder.binding.userNameSearch.text = user.getUsername()
        holder.binding.userFullNameSearch.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.binding.userProfileImageSearch)
        
        checkFollowingStatus(user.getUID(), holder.binding.followBtnSearch)

        holder.itemView.setOnClickListener(View.OnClickListener {
            if (isFragment){
                val pref = mContext?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
                pref?.putString("profileId",user.getUID())
                pref?.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }else{
                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("publisherId",user.getUID())
                mContext!!.startActivity(intent)
            }
        })

        holder.binding.followBtnSearch.setOnClickListener {
            if (holder.binding.followBtnSearch.text.toString() == "Follow") {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                    addNotification(user.getUID())
                }
            }else{
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }

            }
        }






    }

    private fun checkFollowingStatus(uid: String, followBtnSearch: Button){
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.child(uid).exists()){
                    followBtnSearch.text = "Following"
                }else{
                    followBtnSearch.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun addNotification(userId : String){
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "Started following you. "
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }
}