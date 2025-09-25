package com.example.ceygo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.model.Location
import com.example.ceygo.ui.SavedAdapter
import com.example.ceygo.ui.location.SingleLocationFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.ceygo.data.firebase.FirestoreRepository
import android.widget.TextView
import coil.load
import com.google.firebase.auth.FirebaseAuth

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
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        // Observe saved ids and filter LocationsRepository list
        viewLifecycleOwner.lifecycleScope.launch {
            FirestoreRepository.observeSaved().collectLatest { ids ->
                if (ids.isEmpty()) {
                    adapter.submit(emptyList())
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    LocationsRepository.items.collectLatest { all ->
                        val filtered = all.filter { ids.contains(it.id) }
                        adapter.submit(filtered)
                        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
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