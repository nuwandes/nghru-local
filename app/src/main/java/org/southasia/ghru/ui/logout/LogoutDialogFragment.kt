package org.southasia.ghru.ui.logout

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.async
import org.southasia.ghru.LoginActivity
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.LogoutDialogFragmentBinding
import org.southasia.ghru.db.AccessTokenDao
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.TokenManager
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import javax.inject.Inject


class LogoutDialogFragment : DialogFragment(), Injectable {

    val TAG = LogoutDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<LogoutDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var logoutdialogViewModel: LogoutDialogViewModel

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var accessTokenDao: AccessTokenDao


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<LogoutDialogFragmentBinding>(
            inflater,
            R.layout.logout_dialog_fragment,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.buttonAddMember.singleClick {
            dismiss()
        }

        logoutdialogViewModel.accessToken?.observe(this, Observer { accessToken ->


            if (accessToken?.status == Status.SUCCESS) {
                //println(user)
                val accessToken1 = accessToken.data!!
                accessToken1.status = false

//                async {
//                    accessTokenDao.logout(accessToken1)
//
//                }

                logoutdialogViewModel.setLogOut(accessToken1)
            } else if (accessToken?.status == Status.ERROR) {
                //Crashlytics.logException(Exception(accessToken.message?.message))

            }
        })



        logoutdialogViewModel.accessTokenLogout?.observe(this, Observer { accessToken ->


            if (accessToken?.status == Status.SUCCESS) {
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                dismiss()
                activity?.finish()
            } else if (accessToken?.status == Status.ERROR) {
                //Crashlytics.logException(Exception(accessToken.message?.message))
            }
        })

        binding.buttonAcceptAndContinue.singleClick {

            logoutdialogViewModel.setEmail(tokenManager.getEmail()!!)
//            var detete = async {
//                accessTokenDao.logout()
//            }

            tokenManager.deleteToken()

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // the content
        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // creating the fullscreen dialog
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, TAG)
    }

}
