package kz.mobile.mgov.profile.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.repository.SessionRepository
import kz.mobile.mgov.common.repository.user.ProfileData
import kz.mobile.mgov.common.repository.user.UserRepository
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import java.io.File
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<ProfileResult>()
    }

    init {
        getLocalProfileData()
        checkFirstLaunch()
    }

    fun getProfileData(refreshing: Boolean) {
        if (refreshing) {
            getProfileData()
        } else {
            getLocalProfileData()
        }
    }

    fun isAuthorized() = !userRepository.sso.isNullOrEmpty()

    fun isFirstLaunch(): Boolean {
        val firstLaunch = sessionRepository.isFirstLaunch
        if (firstLaunch) {
            sessionRepository.isFirstLaunch = false
        }
        return firstLaunch
    }

    fun checkFirstLaunch() {
        if (sessionRepository.isFirstLaunch) {
            liveData.value = ProfileResult.FirstLaunch
            sessionRepository.isFirstLaunch = false
        }
    }

    fun checkAuth() {
        liveData.value = ProfileResult.AuthorizedResult(isAuthorized = !userRepository.sso.isNullOrEmpty())
    }

    fun uploadAvatar(file: File) = addDisposable(
        userRepository.setAvatar(file)
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result -> liveData.value = ProfileResult.Result(result) },
                { error -> })
    )

    fun deleteAvatar() = addDisposable(
        userRepository.deleteAvatar()
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result -> liveData.value = ProfileResult.Result(result) },
                { error -> })
    )

    fun logout() {
        addDisposable(
            userRepository.updateCitizen(true)
                .flatMap {
                    Single.fromCallable {
                        sessionRepository.removeAllSensitiveData()
                    }
                }
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = ProfileResult.ShowLoading }
                .doFinally { liveData.value = ProfileResult.HideLoading }
                .subscribe(
                    { result -> liveData.value = ProfileResult.Logout },
                    { error -> ProfileResult.Error(error.localizedMessage) }
                )
        )
    }

    fun saveSubscriptionsStatusData() {
        addDisposable(
            userServiceRepository.getServiceSubscriptionStates()
                .compose(schedulers.applySchedulersCompletable())
                .subscribe(
                    {},
                    {}
                )
        )
    }

    private fun getProfileData() {
        val disposable = userRepository.getProfileData()
            .compose(schedulers.applySchedulersSingle())
            .doOnSubscribe { liveData.value = ProfileResult.ShowLoading }
            .doFinally { liveData.value = ProfileResult.HideLoading }
            .subscribe(
                { result -> liveData.value = ProfileResult.Result(result) },
                { error -> ProfileResult.Error(error.localizedMessage) })
        addDisposable(disposable)
    }

    private fun getLocalProfileData() {
        val disposable = userRepository.getLocalProfileData()
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result -> liveData.value = ProfileResult.Result(result) },
                { error ->
                    liveData.value = ProfileResult.Error(error.localizedMessage ?: "")
                    Log.d("error_data", error.toString())
                    error.printStackTrace()
                })
        addDisposable(disposable)
    }
}


sealed class ProfileResult {
    object ShowLoading : ProfileResult()
    object HideLoading : ProfileResult()
    object Logout : ProfileResult()
    object FirstLaunch : ProfileResult()
    data class AuthorizedResult(val isAuthorized: Boolean) : ProfileResult()
    data class Result(val profileData: ProfileData?) : ProfileResult()
    data class Error(val error: String) : ProfileResult()
}