package app.codeitralf.radiofinder.ui.main

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.codeitralf.radiofinder.R
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.repository.RadioRepository
import app.codeitralf.radiofinder.services.PlayerService
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import app.codeitralf.radiofinder.ui.details.DetailsActivity
import app.codeitralf.radiofinder.utils.AppInfo
import app.codeitralf.radiofinder.viewmodel.RadioViewModel
import app.codeitralf.radiofinder.views.ExoVisualizer
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var serviceConnectionManager: ServiceConnectionManager
    private lateinit var floatingController: View
    private lateinit var controllerStationName: TextView
    private lateinit var controllerImage: ImageView
    private lateinit var controllerPlayPauseButton: ImageView
    private lateinit var viewModel: RadioViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RadioStationAdapter
    private lateinit var loadingIndicator: View
    private lateinit var controllerLoadingIndicator: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private  var visualizer: ExoVisualizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppInfo.init(this)

        setContentView(R.layout.activity_main)

        // Initialize the ServiceConnectionManager
        serviceConnectionManager = ServiceConnectionManager(this)
        initializeViews()
        setupFloatingController()

        setupToolbar()
        setupViewModel()
        setupObservers()
        setupRecyclerView()
        setupScrollListener()

        viewModel.searchStations("") // Initial search example
    }

    private fun initializeViews() {
        floatingController = findViewById(R.id.floating_station_controller)
        controllerStationName = floatingController.findViewById(R.id.controller_station_name)
        controllerImage = floatingController.findViewById(R.id.station_image)
        controllerPlayPauseButton = floatingController.findViewById(R.id.controller_play_pause_button)
        controllerLoadingIndicator = floatingController.findViewById(R.id.controller_loading_indicator)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        recyclerView = findViewById(R.id.recyclerView)
        visualizer = findViewById(R.id.visualizer)
    }

    private fun setupFloatingController() {
        // Set play/pause button click listener
        controllerPlayPauseButton.setOnClickListener {
            Log.d("MainActivity", "Play/Pause button clicked")
            playPause(null)
        }
        floatingController.setOnClickListener {
            Log.d("MainActivity", "Floating controller clicked")
            serviceConnectionManager.getService()?.getStation()?.let {
                openStationDetails(it)
            }
        }
    }

    private fun setupToolbar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[RadioViewModel::class.java]
    }

    private fun setupRecyclerView() {

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RadioStationAdapter(
            { station ->
                openStationDetails(station)
            },
            { station ->
                playPause(station)
            },
        )
        recyclerView.adapter = adapter
    }

    private fun openStationDetails(station: RadioStation) {
        val intent = Intent(this, DetailsActivity::class.java).apply {
            putExtra("station", station)
        }
        startActivity(intent)
    }

    private fun setupObservers() {
        setupStationsObserver()
        setupLoadingObserver()
        setupServiceObservers()
    }

    private fun setupStationsObserver() {
        viewModel.stations.observe(this) { stations ->
            val filteredStations = stations.filter {
                !it.name.isNullOrBlank() && !it.resolvedUrl.isNullOrBlank()
            }
            adapter.submitList(filteredStations)
        }
    }

    private fun setupLoadingObserver() {
        viewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupServiceObservers() {
        serviceConnectionManager.bindService { playerService ->
            playerService?.let {
                it.currentStation.observe(this@MainActivity, Observer { station ->
                    handleStationUpdate(station, it)
                })

                visualizer?.processor = it.getAudioProcessor()
                visualizer?.updateProcessorListenerState(true)

                it.isPlaying.observe(this@MainActivity, Observer { playing ->
                    handlePlayingUpdate(it, playing)
                })

                it.isLoading.observe(this@MainActivity, Observer { loading ->
                    handleLoadingUpdate(loading)
                })
            }
        }
    }

    private fun handleStationUpdate(station: RadioStation?, playerService: PlayerService) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                station?.let {
                    RadioRepository.getInstance().clickCounter(it.stationUuid)
                }
            } catch (e: Exception) {
                // Log the error if needed
            }
        }

        updateControllerUI(station, playerService.isPlaying())
        adapter.setCurrentStation(station)
    }

    private fun handlePlayingUpdate(playerService: PlayerService, playing: Boolean) {
        updateControllerUI(playerService.getStation(), playing)
        adapter.setIsPlaying(playing)
        if (playing) {
            controllerPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            controllerPlayPauseButton.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    private fun handleLoadingUpdate(loading: Boolean) {
        adapter.setIsLoading(loading)
        if (loading) {
            controllerPlayPauseButton.visibility = View.GONE
            controllerLoadingIndicator.visibility = View.VISIBLE
        } else {
            controllerPlayPauseButton.visibility = View.VISIBLE
            controllerLoadingIndicator.visibility = View.GONE
        }
    }


    private fun updateControllerUI(currentStation: RadioStation?, isPlaying: Boolean) {
        if (currentStation != null) {
            if (!currentStation.favicon.isNullOrBlank()) {
                try {
                Picasso.get().load(currentStation.favicon).into(controllerImage)

                }catch (e: Exception){
                    Log.e("MainActivity", "Error loading image", e)
                }
            }

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
        setupSearchIcon(searchItem)
        setupSearchView(searchItem.actionView as SearchView)
    }

    private fun setupSearchIcon(searchItem: MenuItem) {
        val searchIcon = searchItem.icon
        searchIcon?.mutate() // Create a new drawable instance so that all icons don't change
        val color = ContextCompat.getColor(this, R.color.neon_pink)
        val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        searchIcon?.colorFilter = colorFilter
    }

    private fun setupSearchView(searchView: SearchView) {
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


    override fun onDestroy() {
        serviceConnectionManager.getService()?.stopMedia();
        serviceConnectionManager.unbindService()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            visualizer?.updateProcessorListenerState(true)
        }, 100)
    }


    override fun onPause() {
        super.onPause()

        visualizer?.updateProcessorListenerState(false)
    }
}
