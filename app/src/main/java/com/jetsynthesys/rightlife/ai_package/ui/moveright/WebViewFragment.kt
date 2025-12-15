package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private var url: String? = null

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(ARG_URL)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        webView = view.findViewById(R.id.webView)

        // Manual back button (ImageView) click
        btnBack.setOnClickListener {
            handleBack()
        }

        // System back button handling
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = WebViewClient()

        url?.let {
            webView.loadUrl(it)
        }
    }

    // Common back handling logic (both for button click and system back)
    private fun handleBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            parentFragmentManager.popBackStack()
        }
    }
}