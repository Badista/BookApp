package com.badista.bookapp.ui.pdfUser.adapter

import android.widget.Filter
import com.badista.bookapp.ui.pdf.model.ModelPdf

class FilterPdfUser : Filter {

    var filterList: ArrayList<ModelPdf>
    var adapterPdfUser: AdapterPdfUser

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence ): FilterResults {
        var constraint: CharSequence? = constraint
        val result = FilterResults()

        if (constraint != null && constraint.isNotEmpty()){
            // change to upper case , or lower case to remove case sensitivity
            constraint = constraint.toString().uppercase()

            val filteredModels = ArrayList<ModelPdf>()

            for (i in filterList.indices){
                // validate if match
                if (filterList[i].title.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            // return filtered list and size
            result.count = filteredModels.size
            result.values = filteredModels
        }
        else {
            // return original list and size
            result.count = filterList.size
            result.values = filterList
        }
        return  result
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // apply filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        // notify changes
        adapterPdfUser.notifyDataSetChanged()
    }
}