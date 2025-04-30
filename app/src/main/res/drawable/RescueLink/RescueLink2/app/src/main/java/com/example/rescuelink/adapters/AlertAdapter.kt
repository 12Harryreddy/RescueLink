package com.example.rescuelink.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rescuelink.R
import com.example.rescuelink.models.Alert

class AlertAdapter(private  val alerts: List<Alert>) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.alertTitle)
        val time: TextView = itemView.findViewById(R.id.alertTime)
        val description = itemView.findViewById<TextView>(R.id.alertDescription)
        val status : TextView = itemView.findViewById<TextView>(R.id.alertStatus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerts, parent,false)

        return AlertViewHolder(view);
    }

    override fun getItemCount(): Int {
        return  alerts.size
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.title.text = alert.title
        holder.time.text = alert.time
        holder.status.text = alert.status
        holder.description.text = alert.description

        holder.status.setTextColor(
            if(alert.status.lowercase() == "resolved")
                Color.parseColor("#388E3C") else Color.parseColor("#D32F2F")
        )
    }
}