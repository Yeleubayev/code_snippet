package kz.mobile.mgov.profile.presentation.family

import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.Child
import kz.mobile.mgov.profile.data.model.Marriage
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class FamilyInfoViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val marriageInfoLiveData by lazy { MutableLiveData<MarriageInfo>() }
    val childrenInfoLiveData by lazy { MutableLiveData<ChildrenInfo>() }

    fun getServiceStatus() = addDisposable(
        userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_CHILDREN)
            .flatMap { childResult ->
                userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_MARRIAGE)
                    .map { marriageResult -> Pair(childResult, marriageResult) }
            }
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result ->
                    childrenInfoLiveData.value = ChildrenInfo.Status(result.first.isEnable())
                    marriageInfoLiveData.value = MarriageInfo.Status(result.second.isEnable())
                },
                { error -> error.printStackTrace() }
            )
    )

    fun getMarriageData() {
        addDisposable(
            userServiceRepository.getMarriageInfo()
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { marriageInfoLiveData.value = MarriageInfo.ShowLoading }
                .doFinally { marriageInfoLiveData.value = MarriageInfo.HideLoading }
                .subscribe({ result ->
                    marriageInfoLiveData.value = MarriageInfo.Result(
                        result.first,
                        result.second
                    )
                },
                    { error ->
                        marriageInfoLiveData.value =
                            MarriageInfo.Error(error.localizedMessage)
                    })
        )
    }


    fun getChildInfo() {
        addDisposable(
            userServiceRepository.getChildrenInfo()
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { childrenInfoLiveData.value = ChildrenInfo.ShowLoading }
                .doFinally { childrenInfoLiveData.value = ChildrenInfo.HideLoading }
                .subscribe(
                    { result -> childrenInfoLiveData.value = ChildrenInfo.Result(result) },
                    { error -> childrenInfoLiveData.value = ChildrenInfo.Error(error.localizedMessage) }
                )
        )
    }

    fun subscribeUnsubscribeSection(isSubscribe: Boolean, section: SubscriptionCode) {
        val single = if (isSubscribe) {
            userServiceRepository.unsubscribeFromSection(section = section)
        } else {
            userServiceRepository.subscribeToSection(section = section)
        }
        addDisposable(
            single
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { marriageInfoLiveData.value = MarriageInfo.ShowLoading }
                .doFinally { marriageInfoLiveData.value = MarriageInfo.HideLoading }
                .subscribe(
                    { result ->
                        when (section) {
                            SubscriptionCode.PP_CHILDREN -> {
                                getChildInfo()
                            }
                            SubscriptionCode.PP_MARRIAGE -> {
                                getMarriageData()
                            }
                            else -> {
                            }
                        }
                    },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class MarriageInfo() {
        object ShowLoading : MarriageInfo()
        object HideLoading : MarriageInfo()
        data class Status(val enable: Boolean) : MarriageInfo()
        data class Result(val updateDate: String, val marriage: Marriage?) : MarriageInfo()
        data class Error(val error: String) : MarriageInfo()
    }

    sealed class ChildrenInfo() {
        object ShowLoading : ChildrenInfo()
        object HideLoading : ChildrenInfo()
        data class Status(val enable: Boolean) : ChildrenInfo()
        data class Result(val list: List<Child>?) : ChildrenInfo()
        data class Error(val error: String) : ChildrenInfo()
    }
}


