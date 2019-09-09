package kz.mobile.mgov.profile.presentation.family

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
import kz.mobile.mgov.common.presentation.ParameterAdapter
import kz.mobile.mgov.common.presentation.Section
import kz.mobile.mgov.common.presentation.base.BaseActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.repository.SessionRepository
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.common.widgets.SwitchLabelListener
import kz.mobile.mgov.common.widgets.SwitchLabelView
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RequestsConstants
import kz.mobile.mgov.profile.data.model.Child
import kz.mobile.mgov.profile.data.model.Marriage
import javax.inject.Inject


class FamilyInfoFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val fullNameFormat = "%s %s %s"
    @Inject
    lateinit var sessionRepository: SessionRepository

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(FamilyInfoViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var userMarriageSwitchView: SwitchLabelView
    private lateinit var childrenInfoSwitchView: SwitchLabelView
    private lateinit var marriageDetails: RecyclerView
    private lateinit var partnerInfo: RecyclerView
    private lateinit var childList: RecyclerView
    private lateinit var partnerInfoTitle: TextView
    private lateinit var edsRequiredTv: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_family, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.family_title))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        userMarriageSwitchView = findViewById(R.id.user_marriage_switch_view)
        userMarriageSwitchView.apply {
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_MARRIAGE)
                }
            })
        }

        childrenInfoSwitchView = findViewById(R.id.children_info_switch_view)
        childrenInfoSwitchView.apply {
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_CHILDREN)
                }
            })
        }

        marriageDetails = findViewById(R.id.marriage_details)
        partnerInfo = findViewById(R.id.partner_info)
        childList = findViewById(R.id.child_list)
        partnerInfoTitle = findViewById(R.id.partner_info_title)
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
        viewModel.getMarriageData()
        viewModel.marriageInfoLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is FamilyInfoViewModel.MarriageInfo.ShowLoading -> {
                    showProgress()
                }
                is FamilyInfoViewModel.MarriageInfo.HideLoading -> {
                    dismissProgress()
                }
                is FamilyInfoViewModel.MarriageInfo.Result -> {
                    if (result.marriage != null) {
                        setMarriageInfo(result.marriage)
                    } else {
                        marriageDetails.visibility = View.GONE
                        partnerInfo.visibility = View.GONE
                        partnerInfoTitle.visibility = View.GONE
                    }
                }
                is FamilyInfoViewModel.MarriageInfo.Error -> {
                    marriageDetails.visibility = View.GONE
                    partnerInfo.visibility = View.GONE
                    partnerInfoTitle.visibility = View.GONE
                }
                is FamilyInfoViewModel.MarriageInfo.Status -> {
                    userMarriageSwitchView.setChecked(result.enable)
                }
            }
        })

        viewModel.getChildInfo()
        viewModel.childrenInfoLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is FamilyInfoViewModel.ChildrenInfo.ShowLoading -> {
                    showProgress()
                }
                is FamilyInfoViewModel.ChildrenInfo.HideLoading -> {
                    dismissProgress()
                }
                is FamilyInfoViewModel.ChildrenInfo.Result -> {
                    if (result.list != null && result.list.isNotEmpty()) {
                        setChildrenInfo(result.list)
                    } else {
                        childList.visibility = View.GONE
                    }
                }
                is FamilyInfoViewModel.ChildrenInfo.Error -> {
                    childList.visibility = View.GONE
                }
                is FamilyInfoViewModel.ChildrenInfo.Status -> {
                    childrenInfoSwitchView.setChecked(result.enable)
                }
            }
        })
    }

    private fun setMarriageInfo(marriage: Marriage) {
        val parameters = ArrayList<Section>()
        if (marriage.zagsName.ruText != null) {
            parameters.add(Section(true, getString(R.string.title_department_name)))
            parameters.add(Section(marriage.zagsName.ruText))
        }
        parameters.add(Section(true, getString(R.string.title_act_number)))
        parameters.add(Section(marriage.actNumber))
        parameters.add(Section(true, getString(R.string.title_registration_date)))
        parameters.add(Section(marriage.getParsedRegistrationDate()))

        val adapter = ParameterAdapter(R.layout.row_item_simple_text, R.layout.row_item_header, parameters)
        marriageDetails.adapter = adapter
        marriageDetails.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        marriageDetails.visibility = View.VISIBLE
        val partnerParameters = ArrayList<Section>()

        partnerParameters.add(Section(true, getString(R.string.title_partner_iin)))
        partnerParameters.add(Section(marriage.partnerIin))
        partnerParameters.add(Section(true, getString(R.string.title_partner_fio)))
        partnerParameters.add(
            Section(
                String.format(
                    fullNameFormat,
                    marriage.partnerLastname,
                    marriage.partnerFirstname,
                    marriage.partnerPatronymic
                )
            )
        )
        val partnerInfoAdapter =
            ParameterAdapter(R.layout.row_item_simple_text, R.layout.row_item_header, partnerParameters)
        partnerInfo.adapter = partnerInfoAdapter
        partnerInfo.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        partnerInfoTitle.visibility = View.VISIBLE
        partnerInfo.visibility = View.VISIBLE
    }


    private fun setChildrenInfo(children: List<Child>) {
        val adapter = ChildrenAdapter(R.layout.row_item_child, children)
        childList.adapter = adapter
        childList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        childList.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance(data: Bundle? = null): FamilyInfoFragment {
            val fragment = FamilyInfoFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
