package org.southasia.ghru.ui.samplemanagement.tubescanbarcode.errordialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class ErrorDialogViewModel
@Inject constructor() : ViewModel() {

    var errorMsg: MutableLiveData<String> = MutableLiveData<String>().apply { "" }
}
