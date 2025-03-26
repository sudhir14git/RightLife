package com.example.rlapp.ai_package.ui.thinkright.tab

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rlapp.R

class PageFragment : Fragment() {

    private var pageNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "Page $pageNumber"
        return view
    }

    companion object {
        private const val ARG_PAGE = "page"

        fun newInstance(page: Int) = PageFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PAGE, page)
            }
        }
    }
}
