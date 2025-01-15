package com.example.rundownssocialmedia

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rundownssocialmedia.Model.User
import com.example.rundownssocialmedia.databinding.ActivityAccountSettingBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAccountSettingBinding
    private var imageUri : Uri? = null
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
        } else {
            val exception = result.error
            print(exception)
        }
    }
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var auth : FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl : String = ""
    private var storageProfilePicRef : StorageReference? = null



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        registerLauncher()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")


        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val logoutIntent = Intent(this@AccountSettingActivity,SignInActivity::class.java)
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(logoutIntent)
            finish()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = "clicked"
            if(ContextCompat.checkSelfPermission(this@AccountSettingActivity,android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@AccountSettingActivity,android.Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for Gallery !!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                            //Burada
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                    }.show()
                }else{
                        //Burada
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                }
            }else{
                startCrop()

            }
        }

        binding.closeProfileBtn.setOnClickListener {
            val mainIntent = Intent(this@AccountSettingActivity,MainActivity::class.java)
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(mainIntent)
        }

        binding.saveInfoProfileBtn.setOnClickListener {
            if (checker == "clicked"){
                uploadImageAndUpdateInfo()
            }else{
                updateUserInfoOnly()
            }
        }
        userInfo()
    }

    private fun startCrop(){
        cropImage.launch(
            CropImageContractOptions(
                uri = imageUri,
                cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.PNG,
                    aspectRatioX = 9,
                    aspectRatioY = 16
                )
            )
        )
        binding.profileImageViewProfileFrag.setImageURI(imageUri)
    }
    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                //permission granted
                startCrop()
            }else{
                //permission denied
                Toast.makeText(this@AccountSettingActivity,"Permission Needed ! ",Toast.LENGTH_SHORT).show()
            }

        }


    }


    private fun updateUserInfoOnly() {

        if (binding.fullNameProfileFrag.text.toString() == ""){
            Toast.makeText(this@AccountSettingActivity,"Please write full name first.",Toast.LENGTH_SHORT).show()
        }else if (binding.usernameProfileFrag.text.toString() == ""){Toast.makeText(this@AccountSettingActivity,"Please write username first.",Toast.LENGTH_SHORT).show()
        }else if (binding.bioProfileFrag.text.toString() == ""){Toast.makeText(this@AccountSettingActivity,"Please write bio first.",Toast.LENGTH_SHORT).show()
        }else{

        }


            val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["fullname"] = binding.fullNameProfileFrag.text.toString()
        userMap["username"] = binding.usernameProfileFrag.text.toString().lowercase()
        userMap["bio"] = binding.bioProfileFrag.text.toString()

        usersRef.child(firebaseUser.uid).updateChildren(userMap)
        Toast.makeText(this@AccountSettingActivity,"Account information has been updated successfully",Toast.LENGTH_SHORT).show()

        val intent = Intent(this@AccountSettingActivity,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.profileImageViewProfileFrag)
                    binding.usernameProfileFrag.setText(user.getUsername())
                    binding.fullNameProfileFrag.setText(user.getFullname())
                    binding.bioProfileFrag.setText(user.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    @Suppress("DEPRECATION")
    private fun uploadImageAndUpdateInfo() {
        when{
            imageUri == null -> Toast.makeText(this,"Please select image first",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) -> Toast.makeText(this,"Please write full name first.",Toast.LENGTH_SHORT).show()
            binding.usernameProfileFrag.text.toString() == "" -> Toast.makeText(this,"Please write username first",Toast.LENGTH_SHORT).show()
            binding.bioProfileFrag.text.toString() == "" -> Toast.makeText(this,"Please write bio first",Toast.LENGTH_SHORT).show()
            else ->{
                val progressDialog = ProgressDialog(this@AccountSettingActivity)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser.uid+ ".jpg")
                fileRef.putFile(imageUri!!)
                fileRef.downloadUrl.addOnSuccessListener {url ->

                    val getDownloadUri = url
                    myUrl = getDownloadUri.toString()

                    val ref = FirebaseDatabase.getInstance().reference.child("Users")
                    val userMap = HashMap<String,Any>()
                    userMap["fullname"] = binding.fullNameProfileFrag.text.toString()
                    userMap["username"] = binding.usernameProfileFrag.text.toString().lowercase()
                    userMap["bio"] = binding.bioProfileFrag.text.toString()
                    userMap["image"] = myUrl

                    ref.child(firebaseUser.uid).updateChildren(userMap)

                    Toast.makeText(this@AccountSettingActivity,"Account information has been updated successfully",Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@AccountSettingActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    progressDialog.dismiss()

                }.addOnFailureListener { e ->
                    Toast.makeText(this@AccountSettingActivity,e.localizedMessage,Toast.LENGTH_SHORT).show()

                    progressDialog.dismiss()
                }

            }

        }
    }
}
