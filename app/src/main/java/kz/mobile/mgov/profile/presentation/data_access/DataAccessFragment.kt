package kz.mobile.mgov.profile.presentation.data_access


import android.os.Bundle
import android.view.*
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import kz.mobile.mgov.R
import kz.mobile.mgov.common.navigator.NavigationAnimation
import kz.mobile.mgov.common.navigator.Screen
import kz.mobile.mgov.common.presentation.ContainerActivity
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.widgets.MGovToolbar
import kz.mobile.mgov.egov_android.extensions.initRecyclerView
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RecyclerViewItemClick

class DataAccessFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView

    private val dataAccessAdapter by lazy {
        DataAccessAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_data_access, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        bindViews(view)
        setData()
        setAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(Menu.NONE, MGovToolbar.ITEM_ID_QUESTION, Menu.NONE, "")?.apply {
            setIcon(R.drawable.ic_question)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == MGovToolbar.ITEM_ID_QUESTION) {
            val data = Bundle().apply {
                putString(Screen.SCREEN, Screen.QUESTION_INFO)
                putBoolean(IntentConstants.SET_CLOSE_ICON, true)
                putString(IntentConstants.TITLE, getString(R.string.title_services_info))
                putString(IntentConstants.DESCRIPTION, getString(R.string.services_info_text))
            }
            ContainerActivity.start(activity, bundle = data, animationType = NavigationAnimation.SLIDE_UP)
            return true
        }
        return false
    }

    private fun bindViews(view: View) = with(view) {
        recyclerView = findViewById(R.id.recycler_view)
        context.initRecyclerView(recyclerView, true)
    }

    private fun setData() {

    }

    private fun setAdapter() {
        dataAccessAdapter.list = arrayListOf(
            DataAccessItem(0, getString(R.string.personal_documents_data), false, R.drawable.ic_personal_documents),
            DataAccessItem(1, getString(R.string.title_social_status), false, R.drawable.ic_personal_documents),
            DataAccessItem(2, getString(R.string.business), false, R.drawable.ic_personal_documents),
            DataAccessItem(3, getString(R.string.title_auto), false, R.drawable.ic_personal_documents),
            DataAccessItem(4, getString(R.string.real_estate_and_property), false, R.drawable.ic_personal_documents),
            DataAccessItem(5, getString(R.string.family_title), false, R.drawable.ic_personal_documents)
        )
        dataAccessAdapter.listener = object : RecyclerViewItemClick<DataAccessItem> {
            override fun onItemClick(position: Int, item: DataAccessItem) {
                val data = Bundle().apply {
                    when (item.id) {
                        0 -> putString(Screen.SCREEN, Screen.PERSONAL_DOCUMENT)
                        1 -> putString(Screen.SCREEN, Screen.SOCIAL_STATUS)
                        2 -> putString(Screen.SCREEN, Screen.BUSINESS)
                        3 -> putString(Screen.SCREEN, Screen.AUTO)
                        4 -> putString(Screen.SCREEN, Screen.REAL_ESTATE_AND_PROPERTY)
                        5 -> putString(Screen.SCREEN, Screen.FAMILY)
                    }

                }
                ContainerActivity.start(context = context, bundle = data, animationType = NavigationAnimation.SLIDE)
            }
        }
        recyclerView.adapter = dataAccessAdapter
    }

    companion object {
        fun newInstance(data: Bundle? = null): DataAccessFragment {
            val fragment = DataAccessFragment()
            fragment.arguments = data
            return fragment
        }
    }
}

class DataAccessAdapter : RecyclerView.Adapter<DataAccessAdapter.DataAccessViewHolder>() {

    var list: ArrayList<DataAccessItem> = arrayListOf()

    var listener: RecyclerViewItemClick<DataAccessItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAccessViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_data_access, parent, false)
        return DataAccessViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: DataAccessViewHolder, position: Int) {
        holder.bind(list.get(position))
    }

    inner class DataAccessViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: DataAccessItem) = with(view) {
            val civDataAccessIcon = findViewById<CircleImageView>(R.id.civDataAccessIcon)
            val tvDataAccessName = findViewById<TextView>(R.id.tvDataAccessName)
            val switchDataAccess = findViewById<Switch>(R.id.switchDataAccess)

            civDataAccessIcon.setImageResource(item.icon)
            tvDataAccessName.text = item.name
            switchDataAccess.isChecked = item.isActive
            switchDataAccess.visibility = View.GONE

            setOnClickListener {
                listener?.onItemClick(adapterPosition, item)
            }
        }
    }
}


data class DataAccessItem(
    val id: Int,
    val name: String,
    val isActive: Boolean = false,
    val icon: Int
)