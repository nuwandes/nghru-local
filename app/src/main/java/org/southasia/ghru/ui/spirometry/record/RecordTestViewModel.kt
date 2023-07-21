package org.southasia.ghru.ui.spirometry.record


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.southasia.ghru.vo.SpirometryRecord
import javax.inject.Inject

class RecordTestViewModel
@Inject constructor() : ViewModel() {


    var spirometryRecord: MutableLiveData<SpirometryRecord>? = null

    var isValidFEV: Boolean = false
    var isValidFVC: Boolean = false
    var isValidRatio: Boolean = false
    var isValidpEFR: Boolean = false

    fun spirometryRecord(): LiveData<SpirometryRecord> {
        if (spirometryRecord == null) {
            spirometryRecord = MutableLiveData<SpirometryRecord>()
            loadSpirometryRecord()
        }
        return spirometryRecord as LiveData<SpirometryRecord>
    }

    fun loadSpirometryRecord() {
        spirometryRecord?.value = SpirometryRecord()
    }
}