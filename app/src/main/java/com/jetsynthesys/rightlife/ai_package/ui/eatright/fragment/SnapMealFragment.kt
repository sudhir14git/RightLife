package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.FileProvider
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.AnalysisRequest
import com.jetsynthesys.rightlife.databinding.FragmentSnapMealBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Base64
import android.view.KeyEvent
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.ai_package.model.ScanMealNutritionResponse
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealsNutrientsRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealNutrientsResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.RatingMealBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.MoveRightLandingFragment
import com.jetsynthesys.rightlife.ai_package.utils.FileUtils
import com.jetsynthesys.rightlife.ai_package.utils.showToast
import com.jetsynthesys.rightlife.apimodel.UploadImage
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlData
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlResponse
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SnapMealFragment : BaseFragment<FragmentSnapMealBinding>(), SnapMealDetectBottomSheet.SnapMealDetectListener {

    private var moduleName : String = ""
    lateinit var apiService: ApiService
    private var preSignedUrlData: PreSignedUrlData? = null
    private var imageGeneratedUrl = ""
    private lateinit var currentPhotoPath: String
    private lateinit var takePhotoInfoLayout : LinearLayoutCompat
    private lateinit var tvProceed : TextView
    private lateinit var enterMealDescriptionLayout : LinearLayoutCompat
    private lateinit var proceedLayout : LinearLayoutCompat
    private lateinit var skipTV : TextView
    private lateinit var mealDescriptionET : EditText
    private lateinit var imageFood : ImageView
    private lateinit var videoView : VideoView
    private var imagePath : String = ""
    private var gallery : String = ""
    private lateinit var title : TextView
    private lateinit var imagePathsecond : Uri
    private var isProceedResult : Boolean = false
    private lateinit var descriptionName: String
    private var isGalleryOpen : Boolean = false
    private lateinit var snapMealDetectBottomSheet : SnapMealDetectBottomSheet
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var backButton : ImageView
    private var loadingOverlay : FrameLayout? = null
    private var homeTab : String = ""
    private lateinit var mealType : String
    private var selectedMealDate : String = ""
    private  var currentPhotoPathsecound : Uri? = null
    private var snapImageUrl: String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSnapMealBinding
        get() = FragmentSnapMealBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        apiService = com.jetsynthesys.rightlife.RetrofitData.ApiClient.getClient(requireContext()).create(ApiService::class.java)
        proceedLayout = view.findViewById(R.id.layout_proceed)
        imageFood = view.findViewById(R.id.imageFood)
        videoView = view.findViewById(R.id.videoView)
        takePhotoInfoLayout = view.findViewById(R.id.takePhotoInfoLayout)
        tvProceed = view.findViewById(R.id.tvProceed)
        enterMealDescriptionLayout = view.findViewById(R.id.enterMealDescriptionLayout)
        skipTV = view.findViewById(R.id.skipTV)
        mealDescriptionET = view.findViewById(R.id.mealDescriptionET)
        backButton = view.findViewById(R.id.backButton)
        title = view.findViewById(R.id.title)

        homeTab = arguments?.getString("homeTab").toString()
        moduleName = arguments?.getString("ModuleName").toString()
        mealType = arguments?.getString("mealType").toString()
        gallery = arguments?.getString("gallery").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        snapImageUrl = arguments?.getString("snapImageUrl").toString()
        descriptionName = arguments?.getString("description").toString()

        val imagePathString = arguments?.getString("ImagePathsecound")
        if (imagePathString != null){
            currentPhotoPathsecound = imagePathString?.let { Uri.parse(it) }!!
        }else{
            currentPhotoPathsecound = null
        }

        skipTV.setOnClickListener {
            if (!skipTV.isEnabled) return@setOnClickListener
            skipTV.isEnabled = false
            if (imagePath != "") {
                if (imageGeneratedUrl != "") {
                    getSnapMealsNutrients(imageGeneratedUrl, mealDescriptionET.text.toString())
                } else {
                    imagePathsecond?.let { getUrlFromURI(it) }
                }
            } else {
                if (snapImageUrl.equals("") || snapImageUrl.equals("null")){
                    Toast.makeText(context, "Please capture food", Toast.LENGTH_SHORT).show()
                }else{
                    getSnapMealsNutrients(snapImageUrl, mealDescriptionET.text.toString())
                }
            }
            skipTV.postDelayed({
                skipTV.isEnabled = true
            }, 1200)
        }

        if (gallery.equals("gallery")){
            if (currentPhotoPathsecound != null){
                try {
                    val path = getRealPathFromURI(requireContext(), currentPhotoPathsecound!!)
                    if (path != null) {
                        val scaledBitmap = decodeAndScaleBitmap(path, 1080, 1080)
                        if (scaledBitmap != null) {
                            imagePath = path
                            imagePathsecond = currentPhotoPathsecound!!
                            val rotatedBitmap = rotateImageIfRequired(requireContext(), scaledBitmap,
                                currentPhotoPathsecound!!
                            )
                            videoView.visibility = View.GONE
                            imageFood.visibility = View.VISIBLE
                            imageFood.setImageBitmap(rotatedBitmap)
                            takePhotoInfoLayout.visibility = View.GONE
                            tvProceed.text = "Proceed"
                            title.visibility = View.VISIBLE
                            enterMealDescriptionLayout.visibility = View.VISIBLE
                            skipTV.visibility = View.VISIBLE
                            proceedLayout.isEnabled = false
                            proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
                            isProceedResult = false
                            mealDescriptionET.text.clear()
                        } else {
                            Toast.makeText(requireContext(), "Failed to decode image", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("ImageCapture", "File does not exist at $currentPhotoPath")
                    }
                } catch (e: Exception) {
                    Log.e("ImageLoad", "Error loading image from file path: $currentPhotoPath", e)
                }
            }
        }else{
            if (sharedPreferenceManager.getFirstTimeUserForSnapMealVideo()) {
                takePhotoInfoLayout.visibility = View.GONE
                tvProceed.text = "Proceed"
                title.visibility = View.VISIBLE
                //   enterMealDescriptionLayout.visibility = View.VISIBLE
                videoView.visibility = View.GONE
                if (snapImageUrl.equals("") || snapImageUrl.equals("null")){
                    val cameraDialog = CameraDialogFragment("", moduleName, homeTab, mealType, selectedMealDate)
                    cameraDialog.imageSelectedListener = object : OnImageSelectedListener {
                        override fun onImageSelected(imageUri: Uri) {
                            val path = getRealPathFromURI(requireContext(), imageUri)
                            imagePathsecond = imageUri
                            if (path != null) {
                                val scaledBitmap = decodeAndScaleBitmap(path, 1080, 1080) // Limit to screen size
                                if (scaledBitmap != null) {
                                    val rotatedBitmap = rotateImageIfRequired(requireContext(), scaledBitmap, imageUri)
                                    imagePath = path
                                    videoView.visibility = View.GONE
                                    imageFood.visibility = View.VISIBLE
                                    imageFood.setImageBitmap(rotatedBitmap)

                                    takePhotoInfoLayout.visibility = View.GONE
                                    tvProceed.text = "Proceed"
                                    title.visibility = View.VISIBLE
                                    enterMealDescriptionLayout.visibility = View.VISIBLE
                                    skipTV.visibility = View.VISIBLE
                                    proceedLayout.isEnabled = false
                                    proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
                                    isProceedResult = false
                                    mealDescriptionET.text.clear()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to decode image", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Unable to get image path", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    cameraDialog.show(parentFragmentManager, "CameraDialog")
                }else{
                    videoView.visibility = View.GONE
                    imageFood.visibility = View.VISIBLE


                    takePhotoInfoLayout.visibility = View.GONE
                    tvProceed.text = "Proceed"
                    title.visibility = View.VISIBLE
                    enterMealDescriptionLayout.visibility = View.VISIBLE
                    skipTV.visibility = View.VISIBLE

                    if (descriptionName.equals("") || descriptionName.equals("null")){
                        proceedLayout.isEnabled = false
                        proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
                        isProceedResult = false
                        mealDescriptionET.text.clear()
                    }else{
                        proceedLayout.isEnabled = true
                        proceedLayout.setBackgroundResource(R.drawable.green_meal_bg)
                        isProceedResult = true
                        mealDescriptionET.setText(descriptionName)
                    }

                    Glide.with(this)
                        .load(snapImageUrl)
                        .placeholder(R.drawable.ic_view_meal_place)
                        .error(R.drawable.ic_view_meal_place)
                        .into(imageFood)
                }

                // Request all permissions at once
//            if (!hasAllPermissions()) {
//                requestAllPermissions()
                //  }else{
                if (isProceedResult){
                    if (imagePath != ""){
                       // uploadFoodImagePath(imagePath, mealDescriptionET.text.toString())
                        if (imageGeneratedUrl != ""){
                            getSnapMealsNutrients(imageGeneratedUrl, mealDescriptionET.text.toString())
                        }else{
                            imagePathsecond?.let { getUrlFromURI(it) }
                        }
                    }else{
                        if (descriptionName.equals("") || descriptionName.equals("null")){
                            Toast.makeText(context, "Please capture food",Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    //  CameraDialogFragment().show(requireActivity().supportFragmentManager, "CameraDialog")
                    //  openCameraForImage()
                }
                //      }
            } else {
                takePhotoInfoLayout.visibility = View.VISIBLE
                tvProceed.text = "Next"
                title.visibility = View.INVISIBLE
                enterMealDescriptionLayout.visibility = View.GONE
                videoView.visibility = View.VISIBLE
                sharedPreferenceManager.setFirstTimeUserForSnapMealVideo(true)
            }
        }

        videoPlay()

        proceedLayout.setOnClickListener {
            if (!proceedLayout.isEnabled) return@setOnClickListener
            proceedLayout.isEnabled = false
                if (isProceedResult){
                    if (imagePath != ""){
                        if (imageGeneratedUrl != ""){
                            getSnapMealsNutrients(imageGeneratedUrl, mealDescriptionET.text.toString())
                        }else{
                            imagePathsecond?.let { getUrlFromURI(it) }
                        }
                       // uploadFoodImagePath(imagePath, mealDescriptionET.text.toString())
                    }else{
                        if (snapImageUrl.equals("") || snapImageUrl.equals("null")){
                            Toast.makeText(context, "Please capture food", Toast.LENGTH_SHORT).show()
                        }else{
                            getSnapMealsNutrients(snapImageUrl, mealDescriptionET.text.toString())
                        }
                    }
                }else{
                    val cameraDialog = CameraDialogFragment("", moduleName, homeTab, mealType, selectedMealDate)
                    cameraDialog.imageSelectedListener = object : OnImageSelectedListener {
                        override fun onImageSelected(imageUri: Uri) {
                            val path = getRealPathFromURI(requireContext(), imageUri)
                            imagePathsecond = imageUri
                            if (path != null) {
                                val scaledBitmap = decodeAndScaleBitmap(path, 1080, 1080) // Limit to screen size
                                if (scaledBitmap != null) {
                                    val rotatedBitmap = rotateImageIfRequired(requireContext(), scaledBitmap, imageUri)
                                    imagePath = path
                                    videoView.visibility = View.GONE
                                    imageFood.visibility = View.VISIBLE
                                    imageFood.setImageBitmap(rotatedBitmap)
                                    takePhotoInfoLayout.visibility = View.GONE
                                    tvProceed.text = "Proceed"
                                    title.visibility = View.VISIBLE
                                    enterMealDescriptionLayout.visibility = View.VISIBLE
                                    skipTV.visibility = View.VISIBLE
                                    proceedLayout.isEnabled = false
                                    proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
                                    isProceedResult = false
                                    mealDescriptionET.text.clear()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to decode image", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Unable to get image path", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    cameraDialog.show(parentFragmentManager, "CameraDialog")
                }

            proceedLayout.postDelayed({
                proceedLayout.isEnabled = true
            }, 1200)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (moduleName.equals("HomeDashboard")){
                   // startActivity(Intent(context, HomeNewActivity::class.java))
                    requireActivity().finish()
                }else if (homeTab.equals("homeTab")){
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("selectedMealDate", selectedMealDate)
                    args.putString("ModuleName", moduleName)
                    args.putString("mealType", mealType)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else{
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRight")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            }
        })

        backButton.setOnClickListener {
            if (moduleName.equals("HomeDashboard")){
               // startActivity(Intent(context, HomeNewActivity::class.java))
                requireActivity().finish()
            }else if (homeTab.equals("homeTab")){
                val fragment = HomeTabMealFragment()
                val args = Bundle()
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("ModuleName", moduleName)
                args.putString("mealType", mealType)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else{
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        mealDescriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s!!.length > 0){
                    proceedLayout.isEnabled = true
                    proceedLayout.setBackgroundResource(R.drawable.green_meal_bg)
                    isProceedResult = true
                }else{
                    proceedLayout.isEnabled = false
                    proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
                    isProceedResult = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun notMealDetectItem() {
        // 1️⃣ Check if BottomSheet already showing
        val existingSheet = childFragmentManager.findFragmentByTag("SnapMealDetectBottomSheet")
        if (existingSheet != null && existingSheet.isAdded) {
            return     // <-- STOP showing again
        }
        // 2️⃣ Create and show if not already visible
        val snapMealDetectBottomSheet = SnapMealDetectBottomSheet()
        snapMealDetectBottomSheet.isCancelable = true
        snapMealDetectBottomSheet.show(
            childFragmentManager,
            "SnapMealDetectBottomSheet"
        )
    }

     fun openGalleryForImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }

    fun openCameraForImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File = createImageFile()
            if (photoFile.exists()) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    photoFile
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                Log.e("Camera", "Failed to create file")
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath  // Save the path for later use
        }
    }
    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var filePath: String? = null
        // Try getting path from content resolver
        if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == "file") {
            filePath = uri.path
        }
        return filePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    REQUEST_IMAGE_CAPTURE -> {
                        val file = File(currentPhotoPath)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                            val imageUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.fileprovider",
                                file
                            )
                            val rotatedBitmap = rotateImageIfRequired(requireContext(), bitmap, imageUri)
                            imageFood.setImageBitmap(rotatedBitmap)
                            imageFood.visibility = View.VISIBLE
                            videoView.visibility = View.GONE
                            // Update UI
                            imagePath = currentPhotoPath
                            takePhotoInfoLayout.visibility = View.GONE
                            tvProceed.text = "Proceed"
                            title.visibility = View.VISIBLE
                            enterMealDescriptionLayout.visibility = View.VISIBLE
                            skipTV.visibility = View.VISIBLE
                            proceedLayout.isEnabled = false
                            proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
                            isProceedResult = false
                            mealDescriptionET.text.clear()
                        } else {
                            Log.e("ImageCapture", "File does not exist at $currentPhotoPath")
                        }
                    }
                    REQUEST_IMAGE_PICK -> {
                        val selectedImage = data?.data
                        selectedImage?.let {
                            val path = FileUtils.getFileFromUri(requireContext(), selectedImage) ?: return
                            imagePath = path.path
                            val downsampledBitmap = getDownsampledBitmap(selectedImage, 1000, 1000)
                            // Rotate the image if required
                            val rotatedBitmap = downsampledBitmap?.let { it1 ->
                                rotateImageIfRequired(requireContext(), it1, selectedImage)
                            }
                            imageFood.setImageBitmap(rotatedBitmap)
                            // Set the image in the UI
                        }
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                // ✅ Only here when user presses back or closes camera
                if (moduleName.equals("HomeDashboard")){
                   // startActivity(Intent(context, HomeNewActivity::class.java))
                    requireActivity().finish()
                }else{
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRight")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            }
        }
    }

    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val exifInterface = inputStream?.let { ExifInterface(it) }
        val orientation = exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        inputStream?.close()

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }
    private fun decodeAndScaleBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
    }

    fun showLoader(activity: View) {
        loadingOverlay = activity.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(activity: View) {
        loadingOverlay = activity.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun encodeImageToBase64(imagePath: String): String? {
        return try {

        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap != null) {
            // Resize if too large (e.g. max 1080px width)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                1080,
                (bitmap.height * 1080f / bitmap.width).toInt(),
                true
            )
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream) // 75% quality
            val compressedBytes = outputStream.toByteArray()
             Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        } else {
        null
    }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun videoPlay(){
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.mealsnap_v31}")
        videoView.setVideoURI(videoUri)
        val mediaController = MediaController(context)
        mediaController.setAnchorView(videoView)
//        videoView.setMediaController(null)
//        videoView.start()
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true  // ✅ This enables auto-continuous playback
            videoView.setMediaController(null)
            videoView.start()
        }
    }

    private fun getDownsampledBitmap(imageUri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        val inputStream2 = requireContext().contentResolver.openInputStream(imageUri)
        val downsampledBitmap = BitmapFactory.decodeStream(inputStream2, null, options)
        inputStream2?.close()
        return downsampledBitmap
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getUrlFromURI(uri: Uri) {
        val uploadImage = UploadImage()
        uploadImage.isPublic = false
        uploadImage.fileType = "USER_FILES"
        if (uri != null) {
            val (fileName, fileSize) = getFileNameAndSize(requireContext(), uri) ?: return
            uploadImage.fileSize = fileSize
            uploadImage.fileName = fileName
        }
        uriToFile(uri)?.let { getPreSignedUrl(uploadImage, it) }
    }

    private fun uriToFile(uri: Uri): File? {
        val contentResolver = requireContext().contentResolver
        val fileName = getFileName(uri) ?: "temp_image_file"
        val tempFile = File(requireContext().cacheDir, fileName)

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val returnCursor = requireContext().contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    private fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long>? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) {
                val name = it.getString(nameIndex)
                val size = it.getLong(sizeIndex)
                return Pair(name, size)
            }
        }
        return null
    }

    private fun getPreSignedUrl(uploadImage: UploadImage, file: File) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call: Call<PreSignedUrlResponse> = apiService.getPreSignedUrl(sharedPreferenceManager.accessToken, uploadImage)
        call.enqueue(object : Callback<PreSignedUrlResponse?> {
            override fun onResponse(
                call: Call<PreSignedUrlResponse?>,
                response: Response<PreSignedUrlResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { preSignedUrlData = it }
                    response.body()?.data?.url?.let {
                        CommonAPICall.uploadImageToPreSignedUrl(
                            requireContext(),
                            file, it
                        ) { success ->
                            if (success) {
                                if (isAdded && view != null) {
                                    requireActivity().runOnUiThread {
                                        dismissLoader(requireView())
                                    }
                                }
                                //showToast("Image uploaded successfully!")
                                imageGeneratedUrl = it.split("?").get(0)
                                getSnapMealsNutrients(imageGeneratedUrl, mealDescriptionET.text.toString())
                            } else {
                                if (isAdded && view != null) {
                                    requireActivity().runOnUiThread {
                                        dismissLoader(requireView())
                                    }
                                }
                                showToast("Upload failed!")
                            }
                        }
                    }
                } else {
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    showToast("Server Error: " + response.code())
                }
            }
            override fun onFailure(call: Call<PreSignedUrlResponse?>, t: Throwable) {
                // handleNoInternetView(t)
                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun getSnapMealsNutrients(imageUrl: String, description: String) {
        if (!isAdded) return
        val act = activity ?: return
        act.runOnUiThread { showLoader(requireView()) }
        val base64Image = encodeImageToBase64(imagePath)
        val request = SnapMealsNutrientsRequest(imageUrl, description)
        val call = ApiClient.apiServiceFastApiV2.getSnapMealsNutrients(request)
        call.enqueue(object : Callback<SnapMealNutrientsResponse> {
            override fun onResponse(
                call: Call<SnapMealNutrientsResponse>,
                response: Response<SnapMealNutrientsResponse>
            ) {
                if (!isAdded) return
                val act = activity ?: return
                act.runOnUiThread { dismissLoader(requireView()) }
                val body = response.body()
                if (response.isSuccessful && body?.data != null) {
                    if (body.data.dish.isNotEmpty()) {
                        if (!isAdded) return
                        val fm = act.supportFragmentManager
                        val snapMealFragment = MealScanResultFragment().apply {
                            arguments = Bundle().apply {
                                putString("homeTab", homeTab)
                                putString("selectedMealDate", selectedMealDate)
                                putString("mealType", mealType)
                                putString("ModuleName", moduleName)
                                putString("description", mealDescriptionET.text.toString())
                                putParcelable("foodDataResponses", body)
                            }
                        }
                        fm.beginTransaction()
                            .replace(R.id.flFragment, snapMealFragment, "Steps")
                            .addToBackStack(null)
                            .commitAllowingStateLoss() // Changed from commit() to commitAllowingStateLoss()
                    } else {
                        notMealDetectItem()
                    }
                } else {
                    notMealDetectItem()
                }
            }
            override fun onFailure(call: Call<SnapMealNutrientsResponse>, t: Throwable) {
                if (!isAdded) return
                val act = activity ?: return
                act.runOnUiThread { dismissLoader(requireView()) }
                notMealDetectItem()
            }
        })
    }

    override fun onSnapMealDetect(notDetect: Boolean) {
        if (imagePath != ""){
            //  imagePathsecond
            if (imageGeneratedUrl != ""){
                skipTV.isEnabled = true
                getSnapMealsNutrients(imageGeneratedUrl, mealDescriptionET.text.toString())
            }else{
                imagePathsecond?.let { getUrlFromURI(it) }
            }
            // imagePathString.let { Uri.parse(it) }!!
            // uploadFoodImagePath(imagePath, mealDescriptionET.text.toString())
        }else{
            Toast.makeText(context, "Please capture food",Toast.LENGTH_SHORT).show()
        }
    }
}

class CameraDialogFragment(private val imagePath: String, val moduleName : String, val homeTab : String,
                           val mealType : String, val selectedMealDate : String) : DialogFragment() {

    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var isTorchOn = false
    private var isGalleryOpen = false
    var imageSelectedListener: OnImageSelectedListener? = null
    private var isCameraDialogShowing = false
    private var cameraSettingsDialog: AlertDialog? = null

    // Activity Result Launcher for selecting an image from gallery

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            imageSelectedListener?.onImageSelected(it)
            if (isAdded) {
                dismiss()
            }
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Image loaded from gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = view.findViewById(R.id.viewFinder)
        // requestPermissionsIfNeeded()
        /*if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }*/

        view.findViewById<ImageView>(R.id.closeButton)?.setOnClickListener {

            if (moduleName.equals("HomeDashboard")){
//                startActivity(Intent(context, HomeDashboardActivity::class.java))
//                requireActivity().finish()
                if (isAdded) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }else if (homeTab.equals("homeTab")){
                if (!isAdded) return@setOnClickListener
                val act = activity ?: return@setOnClickListener

                dismiss()
                val fragment = HomeTabMealFragment()
                val args = Bundle()
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("ModuleName", moduleName)
                args.putString("mealType", mealType)
                fragment.arguments = args
                act.supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commitAllowingStateLoss()
                }
            }else{
                // val fragment = HomeBottomTabFragment()
                if (!isAdded) return@setOnClickListener
                val act = activity ?: return@setOnClickListener
                dismiss()
                val fragmentManager = act.supportFragmentManager
                val snapMealFragment = fragmentManager.findFragmentByTag("SnapMealFragmentTag")
                snapMealFragment?.let {
                    fragmentManager.beginTransaction()
                        .remove(it)
                        .commitAllowingStateLoss()
                }
                //               val args = Bundle()
//                args.putString("ModuleName", "EatRight")
//                fragment.arguments = args
//                requireActivity().supportFragmentManager.beginTransaction().apply {
//                    replace(R.id.flFragment, fragment, "landing")
//                    addToBackStack("landing")
//                    commit()
//                }
            }
        }

        view.findViewById<ImageView>(R.id.captureButton)?.setOnClickListener {
            view.findViewById<ImageView>(R.id.captureButton)?.isEnabled = false
            // Your click logic here
            takePhoto()
            // Re-enable after delay (optional)
            view.findViewById<ImageView>(R.id.captureButton)?.postDelayed({
                view.findViewById<ImageView>(R.id.captureButton)?.isEnabled = true
            }, 700) // 500 ms delay
        }

        view.findViewById<ImageView>(R.id.flashToggle)?.setOnClickListener {
            toggleFlash()
        }

        view.findViewById<ImageView>(R.id.galleryButton)?.setOnClickListener {
            view.findViewById<ImageView>(R.id.galleryButton)?.isEnabled = false
            // Your click logic here
            openGallery()
            // Re-enable after delay (optional)
            view.findViewById<ImageView>(R.id.galleryButton)?.postDelayed({
                view.findViewById<ImageView>(R.id.galleryButton)?.isEnabled = true
            }, 700) // 500 ms delay
        }
        requestPermissionsIfNeeded()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun onPermissionsGranted() {
        startCamera()
    }

    private fun showCameraSettingsDialog() {
        if (!isAdded) return
        if (cameraSettingsDialog?.isShowing == true) return
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Camera Access Denied")
            .setMessage("Please enable camera access in Settings to snap your meal.")
            .setCancelable(false)
            .setPositiveButton("Open Settings", null)
            .setNegativeButton("Cancel", null)
        cameraSettingsDialog = builder.create()
        cameraSettingsDialog?.setOnShowListener {
            val positiveBtn = cameraSettingsDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = cameraSettingsDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveBtn?.setOnClickListener {
                cameraSettingsDialog?.dismiss()
                openAppSettings()
            }
            negativeBtn?.setOnClickListener {
                cameraSettingsDialog?.dismiss()
                closeSnapMealFragment()
            }
        }
        cameraSettingsDialog?.show()
    }

    private fun openAppSettings() {
        if (!isAdded) return
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireActivity().packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: App list settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(fallbackIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (!isAdded) return@registerForActivityResult
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                onPermissionsGranted()
                return@registerForActivityResult
            }
            // Some permissions denied
            val permanentlyDenied = permissions.any { (perm, granted) ->
                !granted && !shouldShowRequestPermissionRationale(perm)
            }
            if (permanentlyDenied) {
                // User denied twice or pressed "Don't ask again"
                showCameraSettingsDialog()
//            } else {
//                // Temporary deny (first time)
//                showRationaleDialog()
            }
        }

    private fun requestPermissionsIfNeeded() {
        if (allPermissionsGranted()) {
            onPermissionsGranted()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCameraPermissionGranted()) {
            cameraSettingsDialog?.dismiss()
            cameraSettingsDialog = null
            if (!allPermissionsGranted()) {
                // This will re-request CAMERA permission if needed
                permissionLauncher.launch(REQUIRED_PERMISSIONS)
            } else {
                startCamera()
            }
        }
        // Intercept device back press while this dialog is showing
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                closeSnapMealFragment()
                true // consume back press
            } else {
                false
            }
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        if (!isAdded) return false
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun closeSnapMealFragment() {
        if (!isAdded) return
        val act = activity ?: return
        //  dismiss() // Close the dialog first
        if (moduleName.equals("HomeDashboard")){
//                startActivity(Intent(context, HomeDashboardActivity::class.java))
//                requireActivity().finish()
            act.onBackPressedDispatcher.onBackPressed()
        }else if (homeTab.equals("homeTab")){
            dismiss()
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("ModuleName", moduleName)
            args.putString("mealType", mealType)
            fragment.arguments = args
            act.supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commitAllowingStateLoss()
            }
        }else{
            dismiss()
            val fragmentManager = act.supportFragmentManager
            val snapMealFragment = fragmentManager.findFragmentByTag("SnapMealFragmentTag")
            snapMealFragment?.let {
                fragmentManager.beginTransaction()
                    .remove(it)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun startCamera() {
        if (!isAdded) return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            if (!isAdded) return@addListener

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val aspectRatio = AspectRatio.RATIO_16_9
            val preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
                // 👇 Set zoom level to 1.0x (linear zoom 0.0)
                camera?.cameraControl?.setLinearZoom(0f)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RightLife")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    Log.d(TAG, "Photo saved: $savedUri")

                    // Check if fragment is still attached before updating UI
                    if (!isAdded) return
                    val act = activity ?: return

                    act.runOnUiThread {
                        if (!isAdded) return@runOnUiThread

                        savedUri?.let { uri ->
                            imageSelectedListener?.onImageSelected(uri)
                            if (isAdded) {
                                dismiss()
                            }
                        }
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Photo saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)

                    // Check if fragment is still attached before updating UI
                    if (!isAdded) return
                    val act = activity ?: return

                    act.runOnUiThread {
                        if (!isAdded) return@runOnUiThread

                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Capture failed: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }

    private fun toggleFlash() {
        if (!isAdded) return

        camera?.let {
            val hasFlash = it.cameraInfo.hasFlashUnit()
            if (!hasFlash) {
                Toast.makeText(requireContext(), "Flash not supported", Toast.LENGTH_SHORT).show()
                return
            }
            if (isTorchOn){
                isTorchOn = !isTorchOn
                it.cameraControl.enableTorch(isTorchOn)
                view?.findViewById<ImageView>(R.id.flashToggle)?.setImageResource(R.drawable.ic_flash_off)
            }else{
                isTorchOn = true
                it.cameraControl.enableTorch(isTorchOn)
                view?.findViewById<ImageView>(R.id.flashToggle)?.setImageResource(R.drawable.flash_icon)
            }
            //   Toast.makeText(requireContext(), "Flash ${if (isTorchOn) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
        } ?: run {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Camera not initialized", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        isAdded && ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (!isAdded) return

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                if (context != null) {
                    Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
                }
                navigateToFragment(MoveRightLandingFragment(), "landingFragment")
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment, tag: String) {
        if (!isAdded) return
        val act = activity ?: return

        act.supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commitAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageCapture = null // Clear the reference to prevent memory leaks
        camera = null
        cameraSettingsDialog?.dismiss()
        cameraSettingsDialog = null
        cameraExecutor.shutdown()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
interface OnImageSelectedListener {
    fun onImageSelected(imageUri: Uri)
}

