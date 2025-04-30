package com.example.rescuelink

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SOSFragment : Fragment(R.layout.fragment_s_o_s) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    companion object {
        private const val PREF_NAME = "contacts_pref"
        private const val KEY_CONTACTS = "contact_numbers"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Register permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false

            when {
                locationGranted && smsGranted -> sendEmergencyLocation()
                !locationGranted -> Toast.makeText(
                    requireContext(),
                    "Location permission denied.",
                    Toast.LENGTH_SHORT
                ).show()
                !smsGranted -> Toast.makeText(
                    requireContext(),
                    "SMS permission denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val sosButton = view.findViewById<ImageButton>(R.id.btn_sos)
        sosButton.setOnClickListener {
            checkPermissionsAndSendSOS()
        }
    }

    private fun checkPermissionsAndSendSOS() {
        val locationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val smsPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.SEND_SMS
        )

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
            Log.d("SOSFragment", "Attempting to fetch location")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Log.d("SOSFragment", "Location fetch success: $location")
                    val message = if (location != null) {
                        "🚨 EMERGENCY ALERT 🚨\nPlease help me. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    } else {
                        Log.w("SOSFragment", "Location is null")
                        "🚨 EMERGENCY ALERT 🚨\nI need help but couldn't fetch my location."
                    }
                    sendSmsToAllContacts(message)
                }
                .addOnFailureListener { exception ->
                    Log.e("SOSFragment", "Location fetch failed: ${exception.message}", exception)
                    sendSmsToAllContacts(
                        "🚨 EMERGENCY ALERT 🚨\nFailed to get location. Please help!"
                    )
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch location: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: SecurityException) {
            Log.e("SOSFragment", "Permission error: ${e.message}", e)
            Toast.makeText(requireContext(), "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSmsToAllContacts(message: String) {
        try {
            // Retrieve contacts from SharedPreferences
            val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val contactNumbers = prefs.getStringSet(KEY_CONTACTS, emptySet()) ?: emptySet()

            if (contactNumbers.isEmpty()) {
                Log.w("SOSFragment", "No contacts found in SharedPreferences")
                Toast.makeText(requireContext(), "No contacts available to send SOS", Toast.LENGTH_LONG).show()
                return
            }

            val smsManager = SmsManager.getDefault()
            var successCount = 0
            var failureCount = 0

            for (phoneNumber in contactNumbers) {
                try {
                    Log.d("SOSFragment", "Attempting to send SMS to $phoneNumber: $message")
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                    successCount++
                    Log.d("SOSFragment", "SMS sent successfully to $phoneNumber")
                } catch (e: Exception) {
                    failureCount++
                    Log.e("SOSFragment", "Failed to send SMS to $phoneNumber: ${e.message}", e)
                }
            }

            val resultMessage = when {
                successCount > 0 && failureCount == 0 -> "🚨 SOS Sent to $successCount contact(s)!"
                successCount > 0 && failureCount > 0 -> "🚨 SOS Sent to $successCount contact(s), failed for $failureCount"
                else -> "Failed to send SOS to all contacts"
            }
            Toast.makeText(requireContext(), resultMessage, Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e("SOSFragment", "Error sending SMS to contacts: ${e.message}", e)
            Toast.makeText(requireContext(), "Failed to send SOS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}