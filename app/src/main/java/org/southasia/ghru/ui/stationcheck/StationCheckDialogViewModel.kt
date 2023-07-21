package org.southasia.ghru.ui.stationcheck

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class StationCheckDialogViewModel
@Inject constructor() : ViewModel() {

    var codecheckMsg: MutableLiveData<String> = MutableLiveData<String>().apply { }
}