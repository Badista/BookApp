package com.badista.bookapp.ui.pdfUser.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.badista.bookapp.databinding.FragmentBooksUserBinding
import com.badista.bookapp.ui.pdf.model.ModelPdf
import com.badista.bookapp.ui.pdfUser.adapter.AdapterPdfUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String) : BooksUserFragment{

            val fragment = BooksUserFragment()
            // put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)

            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get argument that we passed in new instance method
         val args = arguments
        if (args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        // load pdf according to category
        Log.d(TAG, "onCreateView: Category $category")
        if (category == "All"){
            // load all book
            loadAllBooks()
        }
        else if (category == "Most Viewed"){
            // load most viewed books
            loadMostViewedDownloadedBooks("viewsCount")
        }
        else if (category == "Most Downloaded") {
            // load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount")
        }
        else{
            loadCategorizedBooks()
        }

        // search
        binding.searchEt.addTextChangedListener { object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    Log.d(TAG, "onTextChanged: Searching...")
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        } }

        return binding.root
    }

    private fun loadAllBooks() {
        // init list
        pdfArrayList = ArrayList()
        Log.d(TAG, "loadAllBooks: pdfArrayList : $pdfArrayList")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    // get data
                    val model = ds.getValue(ModelPdf::class.java)
                    // add to list
                    pdfArrayList.add(model!!)
                }
                // setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                // set adapter to recyclerView
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: $error")
            }

        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        // init list
        pdfArrayList = ArrayList()
        Log.d(TAG, "loadAllBooks: pdfArrayList : $pdfArrayList")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) // load 10 most viewed or downloaded books.
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    // get data
                    val model = ds.getValue(ModelPdf::class.java)
                    // add to list
                    pdfArrayList.add(model!!)
                }
                // setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                // set adapter to recyclerView
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: $error")
            }

        })
    }

    private fun loadCategorizedBooks() {
        // init list
        pdfArrayList = ArrayList()
        Log.d(TAG, "loadAllBooks: pdfArrayList : $pdfArrayList")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list before starting adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        // get data
                        val model = ds.getValue(ModelPdf::class.java)
                        // add to list
                        pdfArrayList.add(model!!)
                    }
                    // setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    // set adapter to recyclerView
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: $error")
                }

            })
    }
}