package kz.mobile.mgov.profile.presentation.auto

import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.Auto
import kz.mobile.mgov.profile.data.model.DriverLicense
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class AutoViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val driverLicensesLiveData by lazy {
        MutableLiveData<DriverLicenseResult>()
    }

    val carsLiveData by lazy {
        MutableLiveData<AutoResult>()
    }

    fun getDriverLicenses() {
        addDisposable(userServiceRepository.getDriverLicenses()
            .compose(schedulers.applySchedulersSingle())
            .doOnSubscribe { driverLicensesLiveData.value = DriverLicenseResult.ShowLoading }
            .doFinally { driverLicensesLiveData.value = DriverLicenseResult.HideLoading }
            .subscribe(
                { result -> driverLicensesLiveData.value = DriverLicenseResult.Result(result) },
                { error -> driverLicensesLiveData.value = DriverLicenseResult.Error(error.localizedMessage) }
            )
        )
    }

    fun getCarInfo() {
        addDisposable(
            userServiceRepository.getCarInfo()
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { carsLiveData.value = AutoResult.ShowLoading }
                .doFinally { carsLiveData.value = AutoResult.HideLoading }
                .subscribe(
                    { result -> carsLiveData.value = AutoResult.Result(result) },
                    { error -> carsLiveData.value = AutoResult.Error(error.localizedMessage) })
        )
    }

    fun getServiceStatus() = addDisposable(
        userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_DRIVER_LICENSE)
            .flatMap { childResult ->
                userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_TRANSPORT)
                    .map { marriageResult -> Pair(childResult, marriageResult) }
            }
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result ->
                    driverLicensesLiveData.value = DriverLicenseResult.Status(result.first.isEnable())
                    carsLiveData.value = AutoResult.Status(result.second.isEnable())
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
                .doOnSubscribe { driverLicensesLiveData.value = DriverLicenseResult.ShowLoading }
                .doFinally { driverLicensesLiveData.value = DriverLicenseResult.HideLoading }
                .subscribe(
                    { result ->
                        when (section) {
                            SubscriptionCode.PP_DRIVER_LICENSE -> {
                                getDriverLicenses()
                            }
                            SubscriptionCode.PP_TRANSPORT -> {
                                getCarInfo()
                            }
                            else -> {
                            }
                        }
                    },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class DriverLicenseResult() {
        object ShowLoading : DriverLicenseResult()
        object HideLoading : DriverLicenseResult()
        data class Result(val list: List<DriverLicense>?) : DriverLicenseResult()
        data class Status(val enable: Boolean) : DriverLicenseResult()
        data class Error(val error: String) : DriverLicenseResult()
    }

    sealed class AutoResult() {
        object ShowLoading : AutoResult()
        object HideLoading : AutoResult()
        data class Result(val list: List<Auto>?) : AutoResult()
        data class Status(val enable: Boolean) : AutoResult()
        data class Error(val error: String) : AutoResult()
    }
}



