package org.southasia.ghru.ui.ecg.guide.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class GuideMainViewModel
@Inject constructor() : ViewModel() {

    var hasExplained: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { }

    var isChecked: Boolean = false

    fun setHasExplained(explained: Boolean) {
        isChecked = explained
        hasExplained.value = (explained)
    }
}
