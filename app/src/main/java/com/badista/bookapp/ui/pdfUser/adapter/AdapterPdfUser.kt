package com.badista.bookapp.ui.pdfUser.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.badista.bookapp.databinding.RowPdfUserBinding
import com.badista.bookapp.ui.pdf.model.ModelPdf
import com.badista.bookapp.ui.pdf.view.PdfDetailActivity
import com.badista.bookapp.util.MyApplication

class  AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {

    private var context: Context
    public var pdfArrayList: ArrayList<ModelPdf>
    public var filterList: ArrayList<ModelPdf>

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){
        // init ui components of row
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        /* get data, set data, handle click etc */

        // get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp
        // convert time
        val date= MyApplication.formatTimeStamp(timestamp)

        // set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null) // no need number of page so pass null

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        Log.d("BOOKS_USER_TAG", "getFilter: Filter : $filter")
        if (filter != null){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }
}