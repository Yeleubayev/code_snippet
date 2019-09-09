package kz.mobile.mgov.profile.presentation.views.id_docs

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.document.data.DocumentInfo
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel

class IdDocsViewModel(private val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<DocumentsResult>()
    }
}


sealed class DocumentsResult {
    data class Result(val list: List<DocumentInfo>) : DocumentsResult()
    data class Error(val error: String) : DocumentsResult()
    object ShowLoading : DocumentsResult()
    object HideLoading : DocumentsResult()
}