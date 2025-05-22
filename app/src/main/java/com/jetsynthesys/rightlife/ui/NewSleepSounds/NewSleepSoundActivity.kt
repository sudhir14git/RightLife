package com.jetsynthesys.rightlife.ui.NewSleepSounds

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityNewSleepSoundBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.AddPlaylistResponse
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.Service
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.SleepCategory
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.SleepCategoryResponse
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.SleepCategorySoundListResponse
import com.jetsynthesys.rightlife.ui.NewSleepSounds.userplaylistmodel.NewReleaseResponse
import com.jetsynthesys.rightlife.ui.NewSleepSounds.userplaylistmodel.SleepSoundPlaylistResponse
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewSleepSoundActivity : BaseActivity() {

    private lateinit var binding: ActivityNewSleepSoundBinding
    private lateinit var categoryAdapter: SleepCategoryAdapter
    private val categoryList = mutableListOf<SleepCategory>()
    private var sleepCategoryResponse: SleepCategoryResponse? = null
    private var selectedCategoryForTitle: SleepCategory? = null
    private var sleepSoundPlaylistResponse: SleepSoundPlaylistResponse? = null
    private var useplaylistdata: ArrayList<Service> = ArrayList()
    private var servicesList: ArrayList<Service> = ArrayList()
    private val mLimit = 10
    private var mSkip = 0
    private var isLoading = false
    private var isLastPage = false
    private var isForPlayList = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewSleepSoundBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        isForPlayList = intent.getStringExtra("PlayList").toString()

        //back button
        binding.iconBack.setOnClickListener {
            if (binding.layoutVerticalCategoryList.visibility == View.VISIBLE) {
                binding.layoutVerticalCategoryList.visibility = View.GONE
                binding.llMusicHome.visibility = View.VISIBLE
                binding.layouthorizontalMusicList.visibility = View.VISIBLE
                binding.recyclerViewHorizontalList.visibility = View.VISIBLE
                binding.recyclerViewVerticalList.visibility = View.GONE
                //fetchSleepSoundsByCategoryId(categoryList[1]._id, true)
                if (categoryAdapter != null) {
                    categoryAdapter.updateSelectedPosition(-1)
                }
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        setupCategoryRecyclerView()
        fetchCategories()
        getUserCreatedPlaylist()
        getNewReleases()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = SleepCategoryAdapter(categoryList) { selectedCategory ->
            // 🔥 Handle selected category here
            selectedCategoryForTitle = selectedCategory
            Log.d("SleepCategory", "Selected: ${selectedCategory.title}")
            Toast.makeText(this, "Selected: ${selectedCategory.title}", Toast.LENGTH_SHORT).show()
            // You can perform an action, like loading content specific to the category!
            mSkip = 0
            servicesList.clear()
            fetchSleepSoundsByCategoryId(selectedCategory._id, false, selectedCategory.title, mSkip)
        }

        binding.recyclerCategory.apply {
            layoutManager = LinearLayoutManager(
                this@NewSleepSoundActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
        }
    }


    private fun fetchCategories() {
        Utils.showLoader(this)
        val call = apiService.getSleepCategories(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<SleepCategoryResponse> {
            override fun onResponse(
                call: Call<SleepCategoryResponse>,
                response: Response<SleepCategoryResponse>
            ) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                if (response.isSuccessful && response.body() != null) {
                    sleepCategoryResponse = response.body()

                    categoryList.clear()
                    sleepCategoryResponse?.let { categoryList.addAll(it.data) }
                    categoryAdapter.notifyDataSetChanged()
                    if (categoryList.isNotEmpty()) {
                        for (category in categoryList) {
                            fetchSleepSoundsByCategoryId(category._id, true, category.title, 0)
                        }
                    }
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<SleepCategoryResponse>, t: Throwable) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchSleepSoundsByCategoryId(
        categoryId: String,
        isForHome: Boolean,
        title: String,
        skip: Int
    ) {
        isLoading = true
        Utils.showLoader(this)

        val call = apiService.getSleepSoundsById(
            sharedPreferenceManager.accessToken,
            categoryId,
            skip,
            mLimit,
            "catagory"
        )


        call.enqueue(object : Callback<SleepCategorySoundListResponse> {
            override fun onResponse(
                call: Call<SleepCategorySoundListResponse>,
                response: Response<SleepCategorySoundListResponse>
            ) {
                isLoading = false
                Utils.dismissLoader(this@NewSleepSoundActivity)
                if (response.isSuccessful && response.body() != null) {
                    val soundData = response.body()
                    Log.d("SleepSound", "Data: ${soundData?.data?.services}")
                    // Pass soundData.data.services to adapter
                    if (isForHome) {
                        binding.llMusicHome.visibility = View.VISIBLE
                        binding.layouthorizontalMusicList.visibility = View.VISIBLE
                        binding.layoutVerticalCategoryList.visibility = View.GONE
                        //setupHorizontalRecyclerView(soundData?.data?.services)
                        soundData?.data?.services?.let { addServicesSection(it, "" + title) }
                    } else {
                        binding.llMusicHome.visibility = View.GONE
                        binding.layouthorizontalMusicList.visibility = View.GONE
                        binding.layoutVerticalCategoryList.visibility = View.VISIBLE
                        soundData?.data?.services?.let { servicesList.addAll(it) }
                        setupVerticleRecyclerView(servicesList)
                    }

                } else {
                    showToast("Server Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SleepCategorySoundListResponse>, t: Throwable) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                isLoading = false
                handleNoInternetView(t)
            }
        })
    }

    private fun setupHorizontalRecyclerView(services: ArrayList<Service>?) {
        val adapter = services?.let { serviceList ->
            SleepHorizontalListAdapter(
                serviceList,
                onItemClick = { selectedList, position ->
                    // Handle item click (open player screen)
                    startActivity(Intent(this, SleepSoundPlayerActivity::class.java).apply {
                        putExtra("SOUND_LIST", selectedList)
                        putExtra("SELECTED_POSITION", position)
                        putExtra("ISUSERPLAYLIST", false)
                    })
                },
                onAddToPlaylistClick = { service, position ->
                    // Handle add to playlist click here
                    if (service.isActive)
                        addToPlaylist(service._id, position)
                    else
                        removeFromPlaylist(service._id, position)
                    //Toast.makeText(this, "Added to playlist in Activity", Toast.LENGTH_SHORT).show()
                }
            )

        }

        binding.recyclerViewVerticalList.visibility = View.GONE

        binding.layoutVerticalCategoryList.visibility = View.GONE
        binding.layouthorizontalMusicList.visibility = View.VISIBLE
        binding.recyclerViewHorizontalList.visibility = View.VISIBLE

        binding.recyclerViewHorizontalList.apply {
            layoutManager = LinearLayoutManager(
                this@NewSleepSoundActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            this.adapter = adapter
        }
    }

    private fun setupYourPlayListRecyclerView(services: ArrayList<Service>?) {
        val adapter = services?.let { serviceList ->
            SleepHorizontalListFullAdapter(
                serviceList,
                "Playlist",
                onItemClick = { selectedList, position ->
                    // Handle item click (open player screen)
                    startActivity(Intent(this, SleepSoundPlayerActivity::class.java).apply {
                        putExtra("SOUND_LIST", selectedList)
                        putExtra("SELECTED_POSITION", position)
                        putExtra("ISUSERPLAYLIST", true)
                    })
                },
                onAddToPlaylistClick = { service, position ->
                    if (service.isActive)
                        addToPlaylist(service._id, position)
                    else
                        removeFromPlaylist(service._id, position)
                }
            )

        }

        binding.recyclerViewPlayList.apply {
            layoutManager = LinearLayoutManager(
                this@NewSleepSoundActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            this.adapter = adapter
        }
        adapter?.notifyDataSetChanged()
        binding.tvYourPlayList.visibility = View.VISIBLE
    }

    private fun setupNewReleaseRecyclerView(services: ArrayList<Service>?) {
        val adapter = services?.let { serviceList ->
            SleepHorizontalListFullAdapter(
                serviceList,
                onItemClick = { selectedList, position ->
                    // Handle item click (open player screen)
                    startActivity(Intent(this, SleepSoundPlayerActivity::class.java).apply {
                        putExtra("SOUND_LIST", selectedList)
                        putExtra("SELECTED_POSITION", position)
                        putExtra("ISUSERPLAYLIST", false)
                    })
                },
                onAddToPlaylistClick = { service, position ->
                    // Handle add to playlist click here
                    if (service.isActive)
                        addToPlaylist(service._id, position)
                    else
                        removeFromPlaylist(service._id, position)
                }
            )

        }

        binding.recyclerViewNewRelease.apply {
            layoutManager = LinearLayoutManager(
                this@NewSleepSoundActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            this.adapter = adapter
        }
    }


    private fun setupVerticleRecyclerView(services: ArrayList<Service>?) {
        val adapter = services?.let { serviceList ->
            SleepSoundGridAdapter(
                serviceList,
                onItemClick = { selectedList, position ->
                    // Handle item click (open player screen)
                    startActivity(Intent(this, SleepSoundPlayerActivity::class.java).apply {
                        putExtra("SOUND_LIST", selectedList)
                        putExtra("SELECTED_POSITION", position)
                        putExtra("ISUSERPLAYLIST", false)
                    })
                },
                onAddToPlaylistClick = { service, position ->
                    // Handle add to playlist click here
                    if (service.isActive)
                        addToPlaylist(service._id, position)
                    else
                        removeFromPlaylist(service._id, position)
                }
            )

        }

        binding.categorytTitle.text = selectedCategoryForTitle?.title
        binding.categorytTitle.visibility = View.VISIBLE
        binding.recyclerViewVerticalList.visibility = View.VISIBLE
        binding.categorytTitleDesciption.text = selectedCategoryForTitle?.subtitle
        binding.categorytTitleDesciption.visibility = View.VISIBLE

        binding.layoutVerticalCategoryList.visibility = View.VISIBLE
        binding.layouthorizontalMusicList.visibility = View.GONE

        binding.recyclerViewVerticalList.apply {
            layoutManager = GridLayoutManager(this@NewSleepSoundActivity, 2)
            this.adapter = adapter
        }

        binding.recyclerViewVerticalList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                isLastPage = servicesList.size < mSkip
                val isNotLoadingAndNotLastPage = !isLoading && !isLastPage

                if (isNotLoadingAndNotLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= mLimit
                    ) {
                        mSkip += mLimit
                        fetchSleepSoundsByCategoryId(
                            selectedCategoryForTitle?._id!!,
                            false,
                            selectedCategoryForTitle?.title!!,
                            mSkip
                        )
                    }
                }

            }
        })

    }


    // Add Sleep sound to using playlist api
    private fun addToPlaylist(songId: String, position: Int) {
        Utils.showLoader(this)
        val call = apiService.addToPlaylist(sharedPreferenceManager.accessToken, songId)

        call.enqueue(object : Callback<AddPlaylistResponse> {
            override fun onResponse(
                call: Call<AddPlaylistResponse>,
                response: Response<AddPlaylistResponse>
            ) {
                getUserCreatedPlaylist()
                Utils.dismissLoader(this@NewSleepSoundActivity)
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()?.successMessage ?: "Added to Playlist!")
                } else {
                    showToast("Failed to add to playlist: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AddPlaylistResponse>, t: Throwable) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun removeFromPlaylist(songId: String, position: Int) {
        Utils.showLoader(this)
        val call = apiService.removeFromPlaylist(sharedPreferenceManager.accessToken, songId)

        call.enqueue(object : Callback<AddPlaylistResponse> {
            override fun onResponse(
                call: Call<AddPlaylistResponse>,
                response: Response<AddPlaylistResponse>
            ) {
                getUserCreatedPlaylist()
                Utils.dismissLoader(this@NewSleepSoundActivity)
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()?.successMessage ?: "Song removed from Playlist!")
                } else {
                    showToast("try again!: ${response.code()}")
                }

            }

            override fun onFailure(call: Call<AddPlaylistResponse>, t: Throwable) {
                //Utils.dismissLoader(this@NewSleepSoundActivity)
                showToast("Network Error: ${t.message}")
            }
        })
    }

    // get user play list from api
    private fun getUserCreatedPlaylist() {
        Utils.showLoader(this)
        val call = apiService.getUserCreatedPlaylist(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<SleepSoundPlaylistResponse> {
            override fun onResponse(
                call: Call<SleepSoundPlaylistResponse>,
                response: Response<SleepSoundPlaylistResponse>
            ) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                useplaylistdata.clear()
                if (response.isSuccessful && response.body() != null) {
                    sleepSoundPlaylistResponse = response.body()
                    useplaylistdata = sleepSoundPlaylistResponse?.data as ArrayList<Service>
                    if (sleepSoundPlaylistResponse?.data?.isNotEmpty() == true) {
                        if (isForPlayList == "ForPlayList") {
                            startActivity(
                                Intent(
                                    this@NewSleepSoundActivity,
                                    SleepSoundPlayerActivity::class.java
                                ).apply {
                                    putExtra("SOUND_LIST", useplaylistdata)
                                    putExtra("SELECTED_POSITION", 0)
                                    putExtra("ISUSERPLAYLIST", true)
                                })
                            finish()
                        }
                        setupYourPlayListRecyclerView(useplaylistdata)
                    } else {
                        showToast("No playlist data available")
                    }
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<SleepSoundPlaylistResponse>, t: Throwable) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                handleNoInternetView(t)
            }

        })
    }

    // get New Release from api
    private fun getNewReleases() {
        Utils.showLoader(this)
        val call = apiService.getNewReleases(sharedPreferenceManager.accessToken, "recommended")

        call.enqueue(object : Callback<NewReleaseResponse> {
            override fun onResponse(
                call: Call<NewReleaseResponse>,
                response: Response<NewReleaseResponse>
            ) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                if (response.isSuccessful && response.body() != null) {
                    if (response.body()?.data?.services?.isNotEmpty() == true) {
                        setupNewReleaseRecyclerView(response.body()?.data?.services?.let {
                            ArrayList(
                                it
                            )
                        })
                    } else {
                        showToast("No Releases data available")
                        binding.layoutNewRelease.visibility = View.GONE
                    }
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<NewReleaseResponse>, t: Throwable) {
                Utils.dismissLoader(this@NewSleepSoundActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun addServicesSection(services: ArrayList<Service>, categoryName: String) {
        val container = binding.linearLayoutContainer  // Your LinearLayout from XML

        // Optional: Clear existing views if you want fresh list every time
        //container.removeAllViews()

        // 1. Inflate the layout containing TextView + RecyclerView
        val sectionView =
            layoutInflater.inflate(R.layout.item_section_layout_musiclisthome, container, false)

        // 2. Set the title (You can make this dynamic too if needed)
        val titleTextView = sectionView.findViewById<TextView>(R.id.categorytTitleHorizontal)
        titleTextView.text = categoryName // Or set it from function parameter if dynamic

        // 3. Setup horizontal RecyclerView
        val recyclerView =
            sectionView.findViewById<RecyclerView>(R.id.recycler_view_horizontal_list)
        recyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // 4. Setup adapter
        val adapter = SleepHorizontalListAdapter(
            services,
            onItemClick = { selectedList, position ->
                startActivity(Intent(this, SleepSoundPlayerActivity::class.java).apply {
                    putExtra("SOUND_LIST", selectedList)
                    putExtra("SELECTED_POSITION", position)
                    putExtra("ISUSERPLAYLIST", false)
                })
            },
            onAddToPlaylistClick = { service, position ->
                if (service.isActive)
                    addToPlaylist(service._id, position)
                else
                    removeFromPlaylist(service._id, position)
            }
        )
        recyclerView.adapter = adapter

        // 5. Add the section view to container
        container.addView(sectionView)
    }

}
