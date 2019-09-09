package kz.mobile.mgov.profile.presentation.profile_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.redmadrobot.inputmask.helper.Mask
import com.redmadrobot.inputmask.model.CaretString
import kotlinx.android.synthetic.main.fragment_profile_info.*
import kz.mobile.mgov.R
import kz.mobile.mgov.common.di.Injectable
import kz.mobile.mgov.common.presentation.base.BaseActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.egov_android.utils.PhoneMask
import javax.inject.Inject

class ProfileInfoFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ProfileInfoViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(view)
        setData()
    }

    private fun setupToolbar(view: View) = view.findViewById<MGovToolbar>(R.id.mGovToolbar).apply {
        toolbar.setNavigationOnClickListener {
            (activity as BaseActivity).onBackPressed()
        }
    }

    private fun setData() {
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ProfileInfoViewModel.Result.ShowLoading -> {
                }
                is ProfileInfoViewModel.Result.HideLoading -> {
                }
                is ProfileInfoViewModel.Result.ProfileInfo -> result.profileData?.let {
                    dateOfBirthView.valueText = it.birthday ?: ""
                    genderView.valueText = it.gender?.text ?: ""
                    citizenshipView.valueText = it.citizenship?.text ?: ""
                    emailView.valueText = it.email ?: ""

                    it.phone?.let { phone ->
                        val mask = Mask(PhoneMask.PHONE_MASK_KZ)
                        val newPhone = mask.apply(CaretString(phone, phone.length), true).formattedText.string
                        phoneNumberView.valueText = newPhone
                    }
                }
                is ProfileInfoViewModel.Result.AddressInfo -> {

                }
                is ProfileInfoViewModel.Result.Error -> {

                }
            }
        })
    }

    companion object {
        fun newInstance(data: Bundle? = null): ProfileInfoFragment {
            val fragment = ProfileInfoFragment()
            fragment.arguments = data
            return fragment
        }
    }
}