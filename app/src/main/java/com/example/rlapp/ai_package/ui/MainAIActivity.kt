package com.example.rlapp.ai_package.ui


import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseActivity
import com.example.rlapp.databinding.ActivityMainAiBinding
import com.example.rlapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainAIActivity : BaseActivity() {

    lateinit var bi: ActivityMainAiBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bi = ActivityMainAiBinding.inflate(layoutInflater)
        setContentView(bi.root)
    }

    fun setupViews() {
        // Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    override fun onPause() {
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
    }
}
