package org.southasia.ghru.ui.fundoscopy.guide.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class GuideMainViewModel
@Inject constructor() : ViewModel() {


    var validationError: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { }

    var hasExplained: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { }

    var isChecked: Boolean = false


    fun setHasExplained(explained: Boolean) {
        isChecked = explained
        hasExplained.postValue(explained)
    }

}
