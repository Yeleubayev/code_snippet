package kz.mobile.mgov.profile.presentation.views.interesting_services

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chad.library.adapter.base.entity.SectionEntity
import com.google.gson.internal.LinkedHashTreeMap
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.RecommenderInfo

class InterestingServicesViewModel(private val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<ServicesInfoResult>()
    }

    private var currentKey = 0

    private fun createHeaderSections(list: List<RecommenderInfo>): List<SectionHeader> {
        val headers = LinkedHashSet<SectionHeader>()
        val headerMap = LinkedHashTreeMap<RecommenderInfo, LinkedHashSet<RecommenderInfo.ServicesInfo>>()
        list.sortedDescending().forEach { item ->
            val hashSet = LinkedHashSet<RecommenderInfo.ServicesInfo>()
            item.serviceList.forEach { hashSet.add(it) }
            headerMap[item] = hashSet
        }
        for (key in headerMap.keys) {
            val item = SectionHeader(isHeader = true, header = key.message?.text ?: "", isMore = false)
            if (!headers.contains(item)) {
                headers.add(item)
            }
            headerMap.get(key)?.forEach {
                headers.add(SectionHeader(it))
            }
        }
        Log.d("List_sections_size", headers.size.toString() + "\n" + headers.toString())
        return headers.toList()
    }
}

sealed class ServicesInfoResult() {
    data class Result(
        val updateTime: String,
        val list: List<SectionHeader>
    ) : ServicesInfoResult()

    data class Error(val error: String) : ServicesInfoResult()
    object ShowLoading : ServicesInfoResult()
    object HideLoading : ServicesInfoResult()
}

class SectionHeader : SectionEntity<RecommenderInfo.ServicesInfo> {

    constructor(isHeader: Boolean, header: String, isMore: Boolean) : super(isHeader, header) {

    }

    constructor(item: RecommenderInfo.ServicesInfo) : super(item) {

    }

}


