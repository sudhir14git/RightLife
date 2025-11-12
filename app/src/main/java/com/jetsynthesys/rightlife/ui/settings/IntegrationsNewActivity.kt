package com.jetsynthesys.rightlife.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityIntegrationsNewBinding
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem
import kotlinx.coroutines.launch

class IntegrationsNewActivity : BaseActivity() {

    private val allReadPermissions = setOf(
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    private lateinit var binding: ActivityIntegrationsNewBinding
    private lateinit var settingsAdapter: SettingsAdapter
    private lateinit var healthConnectClient: HealthConnectClient

    // Fix: Initialize the launcher as a class property
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val requestPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                lifecycleScope.launch {
                    val allGranted = permissions.values.all { it }
                    if (allGranted) {
                        showToast("Health Connect permissions granted")
                    } else {
                        showToast("Some Health Connect permissions were denied")
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntegrationsNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        setupEmailNotificationsRecyclerView()

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupEmailNotificationsRecyclerView() {
        val settingsItems = listOf(
                SettingItem("Health Connect")
        )

        settingsAdapter = SettingsAdapter(settingsItems) { item ->
            when (item.title) {
                "Health Connect" -> {
                    val availabilityStatus = HealthConnectClient.getSdkStatus(this)
                    if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                        healthConnectClient = HealthConnectClient.getOrCreate(this)
                        lifecycleScope.launch {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                requestPermissionsAndReadAllData()
                            } else {
                                showToast("Health Connect requires Android 14 or later")
                            }
                        }
                    } else {
                        installHealthConnect(this)
                    }
                }
            }
        }

        binding.rvEmailNotifications.apply {
            layoutManager = LinearLayoutManager(this@IntegrationsNewActivity)
            adapter = settingsAdapter
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
                showToast("Health Connect permissions already granted")
            } else {
                requestPermissionsLauncher.launch(allReadPermissions.toTypedArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error checking permissions: ${e.message}")
        }
    }

    private fun installHealthConnect(context: Context) {
        try {
            val uri = "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Cannot open Play Store")
        }
    }
}