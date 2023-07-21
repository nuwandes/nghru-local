package org.southasia.ghru.ui.spirometry.tests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import org.southasia.ghru.R
import org.southasia.ghru.vo.SpirometryRecord


class TestRecordAdapter(private val records: ArrayList<SpirometryRecord>) : RecyclerView.Adapter<TestRecordHolder>() {

    override fun onBindViewHolder(holder: TestRecordHolder, position: Int) {

        val record = records[position]
        holder.bindRecord(record, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestRecordHolder {

        val inflatedView = parent.inflate(R.layout.spirometry_test_record, false)
        return TestRecordHolder(inflatedView)
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun getItemCount() = records.size
}