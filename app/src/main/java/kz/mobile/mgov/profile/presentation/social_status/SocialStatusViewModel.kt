package kz.mobile.mgov.profile.presentation.social_status

import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.SocialStatus
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class SocialStatusViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<ResultData>()
    }

    fun getSocialStatus() {
        addDisposable(
            userServiceRepository.getSocialStatusList()
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = ResultData.ShowLoading }
                .doFinally { liveData.value = ResultData.HideLoading }
                .subscribe(
                    { result -> liveData.value = ResultData.Result(result) },
                    { error -> liveData.value = ResultData.Error(error.localizedMessage) }
                )
        )
    }

    fun getServiceStatus() = addDisposable(
        userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_SOCIAL_STATUS)
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result -> liveData.value = ResultData.Status(result.isEnable()) },
                { error -> error.printStackTrace() }
            )
    )

    fun subscribeUnsubscribeSection(isSubscribe: Boolean) {
        val single = if (isSubscribe) {
            userServiceRepository.unsubscribeFromSection(section = SubscriptionCode.PP_SOCIAL_STATUS)
        } else {
            userServiceRepository.subscribeToSection(section = SubscriptionCode.PP_SOCIAL_STATUS)
        }
        addDisposable(
            single
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = ResultData.ShowLoading }
                .doFinally { liveData.value = ResultData.HideLoading }
                .subscribe(
                    { result -> getSocialStatus() },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class ResultData() {
        object ShowLoading : ResultData()
        object HideLoading : ResultData()
        data class Result(val list: List<SocialStatus>?) : ResultData()
        data class Status(val enable: Boolean) : ResultData()
        data class Error(val error: String) : ResultData()
    }
}

