package com.badista.bookapp.ui.dashboard.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.badista.bookapp.R
import com.badista.bookapp.databinding.ActivityDashboardAdminBinding
import com.badista.bookapp.ui.category.adapter.AdapterCategory
import com.badista.bookapp.ui.category.model.ModelCategory
import com.badista.bookapp.ui.category.view.CategoryAddActivity
import com.badista.bookapp.ui.category.view.PdfAddActivity
import com.badista.bookapp.ui.main.view.MainActivity
import com.badista.bookapp.ui.profile.view.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e:Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this@DashboardAdminActivity, ProfileActivity::class.java))
        }

        binding.addCategoryBtn.setOnClickListener {
            Intent(this@DashboardAdminActivity, CategoryAddActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this@DashboardAdminActivity, PdfAddActivity::class.java))
        }
    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)

                    // add to arraylist
                    categoryArrayList.add(model!!)
                }

                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            startActivity(Intent(this@DashboardAdminActivity, MainActivity::class.java))
        }
        else{
            val email = firebaseUser.email
            binding.subtitleTv.text = email
        }
    }
}