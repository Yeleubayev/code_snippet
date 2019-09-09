package kz.mobile.mgov.profile.presentation.profile_info

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.repository.user.ProfileData
import kz.mobile.mgov.common.repository.user.UserRepository
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.Address
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class ProfileInfoViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val userRepository: UserRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<Result>()
    }

    init {
        getLocalProfileData()
    }

    fun getAllData() {
        addDisposable(
            Single.zip(
                userServiceRepository.getAddresses(),
                userRepository.getLocalProfileData(),
                BiFunction { list: List<Address>, data: ProfileData? ->
                    Pair(list, data)
                }
            )
                .compose(schedulers.applySchedulersSingle())
                .subscribe(
                    { result ->
                        liveData.value = Result.AddressInfo(result.first)
                        liveData.value = Result.ProfileInfo(result.second)
                    },
                    { error -> })
        )
    }

    fun getLocalProfileData() =
        addDisposable(
            userRepository.getLocalProfileData()
                .compose(schedulers.applySchedulersSingle())
                .subscribe(
                    { result -> liveData.value = Result.ProfileInfo(result) },
                    { error ->
                        Log.d("error_data", error.toString())
                        error.printStackTrace()
                    })
        )

    fun getAddresses() = addDisposable(
        userServiceRepository.getAddresses()
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result -> },
                { error -> })
    )

    sealed class Result {
        object ShowLoading : Result()
        object HideLoading : Result()
        data class Error(val error: String) : Result()
        data class ProfileInfo(val profileData: ProfileData?) : Result()
        data class AddressInfo(val list: List<Address>) : Result()
    }
}