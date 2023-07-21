package org.southasia.ghru.ui.registerpatient_sg.scanqrcode.errordialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class ErrorDialogViewModelSG
@Inject constructor() : ViewModel() {

    var errorMsg: MutableLiveData<String> = MutableLiveData<String>().apply { "" }
}
