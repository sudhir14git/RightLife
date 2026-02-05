package com.jetsynthesys.rightlife

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.ActivityBaseBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import java.io.IOException

open class BaseActivity : AppCompatActivity() {

    lateinit var baseBinding: ActivityBaseBinding
    lateinit var sharedPreferenceManager: SharedPreferenceManager
    lateinit var apiService: ApiService
    lateinit var apiServiceFastApi: ApiService

    // Heart pulse animators to manage cancellation
    private var fullHeartAnimator: ObjectAnimator? = null
    var compactHeartAnimator: ObjectAnimator? = null
    //val isSyncing = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(baseBinding.root)

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Apply padding to container
        ViewCompat.setOnApplyWindowInsetsListener(baseBinding.container) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Apply Notch/StatusBar offsets to Sync Views
        setupSyncViewInsets()

        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)
        apiService = ApiClient.getClient(this).create(ApiService::class.java)
        apiServiceFastApi = ApiClient.getAIClient().create(ApiService::class.java)
    }

    // --- Sync UI Logic ---

    /**
     * Main entry point to update the sync state.
     * @param isLoading true if sync is currently running
     * @param isFirstSync true if this is the "Pill" style sync, false for compact heart
     * @param isCompleted true to trigger the "Success" green state
     */
    fun updateSync(isLoading: Boolean, isFirstSync: Boolean = true, isCompleted: Boolean = false) {
        runOnUiThread {
            when {
                isCompleted -> onSyncComplete(isFirstSync)
                isLoading -> {
                    if (isFirstSync) showFullSyncView()
                }

                else -> hideAllSyncViews()
            }
        }
    }

    private fun showFullSyncView() {
        hideAllSyncViews()
        //isSyncing.value = true
        baseBinding.fullSyncStatusView.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = -50f
            animate().alpha(1f).translationY(0f).setDuration(500).start()
        }
        startHeartPulse(baseBinding.fullHeartIcon, true)
    }

    /*private fun showCompactSyncView() {
        hideAllSyncViews()
        isSyncing.value = true
        baseBinding.compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.6f
            scaleY = 0.6f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator(1.5f))
                .start()
        }
        startHeartPulse(baseBinding.compactHeartIcon, false)
    }*/

    fun startHeartPulse(target: ImageView, isFull: Boolean) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            target,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f)
        ).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        if (isFull) {
            fullHeartAnimator?.cancel()
            fullHeartAnimator = animator
        } else {
            compactHeartAnimator?.cancel()
            compactHeartAnimator = animator
        }
        animator.start()
    }

    private fun onSyncComplete(isFirstTime: Boolean) {
        // 1. Define colors for Success State
        //isSyncing.value = false
        val colorGreen = ContextCompat.getColor(this, R.color.color_green)
        val bgSuccess = ContextCompat.getColor(this, R.color.light_green)
        val colorStateList = android.content.res.ColorStateList.valueOf(colorGreen)

        if (isFirstTime) {
            // Stop the pulsing animation so we can reset scale to 1.0
            fullHeartAnimator?.cancel()

            baseBinding.fullSyncStatusView.apply {
                // Force visibility in case this is called from onResume
                visibility = View.VISIBLE
                alpha = 1f
                translationY = 0f
                setCardBackgroundColor(bgSuccess)
            }

            // Update Text and Icons
            baseBinding.syncTitle.text = "Health Data Synced!"
            baseBinding.syncSubTitle.visibility = View.GONE
            baseBinding.fullRotatingArc.visibility = View.GONE

            baseBinding.fullHeartIcon.apply {
                imageTintList = colorStateList
                scaleX = 1f
                scaleY = 1f
                // Optionally change the icon to a checkmark if you have one
                // setImageResource(R.drawable.ic_check_circle_green)
            }

            // 2. Auto-hide with Slide-Up animation after 2.5 seconds
            baseBinding.root.postDelayed({
                baseBinding.fullSyncStatusView.animate()
                    .translationY(-300f) // Slide up past the notch
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction {
                        baseBinding.fullSyncStatusView.visibility = View.GONE
                        // Reset fields for the next time it shows
                        baseBinding.syncSubTitle.visibility = View.VISIBLE
                        baseBinding.fullRotatingArc.visibility = View.VISIBLE
                    }
                    .start()
            }, 2500)

        } else {
            // --- Compact View Completion ---
            compactHeartAnimator?.cancel()

            baseBinding.compactSyncIndicator.apply {
                visibility = View.VISIBLE
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
            }

            baseBinding.compactRotatingArc.visibility = View.GONE
            baseBinding.compactHeartIcon.apply {
                imageTintList = colorStateList
                scaleX = 1f
                scaleY = 1f
            }

            // 3. Auto-hide with Shrink animation after 2.5 seconds
            baseBinding.root.postDelayed({
                baseBinding.compactSyncIndicator.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction {
                        baseBinding.compactSyncIndicator.visibility = View.GONE
                        baseBinding.compactRotatingArc.visibility = View.VISIBLE
                    }
                    .start()
            }, 2500)
        }
    }

    private fun hideAllSyncViews() {
        fullHeartAnimator?.cancel()
        compactHeartAnimator?.cancel()
        baseBinding.fullSyncStatusView.visibility = View.GONE
        baseBinding.compactSyncIndicator.visibility = View.GONE
    }

    private fun setupSyncViewInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(baseBinding.fullSyncStatusView) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = statusBarHeight + 20
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(baseBinding.compactSyncIndicator) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = statusBarHeight + 20
            }
            insets
        }
    }

    // --- Helper Methods ---
    fun setChildContentView(layoutResId: Int) {
        val childView = layoutInflater.inflate(layoutResId, baseBinding.container, false)
        baseBinding.container.addView(childView)
    }

    fun setChildContentView(view: View) {
        baseBinding.container.addView(view)
    }

    fun handleNoInternetView(e: Throwable) {
        runOnUiThread {
            when (e) {
                is java.net.SocketTimeoutException -> Log.e("Error", e.message ?: "Timeout")
                is IOException -> e.message?.let { showCustomToast(it) }
                else -> e.message?.let { showCustomToast(it) }
            }
        }
    }
}