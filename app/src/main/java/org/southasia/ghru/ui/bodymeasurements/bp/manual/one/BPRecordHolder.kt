package org.southasia.ghru.ui.bodymeasurements.bp.manual.one

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bp_record_list_item.view.*
import org.southasia.ghru.vo.BloodPressure


class BPRecordHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

    private var view: View = v

    init {
        v.setOnClickListener(this)
    }

    override fun onClick(v: View) {
       //L.d("RecyclerView", "CLICK!")
    }

    fun bindRecord(record: BloodPressure, index: Int) {
        view.textViewNo.text = (index + 1).toString()
        view.textViewArm.text = record.arm.value.toString()
        view.textViewSystolic.text = record.systolic.value.toString()
        view.textViewDiastolic.text = record.diastolic.value.toString()
        view.textViewPuls.text = record.pulse.value.toString()

    }
}