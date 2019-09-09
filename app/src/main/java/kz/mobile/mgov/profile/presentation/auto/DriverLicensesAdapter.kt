package kz.mobile.mgov.profile.presentation.auto

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kz.mobile.mgov.common.widgets.DriverLicenseView
import kz.mobile.mgov.profile.data.model.DriverLicense

class DriverLicensesAdapter(private val driverLicenses: List<DriverLicense>) :
    RecyclerView.Adapter<DriverLicensesAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val driverLicense = DriverLicenseView(parent.context)
        return ViewHolder(driverLicense)
    }

    override fun getItemCount(): Int {
        return driverLicenses.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.driverLicenseView.setLicenseData(driverLicenses[position])
    }

    open inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val driverLicenseView: DriverLicenseView

        init {
            driverLicenseView = v as DriverLicenseView
        }
    }
}