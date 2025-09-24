package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.ui.NearbyAdapter
import com.example.ceygo.ui.PopularAdapter
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.ui.location.SingleLocationFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var popularAdapter: PopularAdapter
    private lateinit var nearbyAdapter: NearbyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Adapters now take Location and open SingleLocationFragment by id
        popularAdapter = PopularAdapter { loc ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host, SingleLocationFragment.newInstance(loc.id))
                .addToBackStack("single_location")
                .commit()
        }
        nearbyAdapter  = NearbyAdapter { loc ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host, SingleLocationFragment.newInstance(loc.id))
                .addToBackStack("single_location")
                .commit()
        }

        view.findViewById<RecyclerView>(R.id.rvPopular).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = popularAdapter
        }
        view.findViewById<RecyclerView>(R.id.rvNearby).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = nearbyAdapter
        }

        // Pull all seeded locations and split into "popular" & "nearby" as you like.
        // Example: popular = top 2 by rating, nearby = the rest.
        val all = LocationsRepository.locations()
        val popular = all.sortedByDescending { it.rating }.take(2)
        val nearby  = all.minus(popular)

        popularAdapter.submit(popular)
        nearbyAdapter.submit(nearby)

        view.findViewById<TextView>(R.id.tvHello).text = "Hello, Dileena"
    }
}