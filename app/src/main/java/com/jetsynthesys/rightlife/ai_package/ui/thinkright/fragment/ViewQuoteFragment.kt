package com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ThinkQuoteResponse
import com.jetsynthesys.rightlife.databinding.FragmentViewQuoteBinding
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.ai_package.model.BaseResponse
import com.jetsynthesys.rightlife.ai_package.model.request.MindfullRequest
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.time.Instant
import java.time.format.DateTimeFormatter

class ViewQuoteFragment  : BaseFragment<FragmentViewQuoteBinding>() {

    private lateinit var tvQuote: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var imageView: ImageView
    private lateinit var lytQuote: ConstraintLayout
    private lateinit var progressDialog: ProgressDialog
    private lateinit var thinkQuoteResponse : ThinkQuoteResponse
    var mStartDate = ""
    var mEndDate = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewQuoteBinding
        get() = FragmentViewQuoteBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mStartDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        tvQuote = view.findViewById(R.id.tv_quote)
        tvAuthor = view.findViewById(R.id.tv_author)
        val backButton = view.findViewById<ImageView>(R.id.img_back)
        val downloadView = view.findViewById<ImageView>(R.id.quote_download_icon)
        val shareQuote = view.findViewById<ImageView>(R.id.img_share_icon)
        lytQuote = view.findViewById(R.id.lyt_quote_of_the_day)
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        fetchQuoteData()

        downloadView.setOnClickListener {
            Toast.makeText(requireContext(), "start Report downloading...", Toast.LENGTH_SHORT).show()
            saveViewAsPdf(requireContext(),lytQuote,"Quote")
        }

        shareQuote.setOnClickListener{
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, tvQuote.text)
            }
            context?.startActivity(Intent.createChooser(intent, "Share via"))
        }

        backButton.setOnClickListener {
            mEndDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            postMindfullData()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mEndDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                postMindfullData()
             //   navigateToFragment(ThinkRightReportFragment(), "ThinkRightLandingFragment")
            }
        })
    }
    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
    private fun fetchQuoteData() {
        progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.quoteOfDay(token)
        call.enqueue(object : Callback<ThinkQuoteResponse> {
            override fun onResponse(call: Call<ThinkQuoteResponse>, response: Response<ThinkQuoteResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    thinkQuoteResponse = response.body()!!
                    tvQuote.setText(thinkQuoteResponse.data.getOrNull(0)?.quote)
                    tvAuthor.setText("-" + thinkQuoteResponse.data.getOrNull(0)?.authorName)
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<ThinkQuoteResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }

    private fun postMindfullData() {
        val mindfullRequest = MindfullRequest(type = "Quote", startDate = mStartDate, endDate = mEndDate)
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.postMindFull(token,mindfullRequest)
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful) {
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle().apply {
                        putString("ModuleName", "ThinkRight")
                    }
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                        addToBackStack(null)
                        commit()
                    }
                   // Toast.makeText(activity, "OK", Toast.LENGTH_SHORT).show()
                } else {
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle().apply {
                        putString("ModuleName", "ThinkRight")
                    }
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                        addToBackStack(null)
                        commit()
                    }                  //  Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                val fragment = HomeBottomTabFragment()
                val args = Bundle().apply {
                    putString("ModuleName", "ThinkRight")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                    addToBackStack(null)
                    commit()
                }              //  Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun saveViewAsPdf(context: Context, view: View, fileName: String) {
        val bitmap = getBitmapFromView(view)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        var fileUri: Uri? = null
        var success = false
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    fileUri = uri
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, "$fileName.pdf")
                fileUri = Uri.fromFile(file)
                outputStream = file.outputStream()
            }

            outputStream?.use {
                document.writeTo(it)
                success = true
                Toast.makeText(requireContext(), "Report downloaded successfully", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            document.close()
            outputStream?.close()
        }

        if (success && fileUri != null) {
            showDownloadNotification(context, fileName, fileUri)
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDownloadNotification(context: Context, fileName: String, fileUri: Uri) {
        val channelId = "download_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the PDF file
        val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, openPdfIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download Complete")
            .setContentText("$fileName.pdf saved to Downloads")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent) // Open PDF on click
            .setAutoCancel(true) // Dismiss when tapped
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }
}