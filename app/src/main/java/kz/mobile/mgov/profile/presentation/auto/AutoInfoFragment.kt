package kz.mobile.mgov.profile.presentation.auto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kz.mobile.mgov.R
import kz.mobile.mgov.common.di.Injectable
import kz.mobile.mgov.common.extensions.closeWithAnimation
import kz.mobile.mgov.common.presentation.ParameterAdapter
import kz.mobile.mgov.common.presentation.Section
import kz.mobile.mgov.common.presentation.base.BaseActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.widgets.DurationSeekBarView
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.profile.data.model.Auto
import javax.inject.Inject


class AutoInfoFragment : BaseFragment(), Injectable {

    val gson: Gson = Gson()

    lateinit var auto: Auto

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var techInspectionBar: DurationSeekBarView
    lateinit var parameterList: RecyclerView
    lateinit var productionYear: TextView
    lateinit var engineVolume: TextView

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AutoViewModel::class.java)
    }

    private lateinit var mGovToolbar: MGovToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auto = gson.fromJson(arguments?.getString(IntentConstants.AUTO_INFO), Auto::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auto_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    private fun bindViews(view: View) = with(view) {
        activity?.window?.statusBarColor = ContextCompat.getColor(context, R.color.colorInputBackground)
        mGovToolbar = findViewById(R.id.mGovToolbar)
        mGovToolbar.setTitle(getString(R.string.car_info))
        mGovToolbar.setCustomBackgroundColor(ContextCompat.getColor(context, R.color.colorInputBackground))
        mGovToolbar.setNavigationOnClickListener {
            (activity as BaseActivity).apply {
                finish()
                closeWithAnimation()
            }
        }
        techInspectionBar = findViewById(R.id.tech_inspection_bar)
        parameterList = findViewById(R.id.parameter_list)
        productionYear = findViewById(R.id.production_year)
        engineVolume = findViewById(R.id.engine_volume)
        setData(auto)
    }


    private fun setData(auto: Auto) {
        techInspectionBar.setData(auto.techInspection?.inspectionDate, auto.techInspection?.expirationDate)
        productionYear.text = auto.year
        engineVolume.text = auto.volume
        val parameters = ArrayList<Section>()
        if (auto.techPassportNumber != null) {
            parameters.add(Section(true, getString(R.string.technical_certificate)))
            parameters.add(Section(auto.techPassportNumber))
        }

        if (auto.color != null) {
            parameters.add(Section(true, getString(R.string.title_color)))
            parameters.add(Section(auto.color))
        }

        if (auto.vinCode != null) {
            parameters.add(Section(true, getString(R.string.title_car_vin_code)))
            parameters.add(Section(auto.vinCode))
        }
        val adapter = ParameterAdapter(R.layout.row_item_simple_text, R.layout.row_item_header, parameters)
        parameterList.adapter = adapter
        parameterList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
    }


    companion object {
        fun newInstance(data: Bundle? = null): AutoInfoFragment {
            val fragment = AutoInfoFragment()
            fragment.arguments = data
            return fragment
        }
    }
}
