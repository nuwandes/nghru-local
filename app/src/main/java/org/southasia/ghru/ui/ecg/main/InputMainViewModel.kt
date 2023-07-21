package org.southasia.ghru.ui.ecg.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class InputViewModel
@Inject constructor() : ViewModel() {

    var hasGivenConsent: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { }

    var isChecked: Boolean = false

    fun setHasExplained(explained: Boolean) {
        isChecked = explained
        hasGivenConsent.value = (explained)
    }
}
