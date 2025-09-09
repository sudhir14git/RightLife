package com.jetsynthesys.rightlife.ui.mindaudit

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityRedFlagAlertBinding

class RedFlagAlertActivity : BaseActivity() {
    private lateinit var binding: ActivityRedFlagAlertBinding
    private lateinit var adapterState: HelpLineAdapter
    private lateinit var adapterCountry: HelpLineAdapter
    private val stateList: ArrayList<Organization> = ArrayList()
    private val countryList: ArrayList<Organization> = ArrayList()
    private var selectedCountryPosition = 0
    private var selectedStatePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedFlagAlertBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.tvState.paintFlags = binding.tvState.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvCountry.paintFlags = binding.tvCountry.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val jsonString = loadJSONFromAssets()
        val gson = Gson()
        val helplinesResponse = gson.fromJson(jsonString, HelplinesResponse::class.java)
        helplinesResponse.countries[selectedCountryPosition].organizations?.let {
            countryList.addAll(
                it
            )
        }
        helplinesResponse.countries[selectedCountryPosition].states?.get(selectedStatePosition)?.organizations?.let {
            stateList.addAll(
                it
            )
        }
        if (stateList.isEmpty()) {
            binding.rlStateHelpLine.isVisible = false
            binding.rvStateHelpLine.isVisible = false
        } else {
            binding.rlStateHelpLine.isVisible = true
            binding.rvStateHelpLine.isVisible = true
        }
        adapterCountry = HelpLineAdapter(this, countryList)
        adapterState = HelpLineAdapter(this, stateList)
        binding.rvCountryHelpLine.layoutManager = LinearLayoutManager(this)
        binding.rvCountryHelpLine.adapter = adapterCountry
        binding.rvStateHelpLine.layoutManager = LinearLayoutManager(this)
        binding.rvStateHelpLine.adapter = adapterState

        binding.tvState.setOnClickListener {
            val states =
                helplinesResponse.countries[selectedCountryPosition].states?.map { it.name }
            if (states != null) {
                CountryListDialog(states, "States") { selected, position ->
                    stateList.clear()
                    selectedStatePosition = position
                    binding.tvState.text = selected
                    helplinesResponse.countries[selectedCountryPosition].states?.get(position)?.organizations
                        ?.let { it1 ->
                            stateList.addAll(
                                it1
                            )
                        }
                    adapterState.notifyDataSetChanged()
                    val visibility = if (stateList.isEmpty()) View.GONE else View.VISIBLE
                    binding.rlStateHelpLine.visibility = visibility
                    binding.rvStateHelpLine.visibility = visibility
                }.show(supportFragmentManager, "stateDialog")
            }
        }

        binding.tvCountry.setOnClickListener {
            val countries = helplinesResponse.countries.map { it.name }
            CountryListDialog(countries, "Countries") { selected, position ->
                countryList.clear()
                selectedCountryPosition = position
                binding.tvCountry.text = selected
                helplinesResponse.countries[position].organizations?.let { it1 ->
                    countryList.addAll(
                        it1
                    )
                }
                adapterCountry.notifyDataSetChanged()

                stateList.clear()
                selectedStatePosition = 0
                val states = helplinesResponse.countries[selectedCountryPosition].states

                if (states.isNullOrEmpty()) {
                    binding.rlStateHelpLine.isVisible = false
                    binding.rvStateHelpLine.isVisible = false
                } else {
                    binding.rlStateHelpLine.isVisible = true
                    binding.rvStateHelpLine.isVisible = true

                    val selectedState = states.getOrNull(selectedStatePosition)
                    binding.tvState.text = selectedState?.name
                    selectedState?.organizations?.let { stateList.addAll(it) }
                }
                adapterState.notifyDataSetChanged()

            }.show(supportFragmentManager, "countryDialog")
        }

    }


    private fun loadJSONFromAssets(): String? {
        return try {
            assets.open("helplines.json").bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}