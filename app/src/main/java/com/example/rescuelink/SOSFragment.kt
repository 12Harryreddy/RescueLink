package com.example.rescuelink

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SOSFragment : Fragment(R.layout.fragment_s_o_s) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private fun getSavedContacts(): Set<String> {
        val prefs = requireContext().getSharedPreferences("contacts_pref", Context.MODE_PRIVATE)
        return prefs.getStringSet("contact_numbers", emptySet()) ?: emptySet()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                sendEmergencyLocation()
            } else {
                Toast.makeText(requireContext(), "All permissions are required to send SOS", Toast.LENGTH_SHORT).show()
            }
        }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasSmsPermission() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    private fun sendEmergencyLocation() {
        if (!hasLocationPermission() || !hasSmsPermission()) {
            Toast.makeText(requireContext(), "Missing necessary permissions", Toast.LENGTH_SHORT).show()
            return
        }

        val contacts = getSavedContacts()
        if (contacts.isEmpty()) {
            Toast.makeText(requireContext(), "No emergency contacts found!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val message = if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    "🚨 EMERGENCY ALERT 🚨\nI need help! My location: https://maps.google.com/?q=$lat,$lng"
                } else {
                    "🚨 EMERGENCY ALERT 🚨\nI need help, but my location couldn't be determined."
                }

                for (number in contacts) {
                    sendSms(number, message)
                }

            }.addOnFailureListener {
                Log.e("SOSFragment", "Failed to get location: ${it.message}")
                val fallbackMessage = "🚨 EMERGENCY ALERT 🚨\nI need help, and my location couldn't be retrieved!"
                for (number in contacts) {
                    sendSms(number, fallbackMessage)
                }
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
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
            Toast.makeText(requireContext(), "🚨 SOS Sent to $phoneNumber!", Toast.LENGTH_SHORT).show()
            Log.d("SOSFragment", "SMS sent to $phoneNumber: $message")
        } catch (e: Exception) {
            Log.e("SOSFragment", "SMS failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Failed to send SMS to $phoneNumber: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val sosButton = view.findViewById<AppCompatImageButton>(R.id.btn_sos)

        sosButton.setOnClickListener {
            sosButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({ sosButton.isEnabled = true }, 5000)

            if (hasLocationPermission() && hasSmsPermission()) {
                sendEmergencyLocation()
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }
}
