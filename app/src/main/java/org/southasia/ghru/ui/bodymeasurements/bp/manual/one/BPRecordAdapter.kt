package org.southasia.ghru.ui.bodymeasurements.bp.manual.one

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import org.southasia.ghru.R
import org.southasia.ghru.vo.BloodPressure


class BPRecordAdapter(private val records: ArrayList<BloodPressure>?) : RecyclerView.Adapter<BPRecordHolder>() {

    override fun onBindViewHolder(holder: BPRecordHolder, position: Int) {

        if (records != null) {
            val record = records[position]
            holder.bindRecord(record, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPRecordHolder {

        val inflatedView = parent.inflate(R.layout.bp_record_list_item, false)
        return BPRecordHolder(inflatedView)
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun getItemCount(): Int {

        if (records != null)
            return records.size
        else
            return 0
    }

}