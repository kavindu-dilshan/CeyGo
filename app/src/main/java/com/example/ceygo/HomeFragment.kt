package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ceygo.ui.NearbyAdapter
import com.example.ceygo.ui.PopularAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.model.Destination

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var popularAdapter: PopularAdapter
    private lateinit var nearbyAdapter: NearbyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        popularAdapter = PopularAdapter { /* TODO: navigate to detail */ }
        nearbyAdapter  = NearbyAdapter  { /* TODO: navigate to detail */ }

        view.findViewById<RecyclerView>(R.id.rvPopular).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = popularAdapter
        }
        view.findViewById<RecyclerView>(R.id.rvNearby).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = nearbyAdapter
        }

        // Faux data to mirror the mock
        popularAdapter.submit(
            listOf(
                Destination(
                    "Sigiriya",
                    "Sigiriya, Sri Lanka.",
                    5.0,
                    "https://picsum.photos/seed/sigiriya/600/400"
                ),
                Destination("Galle Fort", "Galle, Sri Lanka.", 4.9, "https://picsum.photos/seed/galle/600/400")
            )
        )
        nearbyAdapter.submit(
            listOf(
                Destination("Ambuluwawa Tower", "", 4.5, "https://picsum.photos/seed/ambuluwawa/600/600"),
                Destination("Sri Dalada Maligawa", "", 4.3, "https://picsum.photos/seed/maligawa/600/600")
            )
        )

        // Optional: change greeting name dynamically
        view.findViewById<TextView>(R.id.tvHello).text = "Hello, Dileena"
    }
}