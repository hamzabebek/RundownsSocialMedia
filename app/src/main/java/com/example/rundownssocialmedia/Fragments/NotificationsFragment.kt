package com.example.rundownssocialmedia.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.Adapter.NotificationAdapter
import com.example.rundownssocialmedia.Model.Notification
import com.example.rundownssocialmedia.R
import com.example.rundownssocialmedia.databinding.FragmentNotificationsBinding
import com.example.rundownssocialmedia.databinding.FragmentPostDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class NotificationsFragment : Fragment() {
    private var _binding : FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private var notificationList : List<Notification>? = null
    private var notificationAdapter : NotificationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(inflater, container,false)
        val view = binding.root

        binding.recyclerViewNotifications.setHasFixedSize(true)
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(context)
        notificationList = ArrayList()
        notificationAdapter = context?.let { NotificationAdapter(it,notificationList as ArrayList<Notification>) }
        binding.recyclerViewNotifications.adapter = notificationAdapter

        readNotifications()

        return view
    }

    private fun readNotifications() {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(FirebaseAuth.getInstance().currentUser!!.uid)

        notiRef.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    (notificationList as ArrayList<Notification>).clear()
                    for (snapshot in dataSnapshot.children){
                        val notification = snapshot.getValue(Notification::class.java)
                        (notificationList as ArrayList<Notification>).add(notification!!)
                    }
                    (notificationList as MutableList<*>).reverse()
                    notificationAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}