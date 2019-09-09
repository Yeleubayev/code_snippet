package kz.mobile.mgov.profile.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.dialog_touchid_permission.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kz.mobile.mgov.R
import kz.mobile.mgov.auth.presentation.AuthActivity
import kz.mobile.mgov.common.AuthLevel
import kz.mobile.mgov.common.di.Injectable
import kz.mobile.mgov.common.di.modules.GlideApp
import kz.mobile.mgov.common.extensions.changeTabStyle
import kz.mobile.mgov.common.navigator.Screen
import kz.mobile.mgov.common.navigator.ScreenSelector
import kz.mobile.mgov.common.presentation.ContainerActivity
import kz.mobile.mgov.common.presentation.SelectorBottomSheetFragment
import kz.mobile.mgov.common.presentation.base.BaseFragment
import kz.mobile.mgov.common.repository.SessionRepository
import kz.mobile.mgov.common.repository.user.ProfileData
import kz.mobile.mgov.common.utils.RealPathUtil
import kz.mobile.mgov.common.widgets.GeneralProfileItemView
import kz.mobile.mgov.document.presentation.remote.RemoteDocumentListFragment
import kz.mobile.mgov.profile.presentation.data_access.DataAccessFragment
import kz.mobile.mgov.profile.presentation.profile_info.ProfileInfoFragment
import kz.mobile.mgov.egov_android.utils.IntentConstants
import kz.mobile.mgov.egov_android.utils.RequestsConstants
import kz.mobile.mgov.egov_android.utils.showEdsNeedDialog
import kz.mobile.mgov.menu.presentation.MenuActivity
import java.io.File
import java.io.IOException
import java.sql.Date
import java.text.SimpleDateFormat
import javax.inject.Inject

class ProfileFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!, viewModelFactory).get(ProfileViewModel::class.java)
    }

    @Inject
    lateinit var sessionRepository: SessionRepository

    private var selectorBottomSheetFragment: SelectorBottomSheetFragment<ScreenSelector>? = null

    private lateinit var civAvatar: CircleImageView

    private var capturedPictureFile: File? = null

    private lateinit var requestOrderHistory: GeneralProfileItemView
    private lateinit var myContracts: GeneralProfileItemView
    private lateinit var healthPassport: GeneralProfileItemView
    private lateinit var digitalEmploymentBook: GeneralProfileItemView
    private lateinit var contactInfoSection: GeneralProfileItemView
    private lateinit var addressesSection: GeneralProfileItemView
    private lateinit var familySection: GeneralProfileItemView
    private lateinit var personalDocumentsSection: GeneralProfileItemView
    private lateinit var socialStatusSection: GeneralProfileItemView
    private lateinit var businessSection: GeneralProfileItemView
    private lateinit var carSection: GeneralProfileItemView
    private lateinit var propertySection: GeneralProfileItemView

    private val onClickListenerForEdsButton: DialogInterface.OnClickListener =
        object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                AuthActivity.start(
                    context = context,
                    requestCode = RequestsConstants.AUTH_REQUEST
                )
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setData()
        setSections()
    }

    override fun onStart() {
        super.onStart()
        viewModel.saveSubscriptionsStatusData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RequestsConstants.GALLERY_REQUEST -> {
                    cropImage(data?.data)
                }
                RequestsConstants.CAMERA_REQUEST -> {
                    capturedPictureFile?.let { file ->
                        cropImage(Uri.fromFile(file))
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val file = File(RealPathUtil.getRealPath(context!!, result.uri))
                    viewModel.uploadAvatar(file)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestsConstants.AVATAR_PERMISSION_REQUEST) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openAvatarChooser()
            }
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun bindViews(view: View) = with(view) {
        civAvatar = findViewById(R.id.civAvatar)
        requestOrderHistory = findViewById(R.id.requestOrderHistory)
        myContracts = findViewById(R.id.myContracts)
        healthPassport = findViewById(R.id.healthPassport)
        digitalEmploymentBook = findViewById(R.id.digitalEmploymentBook)
        contactInfoSection = findViewById(R.id.contactInfoSection)
        addressesSection = findViewById(R.id.addressesSection)
        familySection = findViewById(R.id.familySection)
        personalDocumentsSection = findViewById(R.id.personalDocumentsSection)
        socialStatusSection = findViewById(R.id.socialStatusSection)
        businessSection = findViewById(R.id.businessSection)
        carSection = findViewById(R.id.carSection)
        propertySection = findViewById(R.id.propertySection)

/*		tabLayout.apply {
			addTab(newTab().setText(R.string.dossier_title))
			addTab(newTab().setText(R.string.documents_title))
			addTab(newTab().setText(R.string.personal_data_title))
		}
		tabLayout.apply {
			Log.d("selected_pos", selectedTabPosition.toString())
			changeTabStyle(selectedTabPosition, true)
			addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
				override fun onTabReselected(tab: TabLayout.Tab?) {

				}

				override fun onTabUnselected(tab: TabLayout.Tab?) {
					if (tab != null) {
						changeTabStyle(tab.position)
					}
				}

				override fun onTabSelected(tab: TabLayout.Tab?) {
					if (tab?.position != null) {
						changeTabStyle(tab.position, true)
						viewPager.currentItem = tab.position
					}
				}
			})
		}*/

        civAvatar.setOnClickListener {
            val storageGranted = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            val cameraGranted = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (storageGranted && cameraGranted) {
                openAvatarChooser()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    RequestsConstants.AVATAR_PERMISSION_REQUEST
                )
            }
        }

        logoutView.setOnClickListener { viewModel.logout() }
    }

    private fun setData() {
        viewModel.getProfileData(false)
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ProfileResult.ShowLoading -> {

                }
                is ProfileResult.HideLoading -> {

                }
                is ProfileResult.Result -> result.profileData?.apply {
                    setProfileData(this)
                } ?: run {

                }
                is ProfileResult.Error -> {

                }
                is ProfileResult.Logout -> {
                    MenuActivity.start(context)
                }
            }
        })
    }

    private fun checkEdsAuthNeed(dialogTitle: String, screen: String) {
        if(sessionRepository.authLevel != AuthLevel.E_SIGN){
            showEdsNeedDialog(dialogTitle)
        }
        else{
            navigateToScreen(screen)
        }
    }

    private fun showEdsNeedDialog(dialogTitle: String) {
        context?.showEdsNeedDialog(
            title = dialogTitle,
            message = getString(R.string.electronic_storage_not_logged_message),
            edsListener = onClickListenerForEdsButton
        )
    }

    private fun setSections() {
        contactInfoSection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.PROFILE_INFO)
        }
        addressesSection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.ADDRESSES)
        }
        familySection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.FAMILY)
        }
        personalDocumentsSection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.PERSONAL_DOCUMENT)
        }
        socialStatusSection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.SOCIAL_STATUS)
        }
        businessSection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.BUSINESS)
        }
        carSection.setOnClickListener {
            checkEdsAuthNeed(dialogTitle = getString(R.string.contact_information), screen = Screen.AUTO)
        }
        propertySection.setOnClickListener {
            checkEdsAuthNeed(
                dialogTitle = getString(R.string.contact_information),
                screen = Screen.REAL_ESTATE_AND_PROPERTY)
        }
        logoutView.setOnClickListener { viewModel.logout() }
    }

    private fun navigateToScreen(screen: String) {
        val data = Bundle().apply { putString(Screen.SCREEN, screen) }
        ContainerActivity.start(this, data)
    }

    private fun setAdapter() {
        /*val data = Bundle().apply {
            putBoolean("from_profile", true)
        }
        val fragments = arrayListOf<Fragment>(
            ProfileInfoFragment(),
            RemoteDocumentListFragment.newInstance(data),
            DataAccessFragment.newInstance()
        )
        viewPager.adapter = MainFragmentAdapter(childFragmentManager, fragments)
        viewPager.apply {
            offscreenPageLimit = 3
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    for (i in 0..tabLayout.tabCount) {
                        tabLayout.changeTabStyle(i, isSelected = i == position)
                    }
                    tabLayout.setScrollPosition(position, positionOffset, true)

                }

                override fun onPageSelected(position: Int) {

                }
            })
        }*/
    }

    @SuppressLint("SetTextI18n")
    private fun setProfileData(profileData: ProfileData) = with(profileData) {
        tvFullname.text = "${profileData.firstname}\n${profileData.lastname}\n${profileData.patronymic}"
        tvIin.text = "${getString(R.string.title_iin)}: ${profileData.iin}"
        GlideApp.with(this@ProfileFragment)
            .load(avatar)
            .placeholder(R.drawable.ic_circle_place_holder)
            .into(civAvatar)
    }

    private fun openAvatarChooser() {
        val map = HashMap<Int, ScreenSelector>().apply {
            put(0, ScreenSelector(type = PICTURE_FROM_CAMERA, name = getString(R.string.title_take_picture)))
            put(
                1,
                ScreenSelector(
                    type = PICTURE_FROM_GALLERY,
                    name = getString(R.string.title_choose_picture_from_library)
                )
            )
            put(2, ScreenSelector(type = PICTURE_DELETE, name = getString(R.string.title_delete)))
        }
        val data = Bundle().apply {
            putSerializable(IntentConstants.OPTIONS_MAP, map)
        }
        selectorBottomSheetFragment = SelectorBottomSheetFragment.newInstance(data)
        selectorBottomSheetFragment?.positionLiveData?.observe(viewLifecycleOwner, Observer { result ->
            when (result.type) {
                PICTURE_FROM_CAMERA -> {
                    openCamera()
                }
                PICTURE_FROM_GALLERY -> {
                    openGallery()
                }
                PICTURE_DELETE -> {
                    viewModel.deleteAvatar()
                }
            }
        })
        selectorBottomSheetFragment?.show(childFragmentManager, "avatar")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(intent, RequestsConstants.GALLERY_REQUEST)
    }

    private fun openCamera() {
        activity?.let { activity ->
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity.packageManager) != null) {
                val pictureFile = getPictureImage()
                capturedPictureFile = pictureFile
                val photoURI = FileProvider.getUriForFile(
                    activity,
                    "${context?.packageName}.provider",
                    pictureFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, RequestsConstants.CAMERA_REQUEST)
            }
        }
    }

    @Throws(IOException::class)
    private fun getPictureImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date(System.currentTimeMillis()))
        val pictureFile = "MGOV_$timeStamp"
        val storageDir = activity?.cacheDir
        val image = File.createTempFile(pictureFile, ".jpg", storageDir)
        return image
    }

    private fun cropImage(uri: Uri?) {
        CropImage.activity(uri)
            .setAspectRatio(300, 300)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .setCropMenuCropButtonTitle(getString(R.string.crop))
            .start(context!!, this@ProfileFragment)
    }

    companion object {

        private const val PICTURE_FROM_CAMERA = "PICTURE_FROM_CAMERA"
        private const val PICTURE_FROM_GALLERY = "PICTURE_FROM_GALLERY"
        private const val PICTURE_DELETE = "PICTURE_DELETE"
    }
}

