package com.example.radiofinder.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radiofinder.R
import com.example.radiofinder.ui.details.DetailsActivity
import com.example.radiofinder.viewmodel.RadioViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RadioViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RadioStationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[RadioViewModel::class.java]
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RadioStationAdapter { station ->
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("station", station)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        viewModel.stations.observe(this) { stations ->
            adapter.submitList(stations)
        }

        viewModel.fetchStations("pop", 10, 0)
    }
}
