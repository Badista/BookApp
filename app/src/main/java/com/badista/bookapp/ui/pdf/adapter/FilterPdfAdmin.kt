package com.badista.bookapp.ui.pdf.adapter

import android.widget.Filter
import com.badista.bookapp.ui.pdf.model.ModelPdf

class FilterPdfAdmin: Filter {

    var filterList: ArrayList<ModelPdf>
    var adapterPdfAmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAmin: AdapterPdfAdmin) : super() {
        this.filterList = filterList
        this.adapterPdfAmin = adapterPdfAmin
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val result = FilterResults()

        if (constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().lowercase()

            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                if (filterList[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            result.count = filteredModels.size
            result.values = filteredModels
        } else {
            result.count = filterList.size
            result.values = filterList
        }

        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterPdfAmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        adapterPdfAmin.notifyDataSetChanged()
    }
}