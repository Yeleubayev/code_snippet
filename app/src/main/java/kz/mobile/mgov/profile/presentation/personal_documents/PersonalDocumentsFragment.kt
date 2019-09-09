package kz.mobile.mgov.profile.presentation.personal_documents

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
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.common.widgets.PersonalDocumentView
import kz.mobile.mgov.common.widgets.SwitchLabelListener
import kz.mobile.mgov.common.widgets.SwitchLabelView
import kz.mobile.mgov.document.data.DocumentInfo
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RequestsConstants
import javax.inject.Inject

class PersonalDocumentsFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionRepository: SessionRepository

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PersonalDocumentsViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var passportView: PersonalDocumentView
    private lateinit var identityCardView: PersonalDocumentView
    private lateinit var switchLabelView: SwitchLabelView
    private lateinit var edsRequiredTv: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.personal_documents_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.personal_documents_data))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        passportView = findViewById(R.id.passportView)
        identityCardView = findViewById(R.id.identityCardView)
        switchLabelView = findViewById(R.id.switchLabelView)

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

        passportView.setProgressBackground(R.drawable.bg_seek_bar)
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
        viewModel.getDocuments()
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is PersonalDocumentsViewModel.ResultData.ShowLoading -> {
                    showProgress()
                }
                is PersonalDocumentsViewModel.ResultData.HideLoading -> {
                    dismissProgress()
                }
                is PersonalDocumentsViewModel.ResultData.Documents -> {
                    if (result.list.isEmpty()) {
                        identityCardView.visibility = View.GONE
                        passportView.visibility = View.GONE
                    } else if (result.list.size == 1) {
                        val document = result.list.first()
                        identityCardView.visibility = View.GONE
                        passportView.visibility = View.VISIBLE
                        setDocumentInfo(passportView, document)
                    } else {
                        identityCardView.visibility = View.VISIBLE
                        passportView.visibility = View.VISIBLE
                        setDocumentInfo(passportView, result.list.first())
                        setDocumentInfo(identityCardView, result.list[1])
                    }
                }
                is PersonalDocumentsViewModel.ResultData.Status -> {
                    switchLabelView.setChecked(result.enable)
                }
                is PersonalDocumentsViewModel.ResultData.Error -> {
                }
            }
        })
    }

    private fun setDocumentInfo(view: PersonalDocumentView, document: DocumentInfo) {
        view.setDocumentData(
            documentName = document.documentTypeName?.text,
            documentIssueDate = document.getParsedBeginDate(),
            documentValidDate = document.getParsedEndDate(),
            documentIssueOrganization = document.issueOrganizationName?.text,
            documentOwnerNumber = document.number,
            documentOwnerCitizenship = document.userProfileData?.citizenship?.text,
            documentOwnerName = document.userProfileData?.fullName
        )
    }

    companion object {
        fun newInstance(data: Bundle? = null): PersonalDocumentsFragment {
            val fragment = PersonalDocumentsFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
