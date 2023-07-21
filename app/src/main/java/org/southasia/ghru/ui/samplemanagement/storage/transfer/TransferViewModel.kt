package org.southasia.ghru.ui.samplemanagement.storage.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.SampleRequestRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.Message
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.StorageDto
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject

class TransferViewModel @Inject constructor(sampleRequestRepository: SampleRequestRepository) : ViewModel() {

    private val _sampleStorageId: MutableLiveData<SampleStorageId> = MutableLiveData()
    private val _sampleStorageIdDelete: MutableLiveData<SampleRequest> = MutableLiveData()
    var isChecked: Boolean = false

    var sampleMangementPocess: LiveData<Resource<Message>>? = Transformations
            .switchMap(_sampleStorageId) { input ->
                input.ifExists { storageDto, sampleRequest ->

                    sampleRequestRepository.syncSampleStorageFRequest(sampleRequest, storageDto!!)
                }
            }


    var sampleMangementPocessDelete: LiveData<Resource<SampleRequest>>? = Transformations
            .switchMap(_sampleStorageIdDelete) { sample ->
                if (sample == null) {
                    AbsentLiveData.create()
                } else {
                    sampleRequestRepository.delete(sample)
                }
            }

    fun setSync(storageDto: StorageDto?, sampleRequest: SampleRequest?) {
        val update = SampleStorageId(storageDto, sampleRequest)
        if (_sampleStorageId.value == update) {
            return
        }
        _sampleStorageId.value = update
    }


    fun setDelete(sampleRequest: SampleRequest?) {

        if (_sampleStorageIdDelete.value == sampleRequest) {
            return
        }
        _sampleStorageIdDelete.value = sampleRequest
    }

    data class SampleStorageId(val storageDto: StorageDto?, val sampleRequest: SampleRequest?) {
        fun <T> ifExists(f: (StorageDto?, SampleRequest?) -> LiveData<T>): LiveData<T> {
            return if (storageDto == null || sampleRequest == null) {
                AbsentLiveData.create()
            } else {
                f(storageDto, sampleRequest)
            }
        }
    }

    fun setHasChecked(explained: Boolean) {
        isChecked = explained

    }
}