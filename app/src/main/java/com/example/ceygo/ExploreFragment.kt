package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.model.Destination
import com.example.ceygo.ui.ExploreAdapter

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var adapter: ExploreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExploreAdapter { dest ->
            Toast.makeText(requireContext(), "Clicked: ${dest.title}", Toast.LENGTH_SHORT).show()
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvPlaces)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Fake data
        val list = listOf(
            Destination("Mirissa Beach", "Mirissa, Sri Lanka.", 5.0, "https://picsum.photos/seed/mirissa/800/500"),
            Destination("Horton Plains", "Nuwara Eliya, Sri Lanka.", 4.8, "https://picsum.photos/seed/horton/800/500"),
            Destination("Ella Rock", "Badulla, Sri Lanka.", 4.9, "https://picsum.photos/seed/ella/800/500")
        )
        adapter.submit(list)
    }
}
