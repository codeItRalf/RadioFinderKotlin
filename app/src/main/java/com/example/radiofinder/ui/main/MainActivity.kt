package com.example.radiofinder.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.services.ServiceConnectionManager
import com.example.radiofinder.ui.details.DetailsActivity
import com.example.radiofinder.viewmodel.RadioViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var serviceConnectionManager: ServiceConnectionManager
    private lateinit var floatingController: View
    private lateinit var controllerStationName: TextView
    private lateinit var controllerPlayPauseButton: ImageView
    private lateinit var viewModel: RadioViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RadioStationAdapter
    private lateinit var loadingIndicator: View
    private lateinit var controllerLoadingIndicator: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the ServiceConnectionManager
        serviceConnectionManager = ServiceConnectionManager(this)

        floatingController = findViewById(R.id.floating_station_controller)
        controllerStationName = floatingController.findViewById(R.id.controller_station_name)
        controllerPlayPauseButton =
            floatingController.findViewById(R.id.controller_play_pause_button)
        controllerLoadingIndicator =
            floatingController.findViewById(R.id.controller_loading_indicator)
        // Set play/pause button click listener
        controllerPlayPauseButton.setOnClickListener {
            playPause(null)
        }

        setupToolbar()
        setupViewModel()
        setupObservers()
        setupRecyclerView()
        setupScrollListener()

        viewModel.searchStations("") // Initial search example
    }

    private fun setupToolbar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[RadioViewModel::class.java]
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RadioStationAdapter(
            { station ->
                val intent = Intent(this, DetailsActivity::class.java).apply {
                    putExtra("station", station)
                }
                startActivity(intent)
            },
            { station ->
                playPause(station)
            },
        )
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        loadingIndicator = findViewById(R.id.loadingIndicator)

        viewModel.stations.observe(this) { stations ->
            val filteredStations = stations.filter {
                !it.name.isNullOrBlank() && !it.resolvedUrl.isNullOrBlank()
            }
            adapter.submitList(filteredStations)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        serviceConnectionManager.bindService { playerService ->
            playerService?.currentStation?.observe(this, Observer { station ->
                updateControllerUI(station, playerService.isPlaying())
                adapter.setCurrentStation(station)
            })

            playerService?.isPlaying?.observe(this, Observer { playing ->
                updateControllerUI(playerService.getStation(), playing)
                adapter.setIsPlaying(playing)
                if (playing) {
                    controllerPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                } else {
                    controllerPlayPauseButton.setImageResource(android.R.drawable.ic_media_play)
                }
            })

            playerService?.isLoading?.observe(this, Observer { loading ->
                adapter.setIsLoading(loading)
                if (loading) {
                    controllerPlayPauseButton.visibility = View.GONE
                    controllerLoadingIndicator.visibility = View.VISIBLE
                } else {
                    controllerPlayPauseButton.visibility = View.VISIBLE
                    controllerLoadingIndicator.visibility = View.GONE
                }
            })
        }
    }

    private fun updateControllerUI(currentStation: RadioStation?, isPlaying: Boolean) {
        if (currentStation != null) {
            controllerStationName.text = currentStation.name
            controllerPlayPauseButton.setImageResource(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            floatingController.visibility = View.VISIBLE
        } else {
            floatingController.visibility = View.GONE
        }
    }


    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        setupSearch(menu)
        return true
    }

    private fun setupSearch(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    viewModel.searchStations(newText ?: "")
                }
                handler.postDelayed(searchRunnable!!, 500)
                return true
            }
        })
    }

    private fun playPause(station: RadioStation?) {
        val playerService = serviceConnectionManager.getService()
        playerService?.playPause(station)
    }


    override fun onStop() {
        super.onStop()
        serviceConnectionManager.unbindService()
    }


}
