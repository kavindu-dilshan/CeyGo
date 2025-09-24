package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.model.Location
import com.example.ceygo.ui.SavedAdapter
import com.example.ceygo.ui.location.SingleLocationFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private lateinit var adapter: SavedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SavedAdapter { loc: Location ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host, SingleLocationFragment.newInstance(loc.id))
                .addToBackStack("single_location")
                .commit()
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvSaved)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Demo: show all locations as "saved".
        // Replace with your real saved filter when available.
        viewLifecycleOwner.lifecycleScope.launch {
            LocationsRepository.items.collectLatest { all ->
                adapter.submit(all)
            }
        }
    }
}