package com.example.rundownssocialmedia.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rundownssocialmedia.Adapter.UserAdapter
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var userAdapter: UserAdapter? = null
    private lateinit var mUser : MutableList<User>
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        auth = Firebase.auth
        mUser = ArrayList()

        binding.recyclerViewHomeSearch.setHasFixedSize(true)
        binding.recyclerViewHomeSearch.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(context,mUser as ArrayList<User>, true)
        binding.recyclerViewHomeSearch.adapter = userAdapter

        binding.searchEditText.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.searchEditText.text.toString() == ""){
                    Toast.makeText(context,"Kullanıcı Bulunamadı.",Toast.LENGTH_SHORT).show()

                }else{
                    binding.recyclerViewHomeSearch.visibility = View.VISIBLE
                    retrieveUser()
                    searchUser(s.toString().lowercase())

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("username").startAt(input).endAt(input + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser.clear()
                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User::class.java)
                        if (user != null){
                            mUser.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun retrieveUser() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(binding.searchEditText.text.toString() == ""){
                    mUser.clear()
                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User::class.java)
                        if (user != null){
                            mUser.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}

