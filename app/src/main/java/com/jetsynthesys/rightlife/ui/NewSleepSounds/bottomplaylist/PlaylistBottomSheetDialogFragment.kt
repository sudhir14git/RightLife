package com.jetsynthesys.rightlife.ui.NewSleepSounds.bottomplaylist

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.BottomSheetPlaylistBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.AddPlaylistResponse
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.Service
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistBottomSheetDialogFragment(
    private var soundList: ArrayList<Service>,
    private val currentIndex: Int,
    private val onSongSelected: (position: Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var binding: BottomSheetPlaylistBinding
    private lateinit var adapter: PlaylistAdapter

    //private val mutableSoundList = soundList.toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPlaylistBinding.inflate(inflater, container, false)

        sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        adapter = PlaylistAdapter(soundList, currentIndex, { selectedPosition ->
            onSongSelected(selectedPosition)
            dismiss()
        }, { removedPosition ->
            // Get the song ID before removal
            val songId = soundList[removedPosition]._id

            // Remove from server
            removeFromPlaylist(songId, removedPosition)

            // Remove from list
            soundList.removeAt(removedPosition)

            // CRITICAL: Update currentIndex based on removal
            if (removedPosition < adapter.currentIndex) {
                // Item removed before current playing item, shift index down
                adapter.currentIndex -= 1
            } else if (removedPosition == adapter.currentIndex) {
                // Removed the currently playing item
                adapter.currentIndex = -1  // No item playing now
            }
            // If removedPosition > currentIndex, no change needed

            // Notify adapter
            adapter.notifyItemRemoved(removedPosition)
            adapter.notifyItemRangeChanged(removedPosition, soundList.size)
        })


        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.playlistRecyclerView.adapter = adapter

        enableSwipeToDelete()

        return binding.root
    }

    private fun enableSwipeToDelete() {

        val swipeBackground = ColorDrawable(Color.parseColor("#FF3C3C")) // Red color

        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.delete_journal)!!


        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(

                    recyclerView: RecyclerView,

                    viewHolder: RecyclerView.ViewHolder,

                    target: RecyclerView.ViewHolder

                ): Boolean = false


                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    adapter.removeItem(viewHolder.adapterPosition)

                }


                override fun onChildDraw(

                    c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,

                    dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean

                ) {

                    val itemView = viewHolder.itemView



                    swipeBackground.setBounds(

                        itemView.right + dX.toInt(),

                        itemView.top,

                        itemView.right,

                        itemView.bottom

                    )

                    swipeBackground.draw(c)


                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                    val iconTop = itemView.top + iconMargin

                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth

                    val iconRight = itemView.right - iconMargin

                    val iconBottom = iconTop + deleteIcon.intrinsicHeight



                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    //deleteIcon.draw(c)



                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                }

            })



        itemTouchHelper.attachToRecyclerView(binding.playlistRecyclerView)

    }

    private fun removeFromPlaylist(songId: String, position: Int) {
        //Utils.showLoader(context)
        val apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val call = apiService.removeFromPlaylist(sharedPreferenceManager.accessToken, songId)

        call.enqueue(object : Callback<AddPlaylistResponse> {
            override fun onResponse(
                call: Call<AddPlaylistResponse>,
                response: Response<AddPlaylistResponse>
            ) {
              //  Utils.dismissLoader(this@NewSleepSoundActivity)
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()?.successMessage ?: "Song removed from Playlist!")
                    Utils.showNewDesignToast(context, "Removed From Playlist", false)
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

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
