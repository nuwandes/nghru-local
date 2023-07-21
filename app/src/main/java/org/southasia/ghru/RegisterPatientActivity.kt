package org.southasia.ghru

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.pixplicity.easyprefs.library.Prefs
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.register_patient_activity.*
import org.southasia.ghru.util.LocaleManager
import javax.inject.Inject

class RegisterPatientActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_patient_activity)

        Log.d("REGISTER_PATIENT_FRAG","COUNTRY_IS: " + Prefs.getString("COUNTRY", null))

        setupNavigationByCountry(Prefs.getString("COUNTRY", null))

    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager(base).setLocale())
    }

    override fun onSupportNavigateUp(): Boolean {
        val currentDestination = Navigation.findNavController(this, R.id.container).currentDestination
        val parent = currentDestination?.parent
        if (parent == null || currentDestination.id != parent.id)
            super.onBackPressed()
        else
            onSupportNavigateUp()
        return true
    }

    private fun setupNavigationByCountry(countryCode: String) {

        val navHostFragment = container as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val nav1 = inflater.inflate(R.navigation.register_patient)
        val navSg = inflater.inflate(R.navigation.register_patient_sg)
        val navNewSg = inflater.inflate(R.navigation.register_patient_new)
        nav1.setDefaultArguments(intent.extras)
        navSg.setDefaultArguments(intent.extras)
        navNewSg.setDefaultArguments(intent.extras)

        if (countryCode == "SG")
        {
            navHostFragment.navController.graph = navNewSg
        }
        else
        {
            navHostFragment.navController.graph = nav1
        }
    }
}
