package kz.mobile.mgov.profile.presentation.business

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kz.mobile.mgov.R
import kz.mobile.mgov.common.extensions.closeWithAnimation
import kz.mobile.mgov.common.presentation.ParameterAdapter
import kz.mobile.mgov.common.presentation.Section
import kz.mobile.mgov.common.presentation.base.BaseActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.profile.data.model.LegalEntity

class LegalEntityInfoFragment : BaseFragment() {

    private lateinit var mGovToolbar: MGovToolbar
    private lateinit var legalEntityInfo: RecyclerView
    private lateinit var legalEntityName: TextView
    private lateinit var legalEntity: LegalEntity
    private val gson = Gson()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        legalEntity = gson.fromJson(arguments?.getString(IntentConstants.LEGAL_ENTITY_INFO), LegalEntity::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_legal_entity_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setData()
    }

    private fun bindViews(view: View) = with(view) {
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.legal_entity_details))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        legalEntityName = findViewById(R.id.legal_entity_name_tv)
        legalEntityInfo = findViewById(R.id.legal_entity_info)
    }

    private fun setData() {
        legalEntityName.text = legalEntity.name?.ruText
        val legalEntityParameters = ArrayList<Section>()
        legalEntityParameters.add(Section(true, getString(R.string.title_bin)))
        legalEntityParameters.add(Section(legalEntity.bin))
        legalEntityParameters.add(Section(true, getString(R.string.role)))
        legalEntityParameters.add(Section(legalEntity.participantType))
        val adapter = ParameterAdapter(R.layout.row_item_simple_text, R.layout.row_item_header, legalEntityParameters)
        legalEntityInfo.adapter = adapter
        legalEntityInfo.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
    }

    companion object {
        fun newInstance(data: Bundle? = null): LegalEntityInfoFragment {
            val fragment = LegalEntityInfoFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
