package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.ui.ExploreAdapter
import com.example.ceygo.ui.location.SingleLocationFragment   // <— import the details fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.ceygo.data.firebase.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var adapter: ExploreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExploreAdapter(
            onClick = { dest ->
            // Open details for this destination
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host, SingleLocationFragment.newInstance(dest.id))
                .addToBackStack("single_location")
                .commit()
            },
            onToggleSave = { loc ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try { FirestoreRepository.toggleSaved(loc.id) } catch (_: Exception) { }
                }
            }
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvPlaces)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        adapter.submit(LocationsRepository.locations())

        // Observe saved set and update icons
        viewLifecycleOwner.lifecycleScope.launch {
            FirestoreRepository.observeSaved().collectLatest { ids ->
                adapter.setSaved(ids)
            }
        }

        // Greet current user if available
        val name = FirebaseAuth.getInstance().currentUser?.displayName
        if (!name.isNullOrBlank()) {
            view.findViewById<TextView>(R.id.tvHello).text = "Hello, $name"
            view.findViewById<ImageView>(R.id.imgAvatar).load(FirebaseAuth.getInstance().currentUser?.photoUrl)
        }
    }
}
