package kz.mobile.mgov.profile.presentation.views.line_to_home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.AccommodationQueue

class LineToHomeViewModel(private val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<AccommodationQueueInfo>()
    }
}

sealed class AccommodationQueueInfo() {
    data class Result(val list: List<AccommodationQueue>?) : AccommodationQueueInfo()
    data class Error(val error: String) : AccommodationQueueInfo()
    object ShowLoading : AccommodationQueueInfo()
    object HideLoading : AccommodationQueueInfo()
}