package com.example.rundownssocialmedia

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rundownssocialmedia.databinding.ActivityAddPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.util.UUID

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddPostBinding
    private lateinit var storage : FirebaseStorage
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
    private var myUrl : String = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()
        storage = Firebase.storage

        if(ContextCompat.checkSelfPermission(this@AddPostActivity,android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_DENIED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@AddPostActivity,android.Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view,"Permission needed for Gallery !!", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }.show()
            }else{
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }else{
            startCrop()
        }

        binding.saveNewPostBtn.setOnClickListener { uploadImage() }
        binding.imagePost.setOnClickListener {
            startCrop()
        }
        binding.closeAddPostBtn.setOnClickListener {
            val mainIntent = Intent(this@AddPostActivity,MainActivity::class.java)
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(mainIntent)
        }

    }
    @Suppress("DEPRECATION")
    private fun uploadImage(){
        when{

            imageUri == null -> Toast.makeText(this,"Please select image first",Toast.LENGTH_SHORT).show()
            //TextUtils.isEmpty(binding.descriptionPost.text.toString()) -> Toast.makeText(this,"Please write description.",Toast.LENGTH_SHORT).show()
            else ->{
                val progressDialog = ProgressDialog(this@AddPostActivity)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are adding your picture post...")
                    progressDialog.show()
                val uuid = UUID.randomUUID()
                val imageName = "${uuid}.jpg"

                val reference = storage.reference
                val imageReference = reference.child("Posts Pictures").child(imageName)

                imageReference.putFile(imageUri!!).addOnSuccessListener {
                    val uploadPictureReference = storage.reference.child("Posts Pictures").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener {url ->

                    val getDownloadUrl = url.toString()
                    myUrl = getDownloadUrl

                    val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                    val postId = ref.push().key
                    val postMap = HashMap<String, Any>()
                    postMap["postid"] = postId!!
                    postMap["description"] = binding.descriptionPost.text.toString()
                    postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                    postMap["postimage"] = myUrl

                    ref.child(postId).updateChildren(postMap)

                    Toast.makeText(this@AddPostActivity,"Post uploaded successfully",Toast.LENGTH_SHORT).show()

                    val goMainIntent = Intent(this@AddPostActivity, MainActivity::class.java)
                    startActivity(goMainIntent)
                    finish()

                    progressDialog.dismiss()

                }.addOnFailureListener { e ->

                    Toast.makeText(this@AddPostActivity,e.localizedMessage,Toast.LENGTH_SHORT).show()

                    progressDialog.dismiss()

                }
                }.addOnFailureListener{
                    Toast.makeText(this@AddPostActivity,it.localizedMessage,Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        binding.imagePost.setImageURI(imageUri)
    }
    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                //permission granted
                startCrop()
            }else{
                //permission denied
                Toast.makeText(this@AddPostActivity,"Permission Needed ! ", Toast.LENGTH_SHORT).show()
            }

        }


    }
}