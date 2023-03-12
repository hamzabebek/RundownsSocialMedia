package com.example.rundownssocialmedia

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.rundownssocialmedia.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        storage = Firebase.storage
        binding.signinLinkBtn.setOnClickListener {
            startActivity(Intent(this@SignupActivity,SignInActivity::class.java))
        }
        binding.signupBtn.setOnClickListener {
            register()
        }
    }
    @Suppress("DEPRECATION")
    fun register(){
        val fullName = binding.fullnameSignup.text.toString()
        val userName = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this@SignupActivity,"Fullname is required.",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this@SignupActivity,"Username is required.",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this@SignupActivity,"Email is required.",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this@SignupActivity,"Password is required.",Toast.LENGTH_SHORT).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                    auth.createUserWithEmailAndPassword(email , password)
                        .addOnSuccessListener {
                            saveUserInfo(fullName,userName, email, progressDialog)
                        }.addOnFailureListener{
                            Toast.makeText(this@SignupActivity,"Error : ${it.localizedMessage}",Toast.LENGTH_SHORT).show()
                            auth.signOut()
                            progressDialog.dismiss()
                        }
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun saveUserInfo(fullName : String, userName : String, email : String, progressDialog : ProgressDialog){
        val currentUserId = Firebase.auth.currentUser!!.uid

        val usersReference : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName
        userMap["username"] = userName.lowercase()
        userMap["email"] = email
        userMap["bio"] = "Hey i am using Rundowns Social Media."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/rundowns-social-media.appspot.com/o/profile.png?alt=media&token=3048aa1b-9197-45f1-a95b-808bd6af2cf1"

        usersReference.child(currentUserId).setValue(userMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this@SignupActivity,"Account has been created successfully",Toast.LENGTH_SHORT).show()


                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)

                val goMainIntent = Intent(this@SignupActivity,MainActivity::class.java)
                goMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(goMainIntent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@SignupActivity,"Error : ${it.localizedMessage}",Toast.LENGTH_SHORT).show()
                auth.signOut()
                progressDialog.dismiss()
            }
    }

}




