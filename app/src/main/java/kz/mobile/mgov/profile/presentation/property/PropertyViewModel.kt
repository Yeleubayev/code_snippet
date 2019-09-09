package kz.mobile.mgov.profile.presentation.property

import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.Realty
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class PropertyViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<RealtyResult>()
    }

    fun getPropertyInfo() {
        addDisposable(
            userServiceRepository.getRealtyList()
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = RealtyResult.ShowLoading }
                .doFinally { liveData.value = RealtyResult.HideLoading }
                .subscribe(
                    { result -> liveData.value = RealtyResult.Result(result) },
                    { error -> liveData.value = RealtyResult.Error(error.localizedMessage) }
                )
        )
    }

    fun getServiceStatus() = addDisposable(
        userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_REALTY)
            .flatMap { childResult ->
                userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_ACCOMMODATION_QUEUE)
                    .map { marriageResult -> Pair(childResult, marriageResult) }
            }
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result ->
                    liveData.value = RealtyResult.RealtyStatus(result.first.isEnable())
                    liveData.value = RealtyResult.AccomodationStatus(result.second.isEnable())
                },
                { error -> error.printStackTrace() }
            )
    )

    fun subscribeUnsubscribeSection(isSubscribe: Boolean, section: SubscriptionCode) {
        val single = if (isSubscribe) {
            userServiceRepository.unsubscribeFromSection(section = section)
        } else {
            userServiceRepository.subscribeToSection(section = section)
        }
        addDisposable(
            single
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = RealtyResult.ShowLoading }
                .doFinally { liveData.value = RealtyResult.HideLoading }
                .subscribe(
                    { result ->
                        if (section == SubscriptionCode.PP_REALTY) {
                            getPropertyInfo()
                        }
                    },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class RealtyResult {
        object ShowLoading : RealtyResult()
        object HideLoading : RealtyResult()
        data class Result(val data: List<Realty>? = null) : RealtyResult()
        data class RealtyStatus(val status: Boolean) : RealtyResult()
        data class AccomodationStatus(val status: Boolean) : RealtyResult()
        data class Error(val error: String) : RealtyResult()
    }

}






