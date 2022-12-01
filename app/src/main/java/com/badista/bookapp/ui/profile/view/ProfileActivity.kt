package com.badista.bookapp.ui.profile.view

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.badista.bookapp.R
import com.badista.bookapp.databinding.ActivityProfileBinding
import com.badista.bookapp.ui.pdf.model.ModelPdf
import com.badista.bookapp.ui.pdfFavorite.adapter.AdapterPdfFavorite
import com.badista.bookapp.util.MyApplication
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var booksArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var progressDialog: ProgressDialog

    companion object{
        const val TAG = "PROFILE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // reset to default values
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteBooksCountTv.text = "N/A"
        binding.accountStatusTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadUserInfo()
        loadFavoriteBooks()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ProfileEditActivity::class.java))
        }

        binding.accountStatusTv.setOnClickListener {
            if (firebaseUser.isEmailVerified){
                Toast.makeText(this, "Already Verified...", Toast.LENGTH_SHORT).show()
            }
            else {
                emailVerificationDialog()
            }
        }
    }

    private fun emailVerificationDialog() {
        // show confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Are you sure you want to send email verification intructions to your email ${firebaseUser.email}")
            .setPositiveButton("SEND"){ d, e ->
                setEmailVerification()
            }
            .setNegativeButton("CANCEL"){ d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun setEmailVerification() {
        progressDialog.setMessage("Sending email verification instructions to email ${firebaseUser.email}")
        progressDialog.show()

        // send instruction
        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "setEmailVerification: Instructions sent!")
                Toast.makeText(this, "Instruction sent! check your email ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "setEmailVerification: Failed to send due to.. ${e.message}")
                Toast.makeText(this, "Failed to send due to.. ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        // check if user is verified or not, changes my effect after re login when you verify email
        if (firebaseUser.isEmailVerified){
            binding.accountStatusTv.text = "Verified"
        } else {
            binding.accountStatusTv.text = "Not Verified"
        }

        // db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    // convert timestamp to perofer date format
                    val formatedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    // set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formatedDate
                    binding.accountTypeTv.text = userType
                    // set image
                    try{
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }
                    catch (e: Exception){
                        Log.d(TAG, "onDataChange: $e")
                    }
                    Log.d(TAG, "onDataChange: Email: $email, Name: $name, ProfileImage: $profileImage, TimeStamp: $timestamp, Uid: $uid, UserType: $userType, FormattedDate: $formatedDate")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: $error")
                }

            })
    }

    private fun loadFavoriteBooks(){
        // init array list
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear arraylist, before starting adding data
                    booksArrayList.clear()
                    for (ds in snapshot.children){
                        // get only id of the book, rest of the info we have loaded in adapter class
                        val bookId = "${ds.child("bookId").value}"

                        // set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        // add model to list
                        booksArrayList.add(modelPdf)
                    }
                    //set number of favorite book
                    binding.favoriteBooksCountTv.text = "${booksArrayList.size}"

                    // setup adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, booksArrayList)
                    // set adapter to recyclerview
                    binding.favoriteRv.adapter = adapterPdfFavorite
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}