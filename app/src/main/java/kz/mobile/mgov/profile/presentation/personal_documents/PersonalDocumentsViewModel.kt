package kz.mobile.mgov.profile.presentation.personal_documents

import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.common.repository.user.UserRepository
import kz.mobile.mgov.document.data.DocumentInfo
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class PersonalDocumentsViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val userRepository: UserRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<ResultData>()
    }

    fun getDocuments() = addDisposable(
        userServiceRepository.getDocuments()
            .flatMap { list ->
                userRepository.getLocalProfileData().map { profile ->
                    list.map { it.userProfileData = profile }
                    list
                }
            }
            .compose(schedulers.applySchedulersSingle())
            .doOnSubscribe { liveData.value = ResultData.ShowLoading }
            .doFinally { liveData.value = ResultData.HideLoading }
            .subscribe(
                { result -> liveData.value = ResultData.Documents(result) },
                { error -> liveData.value = ResultData.Error(error.localizedMessage ?: "") }
            )
    )

    fun getServiceStatus() = addDisposable(
        userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_DOCUMENT)
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result ->
                    liveData.value = ResultData.Status(result.isEnable())
                },
                { error -> error.printStackTrace() }
            )
    )

    fun subscribeUnsubscribeSection(isSubscribe: Boolean) {
        val single = if (isSubscribe) {
            userServiceRepository.unsubscribeFromSection(section = SubscriptionCode.PP_DOCUMENT)
        } else {
            userServiceRepository.subscribeToSection(section = SubscriptionCode.PP_DOCUMENT)
        }
        addDisposable(
            single
                .flatMap { userServiceRepository.getDocuments() }
                .flatMap { list ->
                    userRepository.getLocalProfileData().map { profile ->
                        list.map { it.userProfileData = profile }
                        list
                    }
                }
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { liveData.value = ResultData.ShowLoading }
                .doFinally { liveData.value = ResultData.HideLoading }
                .subscribe(
                    { result -> liveData.value = ResultData.Documents(result) },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class ResultData() {
        object ShowLoading : ResultData()
        object HideLoading : ResultData()
        data class Documents(val list: List<DocumentInfo>) : ResultData()
        data class Status(val enable: Boolean) : ResultData()
        data class Error(val error: String) : ResultData()
    }
}
