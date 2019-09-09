package kz.mobile.mgov.profile.presentation.auto

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
import com.google.gson.Gson
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


class AutoFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var sessionRepository: SessionRepository

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AutoViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var driverLicenses: RecyclerView
    private lateinit var carList: RecyclerView
    private lateinit var driverLicenseSwitchLabelView: SwitchLabelView
    private lateinit var notFoundTransportInformation: TextView
    private lateinit var notFoundDriverLicense: TextView
    private lateinit var carsSwitchLabelView: SwitchLabelView
    private lateinit var edsRequiredTv: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.title_auto))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        driverLicenses = findViewById(R.id.driver_licenses)
        notFoundDriverLicense = findViewById(R.id.not_found_driver_license_tv)
        carList = findViewById(R.id.car_list)
        notFoundTransportInformation = findViewById(R.id.not_found_transport_information_tv)
        driverLicenseSwitchLabelView = findViewById(R.id.driver_licenses_switch_view)
        driverLicenseSwitchLabelView.apply {
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_DRIVER_LICENSE)
                }
            })
        }


        carsSwitchLabelView = findViewById(R.id.cars_switch_label_view)
        carsSwitchLabelView.apply {
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_TRANSPORT)
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
        viewModel.getServiceStatus()

        viewModel.getDriverLicenses()
        viewModel.driverLicensesLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is AutoViewModel.DriverLicenseResult.ShowLoading -> {
                    showProgress()
                }
                is AutoViewModel.DriverLicenseResult.HideLoading -> {
                    dismissProgress()
                }
                is AutoViewModel.DriverLicenseResult.Result -> {
                    if (result.list != null && result.list.isNotEmpty()) {
                        val adapter = DriverLicensesAdapter(result.list)
                        driverLicenses.adapter = adapter
                        driverLicenses.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                        driverLicenses.visibility = View.VISIBLE
                        notFoundDriverLicense.visibility = View.GONE
                    } else {
                        driverLicenses.visibility = View.GONE
                        notFoundDriverLicense.visibility = if (driverLicenseSwitchLabelView.isChecked()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
                is AutoViewModel.DriverLicenseResult.Error -> {
                    driverLicenses.visibility = View.GONE
                    notFoundDriverLicense.visibility = if (driverLicenseSwitchLabelView.isChecked()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                is AutoViewModel.DriverLicenseResult.Status -> {
                    driverLicenseSwitchLabelView.setChecked(result.enable)
                }
            }
        })


        viewModel.getCarInfo()
        viewModel.carsLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is AutoViewModel.AutoResult.ShowLoading -> {
                    showProgress()
                }
                is AutoViewModel.AutoResult.HideLoading -> {
                    dismissProgress()
                }
                is AutoViewModel.AutoResult.Result -> {
                    if (result.list != null && result.list.isNotEmpty()) {
                        val adapter = CarsAdapter(R.layout.row_item_with_transition, result.list)
                        adapter.setOnItemClickListener { adapter, view, position ->
                            val data = Bundle().apply {
                                putString(IntentConstants.AUTO_INFO, gson.toJson(adapter.data[position]))
                                putString(Screen.SCREEN, Screen.AUTO_INFO)
                            }
                            ContainerActivity.start(
                                context = context, bundle = data, animationType = NavigationAnimation.SLIDE
                            )
                        }

                        carList.adapter = adapter
                        carList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                        carList.visibility = View.VISIBLE
                        notFoundTransportInformation.visibility = View.GONE
                    } else {
                        carList.visibility = View.GONE
                        notFoundTransportInformation.visibility = if (carsSwitchLabelView.isChecked()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
                is AutoViewModel.AutoResult.Error -> {
                    carList.visibility = View.GONE
                    notFoundTransportInformation.visibility = if (carsSwitchLabelView.isChecked()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                is AutoViewModel.AutoResult.Status -> {
                    carsSwitchLabelView.setChecked(result.enable)
                }
            }
        })
    }

    companion object {
        fun newInstance(data: Bundle? = null): AutoFragment {
            val fragment = AutoFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
