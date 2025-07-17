package com.jetsynthesys.rightlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.FragmentBaseBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils.showCustomToast
import java.io.IOException

open class BaseFragment : Fragment() {
    // Use a nullable backing property for the binding
    private var _binding: FragmentBaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    protected val baseBinding get() = _binding!!

    lateinit var sharedPreferenceManager: SharedPreferenceManager
    lateinit var apiService: ApiService
    lateinit var apiServiceFastApi: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize things that don't require a View, e.g., managers, services
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireContext())
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        apiServiceFastApi = ApiClient.getAIClient().create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBaseBinding.inflate(inflater, container, false)
        return baseBinding.root
    }

    fun handleNoInternetView(e: Throwable) {
        /*when (e) {
            is IOException ->
                baseBinding.noInternetView.visibility = View.VISIBLE

            else -> e.message?.let { showCustomToast(it) }
        }*/
        if (e is IOException)
            e.message?.let { showCustomToast(requireContext(), it) }
    }

    // IMPORTANT: Clear the binding reference in onDestroyView to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}