package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.R.color.transparent
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.WaterIntakeRequest
import com.jetsynthesys.rightlife.ai_package.model.response.LogWaterResponse
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterIntakeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var waterAmount: TextView
    var listener: OnWaterIntakeConfirmedListener? = null

    private var selectedCups = 2
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedValueText: TextView
    private lateinit var ivCupIcon : ImageView
    private lateinit var btn_confirm : Button
    private lateinit var progressFill: View
    private lateinit var progressBarContainer: FrameLayout
    private var dY = 0f
    private var minY = 0f
    private var maxY = 0f
    private var waterIntake = 0
    private val maxIntake = 3000
    private var loadingOverlay : FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_water_intake_selection, container, false)
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener {
//            val bottomSheet = bottomSheetDialog
//                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//
//            if (bottomSheet != null) {
//                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//                behavior.isDraggable = false
//            }
//        }
//        return bottomSheetDialog
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_eat_right_landing)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val closeIV = view.findViewById<ImageView>(R.id.closeIV)
        progressFill = view.findViewById(R.id.progressFill)
        btn_confirm = view.findViewById(R.id.btn_confirm)
        progressBarContainer = view.findViewById(R.id.progressBarContainer)
        selectedValueText = view.findViewById(R.id.selectedValueText)
        ivCupIcon = view.findViewById(R.id.ivCupIcon)

        progressBarContainer.viewTreeObserver.addOnGlobalLayoutListener {
            minY = progressBarContainer.top.toFloat()
            maxY = (progressBarContainer.bottom - ivCupIcon.height).toFloat()
        }
        btn_confirm.setOnClickListener {
            val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            logUserWaterIntake(userId = userId, source = "apple", waterMl = waterIntake.toInt(), date = currentDate)

        }
        updateUI()

        ivCupIcon.setOnTouchListener { v, event ->
            val parent = v.parent as View
            val maxY = (parent.height - v.height).toFloat()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newY = (event.rawY + dY).coerceIn(0f, maxY)
                    v.y = newY
                    updateValueBasedOnPosition(newY)
                }
                MotionEvent.ACTION_UP -> {
                    updateValueBasedOnPosition(v.y)
                }
            }
            true
        }
        closeIV.setOnClickListener {
            dismiss()
        }
    }

    private fun updateUI() {
        selectedValueText.text = "${waterIntake}"
        progressBarContainer.post {
            val containerHeight = progressBarContainer.height
            val progressRatio = waterIntake.toFloat() / maxIntake.toFloat()
            val newHeight = (containerHeight * progressRatio).toInt()
            val params = progressFill.layoutParams
            params.height = newHeight
            progressFill.layoutParams = params
        }
    }
     private fun logUserWaterIntake(
        userId: String,
        source: String,
        waterMl: Int,
        date: String
    ) {
         if (isAdded  && view != null){
             requireActivity().runOnUiThread {
                 showLoader(requireView())
             }
         }
        val request = WaterIntakeRequest(
            userId = userId,
            source = source,
            waterMl = waterMl,
            date = date
        )
        val call = com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.logWaterIntake(request)
        call.enqueue(object : Callback<LogWaterResponse> {
            override fun onResponse(
                call: Call<LogWaterResponse>,
                response: Response<LogWaterResponse>
            ) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val responseBody = response.body()
                    response.body()?.waterMl?.let { listener?.onWaterIntakeConfirmed(it.toInt()) }
                    dismiss()
                    Log.d("LogWaterAPI", "Success: $responseBody")
                    // You can do something with responseBody here
                } else {
                    Log.e(
                        "LogWaterAPI",
                        "Error: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<LogWaterResponse>, t: Throwable) {
                Log.e("LogWaterAPI", "Failure: ${t.localizedMessage}")
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun updateValueBasedOnPosition(positionY: Float) {
        val containerHeight = progressBarContainer.height
        val maxSteps = maxIntake / 250  // assuming maxIntake is a multiple of 250
        val stepHeight = containerHeight / maxSteps

        val stepsFromBottom = ((containerHeight - positionY) / stepHeight).toInt()
        val clampedSteps = stepsFromBottom.coerceIn(0, maxSteps)

        waterIntake = clampedSteps * 250
        updateUI()
    }

//    private fun updateValueBasedOnPosition(y: Float) {
//        val containerHeight = progressBarContainer.height - ivCupIcon.height
//        val clampedY = y.coerceIn(0f, containerHeight.toFloat())
//        val progressRatio = 1 - (clampedY / containerHeight)
//        waterIntake = (progressRatio * maxIntake).toInt()
//        updateUI()
//    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = WaterIntakeBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }
}
interface OnWaterIntakeConfirmedListener {
    fun onWaterIntakeConfirmed(amount: Int)
}

