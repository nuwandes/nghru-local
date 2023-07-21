package org.southasia.ghru.ui.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.AccessTokenRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.AccessToken
import org.southasia.ghru.vo.Resource
import javax.inject.Inject


class LogoutDialogViewModel
@Inject constructor(accessTokenRepository: AccessTokenRepository) : ViewModel() {
    private val _email = MutableLiveData<String>()
    private val _updateToken = MutableLiveData<AccessToken>()

    val accessToken: LiveData<Resource<AccessToken>>? = Transformations
        .switchMap(_email) { email ->
            if (email == null) {
                AbsentLiveData.create()
            } else {
                accessTokenRepository.getTokerByEmail(email)
            }
        }

    val accessTokenLogout: LiveData<Resource<AccessToken>>? = Transformations
            .switchMap(_updateToken) { updateToken ->
                if (updateToken == null) {
                    AbsentLiveData.create()
                } else {
                    accessTokenRepository.setLogout(updateToken)
                }
            }

    fun setEmail(email: String) {
        val update = email
        if (_email.value == update) {
            return
        }
        _email.value = update
    }

    fun setLogOut(accessToken: AccessToken) {
        val update = accessToken
        if (_updateToken.value == update) {
            return
        }
        _updateToken.value = update
    }
}
