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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
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
import com.jetsynthesys.rightlife.ai_package.model.ScanMealNutritionResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.MoveRightLandingFragment
import com.jetsynthesys.rightlife.ai_package.utils.FileUtils
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SnapMealFragment : BaseFragment<FragmentSnapMealBinding>() {

    private var moduleName : String = ""

    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var currentPhotoPath: String
    private lateinit var takePhotoInfoLayout : LinearLayoutCompat
    private lateinit var enterMealDescriptionLayout : LinearLayoutCompat
    private lateinit var proceedLayout : LinearLayoutCompat
    private lateinit var skipTV : TextView
    private lateinit var mealDescriptionET : EditText
    private lateinit var imageFood : ImageView
    private lateinit var videoView : VideoView
    private var imagePath : String = ""
    private lateinit var imagePathsecond : Uri
    private var isProceedResult : Boolean = false
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var backButton : ImageView
    private var loadingOverlay : FrameLayout? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSnapMealBinding
        get() = FragmentSnapMealBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        proceedLayout = view.findViewById(R.id.layout_proceed)
        imageFood = view.findViewById(R.id.imageFood)
        videoView = view.findViewById(R.id.videoView)
        takePhotoInfoLayout = view.findViewById(R.id.takePhotoInfoLayout)
        enterMealDescriptionLayout = view.findViewById(R.id.enterMealDescriptionLayout)
        skipTV = view.findViewById(R.id.skipTV)
        mealDescriptionET = view.findViewById(R.id.mealDescriptionET)
        backButton = view.findViewById(R.id.backButton)

        moduleName = arguments?.getString("ModuleName").toString()

        skipTV.setOnClickListener {
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                val snapMealFragment = MealScanResultFragment()
//                val args = Bundle()
//                args.putString("ModuleName", arguments?.getString("ModuleName").toString())
//                args.putString("ImagePath", imagePath)
//                args.putParcelable("foodDataResponses", null)
//                snapMealFragment.arguments = args
//                replace(R.id.flFragment, snapMealFragment, "Steps")
//                addToBackStack(null)
//                commit()
//            }
            if (imagePath != ""){
                uploadFoodImagePath(imagePath, mealDescriptionET.text.toString())
            }else{
                Toast.makeText(context, "Please capture food",Toast.LENGTH_SHORT).show()
            }
//            takePhotoInfoLayout.visibility = View.VISIBLE
//            enterMealDescriptionLayout.visibility = View.GONE
//            skipTV.visibility = View.GONE
//            videoView.visibility = View.VISIBLE
//            imageFood.visibility = View.GONE
//            isProceedResult = false
//            proceedLayout.isEnabled = true
//            proceedLayout.setBackgroundResource(R.drawable.green_meal_bg)
//            videoPlay()
        }

        if (sharedPreferenceManager.getFirstTimeUserForSnapMealVideo()) {
            takePhotoInfoLayout.visibility = View.GONE
         //   enterMealDescriptionLayout.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            val cameraDialog = CameraDialogFragment("")
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
            // Request all permissions at once
//            if (!hasAllPermissions()) {
//                requestAllPermissions()
          //  }else{
                if (isProceedResult){
                    if (imagePath != ""){
                        uploadFoodImagePath(imagePath, mealDescriptionET.text.toString())
                    }else{
                        Toast.makeText(context, "Please capture food",Toast.LENGTH_SHORT).show()
                    }
                }else{
                  //  CameraDialogFragment().show(requireActivity().supportFragmentManager, "CameraDialog")
                  //  openCameraForImage()
                }
      //      }
        } else {
            takePhotoInfoLayout.visibility = View.VISIBLE
            enterMealDescriptionLayout.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            sharedPreferenceManager.setFirstTimeUserForSnapMealVideo(true)
        }
        videoPlay()

        proceedLayout.setOnClickListener {
            // Request all permissions at once
           /* if (!hasAllPermissions()) {
                requestAllPermissions()
            }else{*/
                if (isProceedResult){
                    if (imagePath != ""){
                        uploadFoodImagePath(imagePath, mealDescriptionET.text.toString())
                    }else{
                        Toast.makeText(context, "Please capture food",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val cameraDialog = CameraDialogFragment("")
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
           // }
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                val snapMealFragment = MealScanResultFragment()
//                val args = Bundle()
//                args.putString("ModuleName", arguments?.getString("ModuleName").toString())
//                args.putString("ImagePath", imagePath)
//                args.putParcelable("foodDataResponses", null)
//                snapMealFragment.arguments = args
//                replace(R.id.flFragment, snapMealFragment, "Steps")
//                addToBackStack(null)
//                commit()
//            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (moduleName.equals("HomeDashboard")){
                    startActivity(Intent(context, HomeNewActivity::class.java))
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
        })

        backButton.setOnClickListener {
            if (moduleName.equals("HomeDashboard")){
                startActivity(Intent(context, HomeNewActivity::class.java))
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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_IMAGE_CAPTURE -> {
//                    val file = File(currentPhotoPath)
//                    if (file.exists()) {
//                        val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
//                        // Rotate the image if required
//                        val imageUri = FileProvider.getUriForFile(requireContext(),
//                            "${requireContext().packageName}.fileprovider", file)
//                        val rotatedBitmap = rotateImageIfRequired(requireContext(), bitmap, imageUri)
//                        // Set the image in the UI
//                        videoView.visibility = View.GONE
//                        imageFood.visibility = View.VISIBLE
//                        imageFood.setImageBitmap(rotatedBitmap)
//                        // Save image details
//                        imagePath = currentPhotoPath
//                        takePhotoInfoLayout.visibility = View.GONE
//                        enterMealDescriptionLayout.visibility = View.VISIBLE
//                        skipTV.visibility = View.VISIBLE
//                        proceedLayout.isEnabled = false
//                        proceedLayout.setBackgroundResource(R.drawable.light_green_bg)
//                        isProceedResult = false
//                        mealDescriptionET.text.clear()
//                    } else {
//                        Log.e("ImageCapture", "File does not exist at $currentPhotoPath")
//                    }
//                }
//
//            }
//            if (resultCode == Activity.RESULT_CANCELED) {
//                // User pressed back or closed camera
//                Toast.makeText(context, "Camera canceled", Toast.LENGTH_SHORT).show()
//            }
//            Toast.makeText(context, "Camera canceled", Toast.LENGTH_SHORT).show()
//        }
//    }

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
                startActivity(Intent(context, HomeNewActivity::class.java))
                requireActivity().finish()
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

    private fun uploadFoodImagePath(imagePath: String, description: String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val base64Image = encodeImageToBase64(imagePath)
        val apiKey = "BTmhNkVTRxElUNrSym77"
        val request = AnalysisRequest(apiKey, base64Image, description)
        val call = ApiClient.apiServiceFoodCaptureImageApi.analyzeFoodImage(
            "analysis", request)
        call.enqueue(object : Callback<ScanMealNutritionResponse> {
            override fun onResponse(call: Call<ScanMealNutritionResponse>, response: Response<ScanMealNutritionResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    println("Success: ${response.body()}")
                    if (response.body()?.data != null){
                        if (response.body()?.data!!.isNotEmpty()){
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                val snapMealFragment = MealScanResultFragment()
                                val args = Bundle()
                                args.putString("ModuleName", moduleName)
                                args.putString("ImagePath", imagePath)
                                args.putString("description", mealDescriptionET.text.toString())
                                args.putString("ImagePathsecound", imagePathsecond.toString())
                                args.putParcelable("foodDataResponses", response.body())
                                snapMealFragment.arguments = args
                                replace(R.id.flFragment, snapMealFragment, "Steps")
                                addToBackStack(null)
                                commit()
                            }
                        }else{
                            Toast.makeText(context, "Data not find out please try again", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(context, "Data not find out please try again", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    println("Error: ${response.errorBody()?.string()}")
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    Toast.makeText(context, response.errorBody()?.string(), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ScanMealNutritionResponse>, t: Throwable) {
                println("Failure: ${t.message}")
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun encodeImageToBase64(imagePath: String): String {
        val file = File(imagePath)
        val inputStream = FileInputStream(file)
        val bytes = inputStream.readBytes()
        inputStream.close()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    // Check if all required permissions are granted
//    private fun hasAllPermissions(): Boolean {
//        return REQUIRED_PERMISSIONS.all {
//            ContextCompat.checkSelfPermission(requireActivity(), it) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    // Request all permissions in one go
//    private fun requestAllPermissions() {
//        ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
//    }

//    // Handle permissions result
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            val deniedPermissions = permissions.zip(grantResults.toTypedArray())
//                .filter { it.second != PackageManager.PERMISSION_GRANTED }
//                .map { it.first }
//
//            if (deniedPermissions.isEmpty()) {
//                openCameraForImage()
//                Toast.makeText(context, "All permissions granted!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context, "Permissions denied: $deniedPermissions", Toast.LENGTH_LONG).show()
//            }
//        }
//    }

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
}

class CameraDialogFragment(private val imagePath: String) : DialogFragment() {

    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var isTorchOn = false

    var imageSelectedListener: OnImageSelectedListener? = null

    // Activity Result Launcher for selecting an image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageSelectedListener?.onImageSelected(it)
            dismiss()
            Toast.makeText(requireContext(), "Image loaded from gallery!", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
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

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        view.findViewById<ImageView>(R.id.closeButton)?.setOnClickListener {
            dismiss()
            startActivity(Intent(context, HomeNewActivity::class.java))
            requireActivity().finish()
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

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
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
                    requireActivity().runOnUiThread {
                        savedUri?.let { uri ->
                            imageSelectedListener?.onImageSelected(uri)
                            dismiss()
                        }
                        Toast.makeText(requireContext(), "Photo saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Capture failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    private fun toggleFlash() {
        camera?.let {
            val hasFlash = it.cameraInfo.hasFlashUnit()
            if (!hasFlash) {
                Toast.makeText(requireContext(), "Flash not supported", Toast.LENGTH_SHORT).show()
                return
            }
            isTorchOn = !isTorchOn
            it.cameraControl.enableTorch(isTorchOn)

            Toast.makeText(
                requireContext(),
                "Flash ${if (isTorchOn) "ON" else "OFF"}",
                Toast.LENGTH_SHORT
            ).show()
        } ?: run {
            Toast.makeText(requireContext(), "Camera not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        Handler(Looper.getMainLooper()).post {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
                navigateToFragment(MoveRightLandingFragment(), "landingFragment")
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

