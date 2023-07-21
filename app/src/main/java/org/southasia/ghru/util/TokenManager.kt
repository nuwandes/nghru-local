package org.southasia.ghru.util

import android.content.SharedPreferences
import com.pixplicity.easyprefs.library.Prefs
import org.southasia.ghru.vo.AccessToken
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenManager @Inject constructor(private val prefs: SharedPreferences) : Serializable {
    private val editor: SharedPreferences.Editor

    init {
        this.editor = prefs.edit()
    }

    fun saveToken(token: AccessToken) {
        //Timber.d("TokenManager$token")
        editor.putString("ACCESS_TOKEN", token.tokenType + " " + token.accessToken).commit()
        editor.putString("REFRESH_TOKEN", token.refreshToken).commit()
        editor.apply()
        Prefs.putString("ACCESS_TOKEN", token.tokenType + " " + token.accessToken)
        Prefs.putString("ACCESS_TOKEN_ONLY", token.accessToken)
    }

    fun saveEmail(email: String) {
        //Timber.d("TokenManager$token")
        editor.putString("EMAIL", email).commit()
        editor.apply()
    }

    fun getEmail(): String? {
        //Timber.d("TokenManager$token")
        return prefs.getString("EMAIL", null)
    }

    fun deleteToken() {
        editor.remove("ACCESS_TOKEN").commit()
        editor.remove("REFRESH_TOKEN").commit()
        editor.apply()
    }

    fun getToken(): AccessToken {
        val token = AccessToken()
        token.accessToken = prefs.getString("ACCESS_TOKEN", null)
        token.refreshToken = prefs.getString("REFRESH_TOKEN", null)
        return token
    }

}
