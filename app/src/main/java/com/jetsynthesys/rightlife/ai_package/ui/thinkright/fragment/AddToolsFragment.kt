package com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.AddToolRequest
import com.jetsynthesys.rightlife.ai_package.model.BaseResponse
import com.jetsynthesys.rightlife.ai_package.model.ThinkQuoteResponse
import com.jetsynthesys.rightlife.ai_package.model.ToolsData
import com.jetsynthesys.rightlife.ai_package.model.ToolsResponse
import com.jetsynthesys.rightlife.databinding.FragmentAllToolsListBinding
import com.jetsynthesys.rightlife.ui.affirmation.TodaysAffirmationActivity
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ToolsAdapterList(private val context1: Context, private val items: List<ToolDisplayItem>, private val onItemClick: (Int, ToolsData?) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val contexts = context1

    companion object {
        private const val TYPE_TOOL = 0
        private const val TYPE_AFFIRMATION_CARD = 1
    }

    class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.tool_icon)
        val name: TextView = itemView.findViewById(R.id.tool_name)
        val description: TextView = itemView.findViewById(R.id.tool_description)
        val selectButton: ImageView = itemView.findViewById(R.id.tool_select_button)
    }

    class AffirmationCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val createPlaylist: TextView = itemView.findViewById(R.id.createPlaylistButton)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ToolDisplayItem.ToolItem -> TYPE_TOOL
            is ToolDisplayItem.AffirmationCard -> TYPE_AFFIRMATION_CARD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TOOL) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tools, parent, false)
            ToolViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_affirmation, parent, false)
            AffirmationCardViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ToolDisplayItem.ToolItem -> {
                val tool = item.data
                val toolHolder = holder as ToolViewHolder

                when (tool.title) {
                    "Affirmation" ->{
                        Glide.with(contexts)
                            .load("https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/dccba8e93b41dc3f856d7c18b13d5783.png")
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }
                    "Box Breathing" ->{
                        Glide.with(contexts)
                            .load("https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/62e1e78cca28251bf83e46bc0018ba0b.png")
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }
                    "Alternate Nostril Breathing" ->{
                        Glide.with(contexts)
                            .load( "https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/4a37e4954896bd3a53d90424be77a724.png")
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }
                    "Equal Breathing" ->{
                        Glide.with(contexts)
                            .load( "https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/4a37e4954896bd3a53d90424be77a724.png")
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }
                    "4-7-8 Breathing" ->{
                        Glide.with(contexts)
                            .load(  "https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/c88072eb32c47a1e385fae66696dc293.png")
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }
                    "Free Form" -> {
                        Glide.with(contexts)
                            .load(  R.drawable.ic_freeform_journal)
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }

                    "Bullet" -> {
                        Glide.with(contexts)
                            .load(  R.drawable.ic_bullet_journal)
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }

                    "Gratitude" -> {
                        Glide.with(contexts)
                            .load(  R.drawable.ic_gratitude_journal)
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }

                    "Grief" -> {
                        Glide.with(contexts)
                            .load(  R.drawable.ic_grief_journal)
                            .placeholder(R.drawable.ic_plus)
                            .into(holder.itemView.findViewById<ImageView>(R.id.tool_icon))
                    }
                }

                toolHolder.name.text = tool.title
                toolHolder.description.text = tool.desc

                toolHolder.selectButton.setImageResource(
                    if (tool.isSelectedModule == true)
                        R.drawable.green_check_item
                    else
                        R.drawable.add_think_brown_icon
                )

                toolHolder.selectButton.setOnClickListener {
                    onItemClick(position, tool)
                }

                val getVisit = SharedPreferenceManager.getInstance(contexts).userFirstVisit

                if (position == 0 && getVisit == "1") {
                   // holder.tooltipLayout.visibility = View.VISIBLE
                    showTooltipDialogSync( toolHolder.selectButton,"Quickly add it to your tools")
                } else {
                   // holder.tooltipLayout.visibility = View.GONE
                }
            }

            is ToolDisplayItem.AffirmationCard -> {
                val toolHolder = holder as AffirmationCardViewHolder
                toolHolder.createPlaylist.setOnClickListener {
                    onItemClick(position, null)
                }

            }
        }

    }

    override fun getItemCount(): Int = items.size

    fun showTooltipDialogSync(anchorView: View, tooltipText: String) {
        val dialog = Dialog(contexts)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltip_sync_layout)
        val tvTooltip = dialog.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip.text = tooltipText

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayMetrics = contexts.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val tooltipWidth = 250

        val params = dialog.window?.attributes
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        params?.y = location[1] - anchorView.height + 15
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.TOP)

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }
}

fun transformTools(tools: List<ToolsData>): List<ToolDisplayItem> {
    val result = mutableListOf<ToolDisplayItem>()
    var hasInsertedAffirmationCard = false

    for (tool in tools) {
        val isAffirmation = tool.title?.contains("affirmation", ignoreCase = true)

        // If it's an affirmation, insert the card once and skip the actual tool item
        if (isAffirmation == true) {
            if (!hasInsertedAffirmationCard) {
                result.add(ToolDisplayItem.AffirmationCard)
                hasInsertedAffirmationCard = true
            }
            continue // skip adding the original tool
        }

        result.add(ToolDisplayItem.ToolItem(tool))
    }

    return result
}

sealed class ToolDisplayItem {
    data class ToolItem(val data: ToolsData) : ToolDisplayItem()
    object AffirmationCard : ToolDisplayItem()
}

class FilterAdapter(private val filters: List<FilterItem>, private val onFilterClick: (Int,FilterItem) -> Unit) :
    RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val filterText: TextView = itemView.findViewById(R.id.filter_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.filterText.text = filter.name
        holder.filterText.setBackgroundResource(if (filter.isSelected) R.drawable.brown_bg else R.drawable.filter_white_bg)
        holder.filterText.setTextColor(if (filter.isSelected) Color.WHITE else Color.BLACK)

        holder.filterText.setOnClickListener {
            filters.forEach { it.isSelected = false }
            filters[position].isSelected = true
            notifyDataSetChanged()
            onFilterClick(position,filter)
        }

    }

    override fun getItemCount(): Int = filters.size
}


class AddToolsFragment: BaseFragment<FragmentAllToolsListBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var thinkQuoteResponse : ThinkQuoteResponse
    private lateinit var toolsResponse : ToolsResponse
    private lateinit var baseResponse: BaseResponse
    private lateinit var toolsAdapter: ToolsAdapterList
    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var filterAdapter: FilterAdapter
    val tools = mutableListOf<ToolDisplayItem>()
  //  var tools: ArrayList<ToolsData> = ArrayList()
   var toolsId = ""
    var userId =""
    var moduleName = ""
    var moduleId = ""
    var title = ""
    var moduleType = ""
    var categoryId = ""
    var filterName = "All"
    var isSelectedModule = false

    private val filters = mutableListOf(
        FilterItem("All", true),
        FilterItem("Breathing", false),
        FilterItem("Journaling", false),
        FilterItem("Affirmation", false)
    )

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAllToolsListBinding
        get() = FragmentAllToolsListBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        val backBtn = view.findViewById<ImageView>(R.id.img_back)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        backBtn.setOnClickListener {
            navigateToFragment(ThinkRightReportFragment(), "ThinkRightReportFragment")
        }

        val getVisit = SharedPreferenceManager.getInstance(requireContext()).userFirstVisit
        if (getVisit == ""){
            SharedPreferenceManager.getInstance(requireContext()).saveUserFirstVisit("1")
        }else{
            SharedPreferenceManager.getInstance(requireContext()).saveUserFirstVisit("2")
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(ThinkRightReportFragment(), "ThinkRightReportFragment")
            }
        })
        fetchToolsList("All")

        filterRecyclerView = view.findViewById(R.id.filter_recycler_view)
        filterRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        filterAdapter = FilterAdapter(filters) { position, filterItem ->
            if (filterItem.name == "All") {
                filterName = "All"
                fetchToolsList("All")
            }else  {
                tools.clear()
                filterName = filterItem.name
                fetchToolsList(filterItem.name)

            }
        }
        filterRecyclerView.adapter = filterAdapter

        toolsAdapter = ToolsAdapterList(requireContext(),tools) { position,toolsData ->
            if (toolsData!=null) {
                moduleId = toolsData._id ?: ""
                isSelectedModule = if (toolsData.isSelectedModule == true) false else true
                selectTools()
            }else{
                startActivity(Intent(requireContext(), TodaysAffirmationActivity::class.java))
            }
        }
        recyclerView.adapter = toolsAdapter
    }

    private fun fetchToolsList( filterKey:String) {
        progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
     //   val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdlM2ZiMjdiMzNlZGZkNzRlMDY5OWFjIiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiIiLCJsYXN0TmFtZSI6IiIsImRldmljZUlkIjoiVEUxQS4yNDAyMTMuMDA5IiwibWF4RGV2aWNlUmVhY2hlZCI6ZmFsc2UsInR5cGUiOiJhY2Nlc3MtdG9rZW4ifSwiaWF0IjoxNzQzMDU2OTEwLCJleHAiOjE3NTg3ODE3MTB9.gYLi895fpb4HGitALoGDRwHw3MIDCjYXTyqAKDNjS0A"
       // val userId = "67a5fae9197992511e71b1c8"
        val filter_key = filterKey
        if (filter_key == "All"){
            val call = ApiClient.apiService.fetchToolsListAll(token,filter_key)
            call.enqueue(object : Callback<ToolsResponse> {
                override fun onResponse(call: Call<ToolsResponse>, response: Response<ToolsResponse>) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        if (response.body()!=null) {
                            toolsResponse = response.body()!!
                                toolsResponse.data.let {
                                    tools.clear()
                                    tools.addAll(transformTools(it))
                                }
                            toolsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
                override fun onFailure(call: Call<ToolsResponse>, t: Throwable) {
                    Log.e("Error", "API call failed: ${t.message}")
                    Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })
        }else{
            val call = ApiClient.apiService.fetchToolsListAll(token,filter_key)
            call.enqueue(object : Callback<ToolsResponse> {
                override fun onResponse(call: Call<ToolsResponse>, response: Response<ToolsResponse>) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        if (response.body()!=null) {
                            toolsResponse = response.body()!!
                                toolsResponse.data.let {
                                    tools.clear()
                                    tools.addAll(transformTools(it))
                                }
                            toolsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
                override fun onFailure(call: Call<ToolsResponse>, t: Throwable) {
                    Log.e("Error", "API call failed: ${t.message}")
                    Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })
        }

    }

    private fun selectTools() {
        progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
       // val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdlM2ZiMjdiMzNlZGZkNzRlMDY5OWFjIiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiIiLCJsYXN0TmFtZSI6IiIsImRldmljZUlkIjoiVEUxQS4yNDAyMTMuMDA5IiwibWF4RGV2aWNlUmVhY2hlZCI6ZmFsc2UsInR5cGUiOiJhY2Nlc3MtdG9rZW4ifSwiaWF0IjoxNzQzMDU2OTEwLCJleHAiOjE3NTg3ODE3MTB9.gYLi895fpb4HGitALoGDRwHw3MIDCjYXTyqAKDNjS0A"
        val call = ApiClient.apiService.selectTools(token,AddToolRequest(moduleId = moduleId, isSelectedModule = isSelectedModule))
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    if (response.body()!=null) {
                        baseResponse = response.body()!!
                        Toast.makeText(requireContext(), "${baseResponse.successMessage}", Toast.LENGTH_SHORT).show()
                        toolsAdapter.notifyDataSetChanged()
                        fetchToolsList(filterName)
                        Log.e("Success", "Response is successful: ${response.isSuccessful}")
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
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
}

data class FilterItem(
    val name: String,
    var isSelected: Boolean
)

