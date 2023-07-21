package org.southasia.ghru.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.HomeRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.HomeItem
import org.southasia.ghru.vo.Resource
import javax.inject.Inject


class HomeViewModel
@Inject constructor(repository: HomeRepository) : ViewModel() {
    private val _home = MutableLiveData<String>()
    val home: LiveData<String>
        get() = _home

    val homeItem: LiveData<Resource<List<HomeItem>>> = Transformations
        .switchMap(_home) { login ->
            if (login == null) {
                AbsentLiveData.create()
            } else {
                repository.getHomeItems();
            }
        }


    fun setId(lang: String?) {
        if (_home.value != lang) {
            _home.value = lang
        }
    }

}
