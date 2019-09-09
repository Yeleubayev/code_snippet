package kz.mobile.mgov.profile.presentation.views.car_fines

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.TransportPenalty
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class CarFinesViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<TransportPenaltyInfo>()
    }

    fun getCarFinesInfo() {
        addDisposable(
            userServiceRepository.getCarFines()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { liveData.value = TransportPenaltyInfo.ShowLoading }
                .doFinally { liveData.value = TransportPenaltyInfo.HideLoading }
                .subscribe(
                    { result -> liveData.value = TransportPenaltyInfo.Result(result) },
                    { error -> liveData.value = TransportPenaltyInfo.Error(error.localizedMessage) })
        )
    }

    sealed class TransportPenaltyInfo() {
        object ShowLoading : TransportPenaltyInfo()
        object HideLoading : TransportPenaltyInfo()
        data class Result(val list: List<TransportPenalty>?) : TransportPenaltyInfo()
        data class Error(val error: String) : TransportPenaltyInfo()
    }
}
