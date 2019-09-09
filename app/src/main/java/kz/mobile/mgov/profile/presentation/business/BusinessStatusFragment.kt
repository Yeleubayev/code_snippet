package kz.mobile.mgov.profile.presentation.business

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
import kz.mobile.mgov.profile.data.model.IndividualEntrepreneur
import kz.mobile.mgov.profile.data.model.LegalEntity
import javax.inject.Inject

class BusinessStatusFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var sessionRepository: SessionRepository
    private lateinit var edsRequiredTv: TextView


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(BusinessStatusViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var entrepreneurInfoSwitchLabelView: SwitchLabelView
    private lateinit var entrepreneurInfo: RecyclerView
    private lateinit var parameterAdapter: ParameterAdapter
    private lateinit var notEntrepreneurTv: TextView
    private lateinit var notMemberOfLegalEntitiesTv: TextView
    private lateinit var legalEntitiesSwitchLabelView: SwitchLabelView
    private lateinit var legalEntityList: RecyclerView
    private lateinit var legalEntityInfo: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_businessman_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.business))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        entrepreneurInfo = findViewById(R.id.entrepreneur_info)
        notEntrepreneurTv = findViewById(R.id.not_entrepreneur_tv)
        notMemberOfLegalEntitiesTv = findViewById(R.id.not_member_of_legal_entities_tv)
        legalEntityList = findViewById(R.id.legal_entity_list)
        legalEntityInfo = findViewById(R.id.legal_entity_info)

        entrepreneurInfoSwitchLabelView = findViewById(R.id.entrepreneur_info_switch_label_view)
        entrepreneurInfoSwitchLabelView.apply {
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_INDIVIDUAL_ENTREPRENEUR)
                }
            })
        }
        legalEntitiesSwitchLabelView = findViewById(R.id.legal_entities_switch_label_view)
        legalEntitiesSwitchLabelView.apply {
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
                    viewModel.subscribeUnsubscribeSection(active, SubscriptionCode.PP_LEGAL_PARTICIPANT)
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
        viewModel.getBusinessmanInfo()
        viewModel.entrepreneurStatusLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is BusinessStatusViewModel.BusinessmanResult.ShowLoading -> {
                    showProgress()
                }
                is BusinessStatusViewModel.BusinessmanResult.HideLoading -> {
                    dismissProgress()
                }
                is BusinessStatusViewModel.BusinessmanResult.Result -> {
                    if (result.individualEntrepreneur != null) {
                        setEntrepreneurInfo(result.individualEntrepreneur)
                        entrepreneurInfo.visibility = View.VISIBLE
                        notEntrpreneurTextVisibility()
                    } else {
                        entrepreneurInfo.visibility = View.GONE
                        notEntrpreneurTextVisibility()
                    }
                }
                is BusinessStatusViewModel.BusinessmanResult.Error -> {
                    entrepreneurInfo.visibility = View.GONE
                    notEntrpreneurTextVisibility()
                }
                is BusinessStatusViewModel.BusinessmanResult.Status -> {
                    legalEntitiesSwitchLabelView.setChecked(result.status)
                    notEntrpreneurTextVisibility()
                }
            }
        })

        viewModel.getLegalEntityParticipant()
        viewModel.legalEntitiesLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is BusinessStatusViewModel.LegalEntityResult.ShowLoading -> {
                    showProgress()
                }
                is BusinessStatusViewModel.LegalEntityResult.HideLoading -> {
                    dismissProgress()
                }
                is BusinessStatusViewModel.LegalEntityResult.Result -> {
                    if (result.list != null && result.list.isNotEmpty()) {
                        if (result.list.size > 1) {
                            setLegalEntities(result.list)
                        } else {
                            setLegalEntityInfo(result.list[0])
                        }
                        notMemberOfLegalEntitiesTv.visibility = View.GONE
                    } else {
                        legalEntityInfo.visibility = View.GONE
                        legalEntityList.visibility = View.GONE
                        notLegalEntityTextVisibility()
                    }
                }
                is BusinessStatusViewModel.LegalEntityResult.Error -> {
                    legalEntityInfo.visibility = View.GONE
                    legalEntityList.visibility = View.GONE
                    notLegalEntityTextVisibility()
                }
                is BusinessStatusViewModel.LegalEntityResult.Status -> {
                    legalEntitiesSwitchLabelView.setChecked(result.status)
                    notEntrpreneurTextVisibility()
                }
            }
        })
    }

    private fun setEntrepreneurInfo(individualEntrepreneur: IndividualEntrepreneur) {
        if (individualEntrepreneur.activityList.isNotEmpty()) {
            val sectionList = ArrayList<Section>()
            sectionList.add(Section(true, getString(R.string.activities)))
            for (i in individualEntrepreneur.activityList) {
                sectionList.add(Section(i.activityName?.ruText))
            }

            sectionList.add(Section(true, getString(R.string.title_certificate_issue_date_row)))
            sectionList.add(Section(individualEntrepreneur.parsedDeliverDate()))
            sectionList.add(
                Section(true, getString(R.string.title_registration_account_statement_date_row))
            )
            sectionList.add(Section(individualEntrepreneur.parsedRegistrationDate()))
            sectionList.add(
                Section(
                    true,
                    getString(R.string.title_individual_entrepreneur_registration_place_row)
                )
            )
            sectionList.add(Section(individualEntrepreneur.address.trim()))

            parameterAdapter =
                ParameterAdapter(R.layout.row_item_simple_text, R.layout.row_item_header, sectionList)
            entrepreneurInfo.adapter = parameterAdapter
            entrepreneurInfo.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        }
    }

    private fun setLegalEntities(legalEntities: List<LegalEntity>) {
        val adapter = LegalEntitiesAdapter(R.layout.row_item_with_transition, legalEntities)
        adapter.setOnItemClickListener { adapter, view, position ->
            val data = Bundle().apply {
                putString(IntentConstants.LEGAL_ENTITY_INFO, gson.toJson(adapter.data[position]))
                putString(Screen.SCREEN, Screen.LEGAL_ENTITY_INFO)
            }
            ContainerActivity.start(
                context = context, bundle = data, animationType = NavigationAnimation.SLIDE
            )
        }
        legalEntityList.adapter = adapter
        legalEntityList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        legalEntityList.visibility = View.VISIBLE

    }

    private fun setLegalEntityInfo(legalEntity: LegalEntity) {
        val legalEntityParameters = ArrayList<Section>()
        if (legalEntity.name?.ruText != null) {
            legalEntityParameters.add(Section(true, getString(R.string.title)))
            legalEntityParameters.add(Section(legalEntity.name.ruText))
        }

        legalEntityParameters.add(Section(true, getString(R.string.title_bin)))
        legalEntityParameters.add(Section(legalEntity.bin))

        legalEntityParameters.add(Section(true, getString(R.string.role)))
        legalEntityParameters.add(Section(legalEntity.participantType))
        val adapter = ParameterAdapter(R.layout.row_item_simple_text, R.layout.row_item_header, legalEntityParameters)
        legalEntityInfo.adapter = adapter
        legalEntityInfo.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        legalEntityInfo.visibility = View.VISIBLE
    }

    private fun notEntrpreneurTextVisibility() {
        notEntrepreneurTv.visibility = if (entrepreneurInfoSwitchLabelView.isChecked()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun notLegalEntityTextVisibility() {
        notMemberOfLegalEntitiesTv.visibility = if (legalEntitiesSwitchLabelView.isChecked()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {
        fun newInstance(data: Bundle? = null): BusinessStatusFragment {
            val fragment = BusinessStatusFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
