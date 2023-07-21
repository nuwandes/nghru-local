package org.southasia.ghru.ui.samplemanagement.lipidprofile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class LipidProfileViewModel
@Inject constructor() : ViewModel() {

    var totalCholesterol: MutableLiveData<String> = MutableLiveData<String>().apply { "" }

    var hDL: MutableLiveData<String> = MutableLiveData<String>().apply { "" }

    var lDLC: MutableLiveData<String> = MutableLiveData<String>().apply { "" }

    var triglycerol: MutableLiveData<String> = MutableLiveData<String>().apply { "" }

    var isValidateError: Boolean = false

}
