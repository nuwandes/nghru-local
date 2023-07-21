package org.southasia.ghru.ui.enumeration

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.BodyMeasurementsActivity
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.EnumerationFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import javax.inject.Inject

class EnumerationFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<EnumerationFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<EnumerationFragmentBinding>(
            inflater,
            R.layout.enumeration_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navController = Navigation.findNavController(requireActivity(), R.id.enumeration_nav_fragment)

        updateBottomView(binding.buttonHome)

        binding.buttonStation.singleClick {
            val intent = Intent(activity, BodyMeasurementsActivity::class.java)
            startActivity(intent)
        }
        binding.buttonHome.singleClick {
            navController.navigate(R.id.action_global_home);
            updateBottomView(binding.buttonHome)
        }
    }


    fun updateBottomView(button: Button) {
        DrawableCompat.setTint(
            binding.buttonHome.getCompoundDrawables()[1],
            ContextCompat.getColor(activity!!.applicationContext, R.color.light_gray)
        );
        binding.buttonHome.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))

        DrawableCompat.setTint(
            binding.buttonStation.getCompoundDrawables()[1],
            ContextCompat.getColor(activity!!.applicationContext, R.color.light_gray)
        );
        binding.buttonStation.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))
        DrawableCompat.setTint(
            binding.buttonDevice.getCompoundDrawables()[1],
            ContextCompat.getColor(activity!!.applicationContext, R.color.light_gray)
        );
        binding.buttonDevice.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))
        DrawableCompat.setTint(
            binding.buttonMore.getCompoundDrawables()[1],
            ContextCompat.getColor(activity!!.applicationContext, R.color.light_gray)
        );
        binding.buttonMore.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))
        DrawableCompat.setTint(
            button.getCompoundDrawables()[1],
            ContextCompat.getColor(activity!!.applicationContext, R.color.colorPrimary)
        );
        button.setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))

    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
