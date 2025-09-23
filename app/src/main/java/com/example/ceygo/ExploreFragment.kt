package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.ui.ExploreAdapter
import com.example.ceygo.ui.location.SingleLocationFragment   // <— import the details fragment

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var adapter: ExploreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExploreAdapter { dest ->
            // Open details for this destination
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host, SingleLocationFragment.newInstance(dest.id))
                .addToBackStack("single_location")
                .commit()
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvPlaces)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        adapter.submit(LocationsRepository.locations())
    }
}
