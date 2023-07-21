package org.southasia.ghru.ui.spirometry.tests

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.spirometry_test_record.view.*
import org.southasia.ghru.vo.SpirometryRecord

class TestRecordHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

    private var view: View = v

    init {
        v.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        Log.d("RecyclerView", "CLICK!")
    }

    fun bindRecord(record: SpirometryRecord, index: Int) {
        view.textViewNo.text = (index + 1).toString()
        view.textViewFEV.text = record.fev?.value.toString()
        view.textViewFVC.text = record?.fvc?.value.toString()
        view.textViewRation.text = record?.ratio?.value.toString()
        view.textViewPev.text = record?.pEFR?.value.toString()

    }
}