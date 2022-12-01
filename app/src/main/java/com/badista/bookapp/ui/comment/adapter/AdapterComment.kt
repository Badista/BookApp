package com.badista.bookapp.ui.comment.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import com.badista.bookapp.R
import com.badista.bookapp.databinding.RowCommentBinding
import com.badista.bookapp.ui.comment.model.ModelComment
import com.badista.bookapp.util.MyApplication
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment> {

    val context: Context
    val commentArrayList: ArrayList<ModelComment>

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: RowCommentBinding

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView){
        // init
        val profileIv = binding.profileIv
        val nameTv = binding.nameTv
        val dateTv = binding.dateTv
        val commentTv = binding.commentTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
         binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        /* get data, set data, handle click etc */

        // get data
        val model = commentArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        // format timestamp
        val date = MyApplication.formatTimeStamp(timestamp.toLong())

        // set data
        holder.dateTv.text = date
        holder.commentTv.text = comment

        // we dont have username, profile picture but we have user uid, so we will load using that uid
        loadUserDetails(model, holder)

        // handle click, show dialog for delete comment
        holder.itemView.setOnClickListener {
            /* requirements to delete a comment
            * 1. user must be logged in
            * 2. ui din comment (to be delete) must be same as uid of current user,
            * user can delete only his own comment */

            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid){
                deleteCommentDialog(model, holder)
            }
        }
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get name, profile image
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                     // set data
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    }
                    catch (e: Exception){
                        // in case of exception due to image is empty or null or other reason, set default image
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun deleteCommentDialog(model: ModelComment, holder: HolderComment) {
        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("DELETE"){ d, e ->
                val bookId = model.bookId
                val commentId = model.id

                // delete comment
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comment Deleted...", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete comment due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("CANCEL"){ d, e ->
                d.dismiss()
            }
            .show() // jangan lupa
    }
}