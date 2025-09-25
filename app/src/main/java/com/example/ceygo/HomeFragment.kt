package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.ui.NearbyAdapter
import com.example.ceygo.ui.PopularAdapter
import com.example.ceygo.ui.location.SingleLocationFragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.ceygo.data.firebase.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        // Observe Popular (top 5 avg from reviews) and Nearby (random 5)
        viewLifecycleOwner.lifecycleScope.launch {
            FirestoreRepository.observePopularTop5().collectLatest { list ->
                popularAdapter.submit(list)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            FirestoreRepository.observeNearbyRandom5().collectLatest { list ->
                nearbyAdapter.submit(list)
            }
        }

        // Greet current user if available
        val name = FirebaseAuth.getInstance().currentUser?.displayName
        if (!name.isNullOrBlank()) {
            view.findViewById<TextView>(R.id.tvHello).text = "Hello, $name"
            // Add the user image
            view.findViewById<ImageView>(R.id.imgAvatar).load(FirebaseAuth.getInstance().currentUser?.photoUrl)
        }
    }
}