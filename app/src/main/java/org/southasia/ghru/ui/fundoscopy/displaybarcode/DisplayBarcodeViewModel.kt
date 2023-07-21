package org.southasia.ghru.ui.fundoscopy.displaybarcode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class DisplayBarcodeViewModel
@Inject constructor() : ViewModel() {


    var hasGivenConsent: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { false }

    var isChecked: Boolean = false

    fun setHasExplained(explained: Boolean) {
        isChecked = explained
        hasGivenConsent.value = (explained)
    }

}
