package com.example.rescuelink

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private lateinit var googleMap: GoogleMap
class RescueMapFragment : Fragment(), OnMapReadyCallback {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rescue_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
    mapFragment.getMapAsync(this)

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location->
                if(location != null) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,14f))

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(myLatLng)
                            .title("You are here!")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        val alerts = listOf(
            Triple("Flood in Assam", 26.2006, 92.9376),
            Triple("Fire in Mumbai", 19.0760, 72.8777),
            Triple("Earthquake in Delhi", 28.6139, 77.2090),
            Triple("Flood in Shoreline Park", 37.432785, -122.091743),
            Triple("Fire in Google bay view", 37.4235, -122.0666)
        )

        for (alert in alerts) {
            val alertLatLng = LatLng(alert.second, alert.third)
            val iconRes = when {
                alert.first.contains("Fire", ignoreCase = true) -> R.drawable.ic_fire
                alert.first.contains("Flood", ignoreCase = true) -> R.drawable.ic_flood
                alert.first.contains("Earthquake", ignoreCase = true) -> R.drawable.ic_earthquake
                else -> R.drawable.ic_map2
            }
            val resizedIcon = getResizedBitmapDescriptor(iconRes, 160, 160)
            googleMap.addMarker(
                MarkerOptions()
                    .position(alertLatLng)
                    .title(alert.first)
                    .icon(resizedIcon)
            )
        }

        googleMap.setOnMarkerClickListener { marker ->
            Toast.makeText(requireContext(), "Alert: ${marker.title}", Toast.LENGTH_SHORT).show()
            false
        }


    }
    private fun getResizedBitmapDescriptor(@DrawableRes resId: Int, width: Int, height: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(requireContext(), resId)!!
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


}