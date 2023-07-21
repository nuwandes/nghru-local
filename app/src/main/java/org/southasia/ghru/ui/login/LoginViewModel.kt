package org.southasia.ghru.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.AccessTokenRepository
import org.southasia.ghru.repository.StationDevicesRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.AccessToken
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.StationDeviceData
import javax.inject.Inject


class LoginViewModel
@Inject constructor(
    accessTokenRepository: AccessTokenRepository,
    stationDevicesRepository: StationDevicesRepository
) : ViewModel() {


    private val _device = MutableLiveData<String>()

    val device: LiveData<String>
        get() = _device


    private val _loginId: MutableLiveData<LoginId> = MutableLiveData()


    private val _accessToken = MutableLiveData<AccessToken>()


    private val _email = MutableLiveData<String>()

    private val _stationDevice = MutableLiveData<String>()

    //private val _hemoDevice = MutableLiveData<String>()

    private val _stationDeviceList = MutableLiveData<List<StationDeviceData>>()

    private var isOnline : Boolean = false

    val accessTokenOffline: LiveData<Resource<AccessToken>>? = Transformations
        .switchMap(_email) { email ->
            if (email == null) {
                AbsentLiveData.create()
            } else {
                accessTokenRepository.getTokerByEmail(email)
            }
        }

    fun setEmail(email: String) {
        val update = email
        if (_email.value == update) {
            return
        }
        _email.value = update
    }

    fun setStationDevice(stationDevice: String) {
        val update = stationDevice
        if (_stationDevice.value == update) {
            return
        }
        _stationDevice.value = update
    }

    fun setStationDeviceList(stationDeviceList: List<StationDeviceData>) {
        val update = stationDeviceList
        if (_stationDeviceList.value == update) {
            return
        }
        _stationDeviceList.value = update
    }

    var accessToken: LiveData<Resource<AccessToken>>? = Transformations
        .switchMap(_loginId) { input ->
            input.ifExists { email, password ->
                accessTokenRepository.loginUser(email, password,isOnline)
            }
        }

    var stationDevices: LiveData<Resource<ResourceData<List<StationDeviceData>>>>? = Transformations
        .switchMap(_stationDevice) { input ->
            stationDevicesRepository.loadStationDevices()
        }

//    var hemoDevices: LiveData<Resource<ResourceData<List<StationDeviceData>>>>? = Transformations
//        .switchMap(_hemoDevice) { input ->
//            stationDevicesRepository.loadHemoDevices()
//        }


    var stationDeviceList: LiveData<Resource<List<StationDeviceData>>>? = Transformations
        .switchMap(_stationDeviceList) { input ->
            stationDevicesRepository.insertStationDeviceList(_stationDeviceList.value!!)
        }


    var refreshToken: LiveData<Resource<AccessToken>>? = Transformations
        .switchMap(_accessToken) { accessToken ->
            if (accessToken == null) {
                AbsentLiveData.create()
            } else {
                accessTokenRepository.refreshToken(accessToken)
            }
        }


    fun setRefreshToken(accessToken: AccessToken?) {
        if (_accessToken.value != accessToken) {
            _accessToken.value = accessToken
        }
    }


    fun setLogin(email: String?, password: String, online : Boolean) {
        isOnline = online
        val update = LoginId(email, password)
        if (_loginId.value == update) {
            return
        }
        _loginId.value = update
    }

    fun onError() {
        val update = LoginId("", "")
        _loginId.value = update
    }

    data class LoginId(val email: String?, val password: String?) {
        fun <T> ifExists(f: (String, String) -> LiveData<T>): LiveData<T> {
            return if (email.isNullOrBlank() || password.isNullOrBlank()) {
                AbsentLiveData.create()
            } else {
                f(email!!, password!!)
            }
        }

    }


}
