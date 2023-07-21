package org.southasia.ghru.ui.registerpatient_sg.scanqrcode.membersdialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.MembersDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import java.util.*
import javax.inject.Inject


class MembersDialogFragmentSG : DialogFragment(), Injectable {

    val TAG = MembersDialogFragmentSG::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<MembersDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)


    private var memberList: ArrayList<Member>? = null

    private var adapter by autoCleared<MembersDialogAdapterSG>()

    var householdId: String = ""

    var meta: Meta? = null

    var hoursFasted: String? = null

    var household: HouseholdRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberList = arguments?.getParcelableArrayList<Member>("memberList")
        householdId = arguments?.getString("householdId")!!
        meta = arguments?.getParcelable<Meta>("meta")!!
        hoursFasted = arguments?.getString("hours_fasted")
        household = arguments?.getParcelable("household")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<MembersDialogFragmentBinding>(
            inflater,
            R.layout.members_dialog_fragment,
            container,
            false
        )
        binding = dataBinding

        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //L.d("Shanuka")
        val adapter = MembersDialogAdapterSG(dataBindingComponent, appExecutors) { member ->
            dismiss()
            activity?.runOnUiThread({
                //  findNavController().navigate(R.id.action_scanCodeFragment_to_explanationFragment, bundleOf("member" to member, "householdId" to householdId, "hours_fasted" to hoursFasted, "meta" to meta, "household" to household))
                Navigation.findNavController(activity!!, R.id.container).navigate(
                    R.id.action_global_explanationFragment,
                    bundleOf(
                        "member" to member,
                        "householdId" to householdId,
                        "hours_fasted" to hoursFasted,
                        "meta" to meta,
                        "household" to household
                    )
                )
            })
        }
        adapter.submitList(memberList)
        this.adapter = adapter
        binding.membersList.adapter = adapter
        binding.membersList.setHasFixedSize(false);
        val linearLayoutManager = LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        binding.membersList.setLayoutManager(linearLayoutManager)

//        binding.buttonNewPaticipant.singleClick {
//            dismiss()
//            findNavController().navigate(R.id.action_scanCodeFragment_to_explanationFragment, bundleOf("householdId" to householdId, "hours_fasted" to hoursFasted, "meta" to meta, "household" to household))
//        }
        if (memberList?.count() == 0) {
            binding.textViewNoMemberFound.visibility = View.VISIBLE
        } else {
            binding.textViewNoMemberFound.visibility = View.GONE
        }

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
        dialog.setCancelable(false)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        //L.d("back")
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, TAG)
    }

}
