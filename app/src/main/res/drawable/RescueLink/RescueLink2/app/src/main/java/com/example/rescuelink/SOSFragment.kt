package com.example.rescuelink

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SOSFragment : Fragment(R.layout.fragment_s_o_s) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val phoneNumber = "+917018138284" // Replace with dynamic contact fetching in future

    // Register permission launcher for multiple permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false

        when {
            locationGranted && smsGranted -> {
                sendEmergencyLocation()
            }
            !locationGranted -> {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
            !smsGranted -> {
                Toast.makeText(requireContext(), "SMS permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val sosButton = view.findViewById<Button>(R.id.btn_sos)
        sosButton.setOnClickListener {
            checkPermissionsAndSendSOS()
        }

        // Optionally request on start
        // requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS))
    }

    private fun checkPermissionsAndSendSOS() {
        val locationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val smsPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)

        if (locationPermission == PackageManager.PERMISSION_GRANTED &&
            smsPermission == PackageManager.PERMISSION_GRANTED
        ) {
            sendEmergencyLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS
                )
            )
        }
    }

    private fun sendEmergencyLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                val message = if (location != null) {
                    "🚨 EMERGENCY ALERT 🚨\nPlease help me. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    "🚨 EMERGENCY ALERT 🚨\nI need help but couldn't fetch my location."
                }
                sendSms(phoneNumber, message)
            }.addOnFailureListener {
                sendSms(phoneNumber, "🚨 EMERGENCY ALERT 🚨\nFailed to get location. Please help!")
                Toast.makeText(requireContext(), "Failed to fetch location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("SOSFragment", "Permission error: ${e.message}", e)
            Toast.makeText(requireContext(), "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                requireContext().getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(requireContext(), "🚨 SOS Sent!", Toast.LENGTH_SHORT).show()
            Log.d("SOSFragment", "SMS sent: $message")
        } catch (e: Exception) {
            Log.e("SOSFragment", "Failed to send SMS: ${e.message}", e)
            Toast.makeText(requireContext(), "Failed to send SOS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
