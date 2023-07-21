package org.southasia.ghru

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.southasia.ghru.ui.questionnaire.cancel.CancelDialogFragment
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class WebViewActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_activity)

    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onSupportNavigateUp(): Boolean {

        Log.d("WEB_VIEW_ACTIVITY", "CLOSE_BTN:")

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)


         builder.setTitle("Confirmation")
        builder.setMessage(getString(R.string.questionnariy_exit_message))
        builder.setIcon(getDrawable(R.drawable.ic_circular_cross))


        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE ->
                {
                    val currentDestination = Navigation.findNavController(this, R.id.container).currentDestination
                    val parent = currentDestination?.parent
                    if (parent == null || currentDestination.id != parent.id)
                        super.onBackPressed()
                    else
                        onSupportNavigateUp()

                }
                DialogInterface.BUTTON_NEGATIVE ->
                {
                    dialog.dismiss()
                }
            }
        }

        builder.setPositiveButton(getString(R.string.app_yes),dialogClickListener)
        builder.setNegativeButton(getString(R.string.app_no),dialogClickListener)

        dialog = builder.create()

        dialog.show()

        return true
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager(base).setLocale())
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}