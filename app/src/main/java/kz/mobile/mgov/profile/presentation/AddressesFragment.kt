package kz.mobile.mgov.profile.presentation

import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.custom_label_layout.view.*
import kotlinx.android.synthetic.main.fragment_addresses.*
import kotlinx.android.synthetic.main.fragment_addresses.view.*
import kz.mobile.mgov.R
import kz.mobile.mgov.auth.presentation.AuthActivity
import kz.mobile.mgov.common.AuthLevel
import kz.mobile.mgov.common.di.Injectable
import kz.mobile.mgov.common.extensions.closeWithAnimation
import kz.mobile.mgov.common.navigator.NavigationAnimation
import kz.mobile.mgov.common.navigator.Screen
import kz.mobile.mgov.common.presentation.ContainerActivity
import kz.mobile.mgov.common.presentation.base.BaseActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.repository.SessionRepository
import kz.mobile.mgov.common.widgets.SwitchLabelListener
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RequestsConstants
import kz.mobile.mgov.profile.data.model.Address
import kz.mobile.mgov.profile.presentation.views.address.AddressViewModel
import javax.inject.Inject

class AddressesFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionRepository: SessionRepository

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AddressViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_addresses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }

        switchLabelView.apply {
            setChecked(false)
            questionClick {
                val data = Bundle().apply {
                    putString(Screen.SCREEN, Screen.QUESTION_INFO)
                    putBoolean(IntentConstants.SET_CLOSE_ICON, true)
                    putString(IntentConstants.TITLE, getString(R.string.title_services_info))
                    putString(IntentConstants.DESCRIPTION, getString(R.string.services_info_text))
                }
                ContainerActivity.start(activity, bundle = data, animationType = NavigationAnimation.SLIDE_UP)
            }
            setSwitchListener(object : SwitchLabelListener {
                override fun onSwitchStateListener(active: Boolean) {
                    viewModel.subscribeUnsubscribeSection(active)
                }
            })
        }
    }

    private fun setData() {
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is AddressViewModel.AddressInfoResult.ShowLoading -> {
                    showProgress()
                }
                is AddressViewModel.AddressInfoResult.HideLoading -> {
                    dismissProgress()
                }
                is AddressViewModel.AddressInfoResult.Result -> {
                    result.list?.find { it.type == Address.TYPE_BIRTH_PLACE }?.let {
                        birthPlaceContainer.visibility = View.VISIBLE
                        birthCountryView.valueText = it.countryName?.text ?: ""
                        birthRegionView.valueText = it.regionName?.text ?: ""
                        birthCityView.valueText = it.city ?: ""
                    } ?: run { birthPlaceContainer.visibility = View.GONE }

                    result.list?.find { it.type == Address.TYPE_REG_ADDRESS }?.let {
                        registeredPlaceContainer.visibility = View.VISIBLE
                        countryView.valueText = it.countryName?.text ?: ""
                        regionView.valueText = it.regionName?.text ?: ""
                        cityView.valueText = it.city ?: ""
                        addressView.valueText = it.getFullAddress()
                    } ?: run { registeredPlaceContainer.visibility = View.GONE }
                }
            }
        })
    }

    companion object {
        fun newInstance(data: Bundle? = null): AddressesFragment {
            val fragment = AddressesFragment()
            fragment.arguments = data
            return fragment
        }
    }

    private fun checkAuthLevel() {
        if (sessionRepository.authLevel != AuthLevel.E_SIGN) {
            requireEds()
        } else {
            setData()
        }
    }

    private fun requireEds() {
        val titleText = getText(R.string.eds_require_part1) as SpannedString
        val annotations = titleText.getSpans(0, titleText.length, Annotation::class.java)
        val spannableText = SpannableString(titleText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                AuthActivity.start(
                    context = context,
                    requestCode = RequestsConstants.AUTH_REQUEST
                )
            }
        }
        for (annotation in annotations) {
            if (annotation.key == "click") {
                spannableText.setSpan(
                    clickableSpan, titleText.getSpanStart(annotation), titleText.getSpanEnd(annotation),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        eds_required_tv.movementMethod = LinkMovementMethod.getInstance()
        eds_required_tv.setText(spannableText, TextView.BufferType.SPANNABLE)
        eds_required_tv.visibility = View.VISIBLE
    }
}