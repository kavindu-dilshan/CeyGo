package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.model.Destination
import com.example.ceygo.ui.SavedAdapter

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private lateinit var adapter: SavedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SavedAdapter { dest ->
            Toast.makeText(requireContext(), "Clicked: ${dest.title}", Toast.LENGTH_SHORT).show()
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvSaved)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Demo saved list
        val list = listOf(
            Destination("Ruwanweli Maha Seya", "Anuradhapura, Sri Lanka.", 4.9, "https://picsum.photos/seed/ruwanweli/800/500"),
            Destination("Horton Plains", "Nuwara Eliya, Sri Lanka.", 4.8, "https://picsum.photos/seed/horton2/800/500")
        )
        adapter.submit(list)
    }
}