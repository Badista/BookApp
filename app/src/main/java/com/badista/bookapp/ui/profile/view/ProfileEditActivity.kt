package com.badista.bookapp.ui.profile.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.badista.bookapp.R
import com.badista.bookapp.databinding.ActivityProfileEditBinding
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    companion object{
        const val TAG = "PROFILE_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup dialog progress
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }

            profileIv.setOnClickListener {
                showImageAttachMenu()
            }

            updateBtn.setOnClickListener {
                validateData()
            }
        }
    }

    private var name = ""

    private fun validateData() {
        // get data
        name = binding.nameEt.text.toString().trim()

        // validate data
        if (name.isEmpty()){
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        }
        else{
            if (imageUri == null){
                // need to update without image
                updateProfile("")
            }
            else{
                // need to update with image
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        // image path and name, use uid to replace previous
        val filePathAndName = "ProfileImages/"+firebaseAuth.uid

        // storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // image uploaded, get url of uploaded image
                progressDialog.dismiss()

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "uploadImage: Failed to upload image due to ${e.message}")
                Toast.makeText(this, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating Profile")
        progressDialog.show()

        // setup info to update to db
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["name"] = "$name"
        if (imageUri != null){
            hashmap["profileImage"] = uploadedImageUrl
        }

        // update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "updateProfile: Failed to update Profile due to ${e.message}")
                Toast.makeText(this, "Failed to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImageAttachMenu() {
        /* Show popup menu with options camera, galery to pick image*/

        // setup popup menu
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        // handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item ->
            // get id of clicked item

            val id = item.itemId
            if (id == 0){
                // camera clicked
                pickImagecamera()
            }
            else {
                // galecy clicked
                pickImageGallery()
            }

            true
        }
    }

    private fun pickImagecamera() {
        // intent to oick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    // used to handle result of camera intent (new way in replacement of start activity for result
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult> { result ->
            // get uri of imgae
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
//                imageUri = data!!.data -> no need we already have image in imageUri in camera case (line 94)

                // set to image view
                binding.profileIv.setImageURI(imageUri)
            }
            else {
                // Cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun pickImageGallery() {
        // intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    // used to handle result of gallery intent (new way in replacement of start activity for result
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult> { result ->
            // get uri of imgae
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                // set to image view
                binding.profileIv.setImageURI(imageUri)
            }
            else {
                // Cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun loadUserInfo() {
        // db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get user info
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    // set data
                    binding.nameEt.setText(name)
                    // set image
                    try{
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }
                    catch (e: Exception){
                        Log.d(TAG, "onDataChange: $e")
                    }
                    Log.d(TAG, "onDataChange: Name: $name, ProfileImage: $profileImage, TimeStamp: $timestamp")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: $error")
                }

            })
    }
}