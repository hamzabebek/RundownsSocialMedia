package com.example.rundownssocialmedia

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.rundownssocialmedia.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        binding.signupLinkBtn.setOnClickListener {
            startActivity(Intent(this@SignInActivity,SignupActivity::class.java))
        }
        binding.loginBtn.setOnClickListener {
            loginUser()
        }
    }

    @Suppress("DEPRECATION")
    private fun loginUser() {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        when{
            TextUtils.isEmpty(email) -> Toast.makeText(this@SignInActivity,"Email is required.",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this@SignInActivity,"Password is required.",Toast.LENGTH_SHORT).show()

            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Login")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    progressDialog.dismiss()

                    val intent = Intent(this@SignInActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this@SignInActivity,"Error : ${it.localizedMessage}",Toast.LENGTH_SHORT).show()

                    auth.signOut()

                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onStart() {
        if (auth.currentUser != null){
            val intent = Intent(this@SignInActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        super.onStart()
    }

}