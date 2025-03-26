package com.example.rlapp.ai_package.ui.thinkright.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.CardItem
import com.example.rlapp.ai_package.ui.thinkright.adapter.CrousalTabAdapter
import com.example.rlapp.ai_package.ui.thinkright.adapter.MoreToolsAdapter
import com.example.rlapp.ai_package.ui.thinkright.adapter.ToolsAdapter
import com.example.rlapp.databinding.FragmentThinkRightLandingBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.math.abs

class ThinkRightReportFragment : BaseFragment<FragmentThinkRightLandingBinding>() {

    private lateinit var carouselViewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>
    private lateinit var toolsRecyclerView : RecyclerView
    private lateinit var journalingRecyclerView : RecyclerView
    private val toolsAdapter by lazy { ToolsAdapter(requireContext(),3) }

    private val toolsMoreAdapter by lazy { MoreToolsAdapter(requireContext(),4) }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentThinkRightLandingBinding
        get() = FragmentThinkRightLandingBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carouselViewPager = view.findViewById(R.id.viewPager)
        dotsLayout = view.findViewById(R.id.dotsLayout)
        toolsRecyclerView = view.findViewById(R.id.rec_journaling_tools)
        toolsRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        toolsRecyclerView.adapter = toolsAdapter

        journalingRecyclerView = view.findViewById(R.id.rec_add_tools)
        journalingRecyclerView.layoutManager = GridLayoutManager(context, 4)
        journalingRecyclerView.adapter = toolsMoreAdapter

        val cardItems = listOf(
            CardItem("Functional Strength Training", "This is the first card."),
            CardItem("Functional Strength Training", "This is the second card."),
            CardItem("Functional Strength Training", "This is the third card.")
        )
        val adapter = CrousalTabAdapter(cardItems)
        carouselViewPager.adapter = adapter
        addDotsIndicator(cardItems.size)
        carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })
        carouselViewPager.setPageTransformer { page, position ->
            val offset = abs(position)
            page.scaleY = 1 - (offset * 0.1f)
        }

    }

    private fun addDotsIndicator(count: Int) {
        dots = arrayOfNulls(count)
        for (i in 0 until count) {
            dots[i] = ImageView(requireContext()).apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_unselected))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                layoutParams = params
            }
            dotsLayout.addView(dots[i])
        }

        updateDots(0)
    }

    private fun updateDots(position: Int) {
        for (i in dots.indices) {
            val drawable = if (i == position) {
                R.drawable.think_dot_selected
            } else {
                R.drawable.dot_unselected
            }
            dots[i]?.setImageResource(drawable)
        }
    }
}