package com.example.radiofinder.ui.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radiofinder.R
import com.example.radiofinder.services.PlayerService
import com.example.radiofinder.ui.details.DetailsActivity
import com.example.radiofinder.viewmodel.RadioViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RadioViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RadioStationAdapter
    private lateinit var loadingIndicator: View
    private var playerService: PlayerService? = null
    private var isBound = false

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.service
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            playerService = null
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupObservers()
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
        adapter = RadioStationAdapter { station ->
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("station", station)
            }
            startActivity(intent)
        }
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

    override fun onStart() {
        super.onStart()
        Intent(this, PlayerService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}
