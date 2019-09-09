package kz.mobile.mgov.profile.presentation.property

import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kz.mobile.mgov.R
import kz.mobile.mgov.auth.presentation.AuthActivity
import kz.mobile.mgov.common.AuthLevel
import kz.mobile.mgov.common.di.Injectable
import kz.mobile.mgov.common.extensions.closeWithAnimation
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.common.navigator.NavigationAnimation
import kz.mobile.mgov.common.navigator.Screen
import kz.mobile.mgov.common.presentation.ContainerActivity
import kz.mobile.mgov.common.presentation.base.BaseActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.repository.SessionRepository
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.common.widgets.SwitchLabelListener
import kz.mobile.mgov.common.widgets.SwitchLabelView
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RequestsConstants
import javax.inject.Inject


class PropertyFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionRepository: SessionRepository

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PropertyViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var propertyList: RecyclerView
    private lateinit var accommodationQueueSwitchView: SwitchLabelView
    private lateinit var notFoundProperty: TextView
    private lateinit var propertiesSwitchLabelView: SwitchLabelView
    private lateinit var edsRequiredTv: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_property, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.property))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        propertyList = findViewById(R.id.property_list)
        notFoundProperty = findViewById(R.id.not_found_property)

        accommodationQueueSwitchView = findViewById(R.id.accommodation_queue_switch_view)
        accommodationQueueSwitchView.apply {
            enableSwitch(true)
            setChecked(false)
            questionClick {
                val data = Bundle().apply {
                    putString(Screen.SCREEN, Screen.QUESTION_INFO)
                    putBoolean(IntentConstants.SET_CLOSE_ICON, true)
                }
                ContainerActivity.start(activity, bundle = data, animationType = NavigationAnimation.SLIDE_UP)
            }
            setSwitchListener(object : SwitchLabelListener {
                override fun onSwitchStateListener(active: Boolean) {
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_ACCOMMODATION_QUEUE)
                }
            })
        }

        propertiesSwitchLabelView = findViewById(R.id.properties_switch_label_view)
        propertiesSwitchLabelView.apply {
            enableSwitch(true)
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_REALTY)
                }
            })
        }
        edsRequiredTv = findViewById(R.id.eds_required_tv)

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
        edsRequiredTv.movementMethod = LinkMovementMethod.getInstance()
        edsRequiredTv.setText(spannableText, TextView.BufferType.SPANNABLE)
        edsRequiredTv.visibility = View.VISIBLE
    }

    private fun setData() {
        viewModel.getPropertyInfo()
        viewModel.getServiceStatus()
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is PropertyViewModel.RealtyResult.ShowLoading -> {
                    showProgress()
                }
                is PropertyViewModel.RealtyResult.HideLoading -> {
                    dismissProgress()
                }
                is PropertyViewModel.RealtyResult.Result -> {
                    if (result.data != null && result.data.isNotEmpty()) {
                        val adapter = PropertyAdapter(R.layout.row_item_property, result.data)
                        propertyList.adapter = adapter
                        propertyList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                        propertyList.visibility = View.VISIBLE
                        notFoundProperty.visibility = View.GONE
                    } else {
                        propertyList.visibility = View.GONE
                        notFoundProperty.visibility = View.VISIBLE
                    }
                }
                is PropertyViewModel.RealtyResult.Error -> {
                    propertyList.visibility = View.GONE
                    notFoundProperty.visibility = if (propertiesSwitchLabelView.isChecked()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                is PropertyViewModel.RealtyResult.RealtyStatus -> {
                    propertiesSwitchLabelView.setChecked(result.status)
                }
                is PropertyViewModel.RealtyResult.AccomodationStatus -> {
                    accommodationQueueSwitchView.setChecked(result.status)
                }
            }
        })
    }

    companion object {
        fun newInstance(data: Bundle? = null): PropertyFragment {
            val fragment = PropertyFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
