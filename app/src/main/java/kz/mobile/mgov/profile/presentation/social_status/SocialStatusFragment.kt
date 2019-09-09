package kz.mobile.mgov.profile.presentation.social_status

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
import android.widget.LinearLayout
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
import kz.mobile.mgov.common.widgets.SocialStatusView
import kz.mobile.mgov.common.widgets.SwitchLabelListener
import kz.mobile.mgov.common.widgets.SwitchLabelView
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RequestsConstants
import kz.mobile.mgov.profile.data.model.SocialStatus
import javax.inject.Inject


class SocialStatusFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionRepository: SessionRepository


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SocialStatusViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var socialStatusView: SocialStatusView
    private lateinit var switchLabelView: SwitchLabelView
    private lateinit var emptyListSocialStatus: LinearLayout
    private lateinit var edsRequiredTv: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_social_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        checkAuthLevel()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.title_social_status))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        socialStatusView = findViewById(R.id.socialStatusView)
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
        emptyListSocialStatus = findViewById(R.id.empty_list_social_status)
        edsRequiredTv = findViewById(R.id.eds_required_tv)

    }

    private fun checkAuthLevel() {
        if (sessionRepository.authLevel != AuthLevel.E_SIGN) {
            requireEds()
        } else {
            setData()
        }
    }

    private fun setData() {
        viewModel.getSocialStatus()
        viewModel.getServiceStatus()
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is SocialStatusViewModel.ResultData.ShowLoading -> {
                    showProgress()
                }
                is SocialStatusViewModel.ResultData.HideLoading -> {
                    dismissProgress()
                }
                is SocialStatusViewModel.ResultData.Result -> {
                    if (result.list != null && result.list.isNotEmpty()) {
                        val socialStatus = result.list.first()
                        emptyListSocialStatus.visibility = View.GONE
                        socialStatusView.visibility = View.VISIBLE
                        setSocialStatusInfo(socialStatusView, socialStatus)
                    } else {
                        emptyListSocialStatus.visibility = View.VISIBLE
                        socialStatusView.visibility = View.GONE
                    }

                }
                is SocialStatusViewModel.ResultData.Error -> {
                    socialStatusView.visibility = View.GONE
                    emptyListSocialStatus.visibility = if (switchLabelView.isChecked()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                is SocialStatusViewModel.ResultData.Status -> {
                    switchLabelView.setChecked(result.enable)
                }
            }
        })
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

    private fun setSocialStatusInfo(view: SocialStatusView, socialStatus: SocialStatus) {
        view.setSocialStatusData(
            statusName = socialStatus.title?.text,
            statusIssueDate = socialStatus.formattedIssueDate(),
            statusValidDate = socialStatus.formattedExpireDate()
        )
    }

    companion object {
        fun newInstance(data: Bundle? = null): SocialStatusFragment {
            val fragment = SocialStatusFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
