package org.southasia.ghru.ui.datamanagement

import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.data_management_item.view.*
import org.southasia.ghru.R
import org.southasia.ghru.vo.Axivity
import org.southasia.ghru.vo.ECGStatus
import org.southasia.ghru.vo.FundoscopyRequest
import org.southasia.ghru.vo.SpirometryRequest
import org.southasia.ghru.vo.request.*

class DataManagementHolder (v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

    private var view: View = v

    init {
        v.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        //L.d("RecyclerView", "CLICK!")
    }
    fun bindRecord(record: Any, index: Int) {

        if(record is BloodPressureMetaRequest)
        {
            var bloodPressureMetaRequest : BloodPressureMetaRequest = record
            view.imgStation.setImageResource(R.drawable.icon_data_bp)
            view.textViewScreeningID.text = bloodPressureMetaRequest.body.screeningId
            view.textViewDate.text = bloodPressureMetaRequest.meta.endTime
        }
        else if (record is BodyMeasurementMeta)
        {
            var bodyMeasurementMeta : BodyMeasurementMeta = record
            view.imgStation.setImageResource(R.drawable.icon_data_bm)
            view.textViewScreeningID.text = bodyMeasurementMeta.screeningId
            view.textViewDate.text = bodyMeasurementMeta.meta?.endTime
        }
        else if (record is ECGStatus)
        {
            var ecgStatus :ECGStatus = record
            view.imgStation.setImageResource(R.drawable.icon_data_ecg)
            view.textViewScreeningID.text = ecgStatus.screeningId
            view.textViewDate.text = ecgStatus.meta?.endTime
        }
        else if (record is SpirometryRequest)
        {
            var spirometryRequest :SpirometryRequest = record
            view.imgStation.setImageResource(R.drawable.icon_data_spirometry)
            view.textViewScreeningID.text = spirometryRequest.screeningId
            view.textViewDate.text = spirometryRequest.meta?.endTime
        }
        else if (record is FundoscopyRequest)
        {
            var fundoscopyRequest :FundoscopyRequest = record
            view.imgStation.setImageResource(R.drawable.icon_data_fundoscopy)
            view.textViewScreeningID.text = fundoscopyRequest.screeningId
            view.textViewDate.text = fundoscopyRequest.meta?.endTime
        }
        else if(record is CancelRequest)
        {
            var cancelRequest : CancelRequest = record
            if(cancelRequest.stationType == "body-measurement")
                view.imgStation.setImageResource(R.drawable.icon_data_bm)
            else if(cancelRequest.stationType == "blood-pressure")
                view.imgStation.setImageResource(R.drawable.icon_data_bp)
            else if(cancelRequest.stationType == "spirometry")
                view.imgStation.setImageResource(R.drawable.icon_data_spirometry)
            else if(cancelRequest.stationType == "fundoscopy")
                view.imgStation.setImageResource(R.drawable.icon_data_fundoscopy)
            else if(cancelRequest.stationType == "ecg")
                view.imgStation.setImageResource(R.drawable.icon_data_ecg)
            else if(cancelRequest.stationType == "axivity")
                view.imgStation.setImageResource(R.drawable.icon_data_activity)
            else
                view.imgStation.setImageResource(R.drawable.icon_data_bio_samples)

            view.textViewScreeningID.text = cancelRequest.screeningId
            view.textViewDate.text = cancelRequest.createdDateTime

        }
        else if(record is SampleRequest)
        {
            var sampleRequest : SampleRequest = record
            view.imgStation.setImageResource(R.drawable.icon_data_bio_samples)
            view.textViewScreeningID.text = sampleRequest.screeningId
            view.textViewDate.text = sampleRequest.meta?.endTime
        }
        else if(record is Axivity)
        {
            var axivity : Axivity = record
            view.imgStation.setImageResource(R.drawable.icon_data_activity)
            view.textViewScreeningID.text = axivity.screeningId
            view.textViewDate.text = axivity.meta?.endTime
        }
        else if(record is HouseholdRequestMeta)
        {
            var househld : HouseholdRequestMeta = record
            view.imgStation.setImageResource(R.drawable.ic_icon_enumeration)
            view.textViewScreeningID.text = househld.householdRequest?.enumerationId
            view.textViewDate.text = househld.meta?.endTime
        }
        else if(record is ParticipantRequest)
        {
            var participant : ParticipantRequest = record
            view.imgStation.setImageResource(R.drawable.ic_icon_register_patient)
            view.textViewScreeningID.text = participant.screeningId
            view.textViewDate.text = participant.createdDateTime
        }

    }
}