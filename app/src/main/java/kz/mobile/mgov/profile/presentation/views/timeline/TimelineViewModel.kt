package kz.mobile.mgov.profile.presentation.views.timeline

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.Timeline

class TimelineViewModel(val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<TimelineInfo>()
    }
}

class TimelineViewModelFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TimelineViewModel(context) as T
    }
}

sealed class TimelineInfo() {
    object ShowLoading : TimelineInfo()
    object HideLoading : TimelineInfo()
    data class Result(val list: List<Timeline>?) : TimelineInfo()
    data class Error(val error: String) : TimelineInfo()
}