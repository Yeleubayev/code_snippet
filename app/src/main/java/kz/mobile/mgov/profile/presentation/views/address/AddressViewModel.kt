package kz.mobile.mgov.profile.presentation.views.address

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.Address
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import kz.mobile.mgov.profile.presentation.auto.AutoViewModel
import javax.inject.Inject

class AddressViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<AddressInfoResult>()
    }

    fun getAddresses() {
        val disposable = userServiceRepository.getAddresses()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { liveData.value = AddressInfoResult.ShowLoading }
            .doFinally { liveData.value = AddressInfoResult.HideLoading }
            .subscribe(
                { result -> liveData.value = AddressInfoResult.Result(result) },
                { error -> liveData.value = AddressInfoResult.Error(error.localizedMessage) })
        addDisposable(disposable)
    }

    fun subscribeUnsubscribeSection(isSubscribe: Boolean) {
        val single = if (isSubscribe) {
            userServiceRepository.unsubscribeFromSection(section = SubscriptionCode.PP_ADDRESS)
        } else {
            userServiceRepository.subscribeToSection(section = SubscriptionCode.PP_ADDRESS)
        }
        addDisposable(
            single
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = AddressInfoResult.ShowLoading }
                .doFinally { liveData.value = AddressInfoResult.HideLoading }
                .subscribe(
                    { result -> getAddresses() },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class AddressInfoResult {
        object ShowLoading : AddressInfoResult()
        object HideLoading : AddressInfoResult()
        data class Result(val list: List<Address>?) : AddressInfoResult()
        data class Error(val error: String) : AddressInfoResult()
    }
}


