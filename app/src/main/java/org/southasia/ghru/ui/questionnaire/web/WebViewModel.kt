package org.southasia.ghru.ui.questionnaire.web

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.nuvoair.sdk.launcher.NuvoairLauncherMeasurement
import org.southasia.ghru.repository.QuestionnaireRepository
import org.southasia.ghru.repository.SurveyRepository
import org.southasia.ghru.repository.UserRepository
import org.southasia.ghru.ui.spirometry.tests.TestModel
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class WebViewModel
@Inject constructor(
    surveyRepository: SurveyRepository,
    userRepository: UserRepository,
    questionnaireRepository: QuestionnaireRepository
) : ViewModel() {

    private val _survey: MutableLiveData<QuestionMeta> = MutableLiveData()

    var survey: LiveData<Resource<ResourceData<CommonResponce>>>? = Transformations
        .switchMap(_survey) { _surveyValue ->
            if (_surveyValue == null) {
                AbsentLiveData.create()
            } else {
                surveyRepository.syncSurvey(_surveyValue)
            }
        }

    fun setSurvey(json: QuestionMeta) {
        _survey.postValue(json)
    }

    private val _email = MutableLiveData<String>()

    val user: LiveData<Resource<User>>? = Transformations
        .switchMap(_email) { emailx ->
            if (emailx == null) {
                AbsentLiveData.create()
            } else {
                userRepository.loadUserDB()
            }
        }


    fun setUser(email: String?) {
        if (_email.value != email) {
            _email.value = email
        }
    }

    private val _language = MutableLiveData<QuestionnaireId>()

    val language: LiveData<Resource<Questionnaire>>? = Transformations
        .switchMap(_language) { input ->
            input.ifExists { language, network ->
                questionnaireRepository.getQuestionnaire(language = language, network = network)
            }
        }


    fun getQuestionnaire(
        language: String?,
        network: Boolean?
    ) {
        val update =
            QuestionnaireId(language, network)
        if (_language.value == update) {
            return
        }
        _language.value = update
    }


    data class QuestionnaireId(
        val language: String?,
        val network: Boolean?
    ) {
        fun <T> ifExists(f: (String, Boolean) -> LiveData<T>): LiveData<T> {
            return if (language == null || network == null) {
                AbsentLiveData.create()
            } else {
                f(language!!, network!!)
            }
        }
    }


    //   var member = Member("Faruk Hasan", "Neyamot Ullah", "Ullah", "Male", true, "1254896", "25", "2 May 1980, 38 years", true, true, false, "")

}
