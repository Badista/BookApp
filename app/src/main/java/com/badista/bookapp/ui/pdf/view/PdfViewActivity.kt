package com.badista.bookapp.ui.pdf.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.badista.bookapp.databinding.ActivityPdfViewBinding
import com.badista.bookapp.util.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding

    // book id
    var bookId = ""

    companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        loadBookDetails()
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get pdg url from db")

        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value

                    Log.d(TAG, "onDataChange: Pdf Url : $pdfUrl")

                    loadBookFromUrl("$pdfUrl")
                }
                override fun onCancelled(error: DatabaseError) {
                    
                }

            })
    }

    private fun loadBookFromUrl(pdfurl: String) {
        Log.d(TAG, "loadBookFromUrl: Get pdf from firebase storage using url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfurl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookFromUrl: pdf got from url")

                // load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError { t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()

                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: Failed to get pdg due to ${e.message} ")
                binding.progressBar.visibility = View.GONE
            }
    }
}