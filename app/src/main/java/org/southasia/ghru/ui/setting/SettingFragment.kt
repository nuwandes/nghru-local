package org.southasia.ghru.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.MainActivity
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SettingFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.util.autoCleared
import javax.inject.Inject

class SettingFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager

    val LANGUAGE_ENGLISH = "en"
    val LANGUAGE_URDU = "ur"
    val LANGUAGE_HINDI = "hi"
    val LANGUAGE_BENGALI = "bn"


    var binding by autoCleared<SettingFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var settingViewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SettingFragmentBinding>(
            inflater,
            R.layout.setting_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        when (localeManager.getLanguage()) {
            LANGUAGE_ENGLISH -> binding.radioButtonEng.isChecked = true
            LANGUAGE_URDU -> binding.radioButtonUrdu.isChecked = true
            LANGUAGE_HINDI -> binding.radioButtonHindi.isChecked = true
            LANGUAGE_BENGALI -> binding.radioButtonBengali.isChecked = true

            else -> { // Note the block
                setNewLocale(LANGUAGE_ENGLISH, false)
            }
        }
        binding.radioGroup.setOnCheckedChangeListener({ _, i ->
            when (i) {
                R.id.radioButtonEng -> {
                    setNewLocale(LANGUAGE_ENGLISH, false)
                }

                R.id.radioButtonUrdu -> {
                    setNewLocale(LANGUAGE_URDU, false)
                }
                R.id.radioButtonHindi -> {
                    setNewLocale(LANGUAGE_HINDI, false)
                }

                R.id.radioButtonBengali -> {
                    setNewLocale(LANGUAGE_BENGALI, false)
                }
                else -> { // Note the block
                    setNewLocale(LANGUAGE_ENGLISH, false)
                }
            }
        })

    }

    private fun setNewLocale(language: String, restartProcess: Boolean): Boolean {
        localeManager.setNewLocale(language)
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        if (restartProcess) {
            System.exit(0)
        }
        return true
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
