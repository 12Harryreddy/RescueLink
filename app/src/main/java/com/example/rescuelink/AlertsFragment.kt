package com.example.rescuelink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rescuelink.adapters.AlertAdapter
import com.example.rescuelink.models.Alert

class AlertsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlertAdapter
    private lateinit var alertList: ArrayList<Alert>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_alerts, container, false)
        recyclerView = view.findViewById(R.id.alertsRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        alertList = arrayListOf(
            Alert("Flood in Assam", "2 hrs ago", "Stay indoors. Zone 2 evacuation.", "Ongoing"),
            Alert("Earthquake in Nepal", "1 day ago", "No tsunami warning. Aftershocks expected.", "Resolved"),
            Alert("Cyclone Warning", "5 hrs ago", "Landfall expected near coast. Stay alert.", "Ongoing"),
            Alert("Flood in Shoreline Park","2 hrs ago", "Stay Away from park area","Ongoing")
        )

        adapter = AlertAdapter(alertList)
        recyclerView.adapter = adapter
        return view
    }


}