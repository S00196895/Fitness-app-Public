package bart.saj.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapRun.newInstance] factory method to
 * create an instance of this fragment.
 */

class MapRun : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var startRunButton: Button
    private lateinit var pauseRunButton: Button
    private lateinit var timerTextView: TextView
    private lateinit var chronometer: Chronometer
    private var isRunning = false
    private var timeWhenStopped: Long = 0
    private var startTime: Long = 0L
    private var timeInMilliseconds: Long = 0L
    private var updatedTime: Long = 0L
    private var timeSwapBuff: Long = 0L
    private var secs: Int = 0
    private var mins: Int = 0
    private lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map_run, container, false)


        return rootView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        startRunButton = view.findViewById(R.id.startRunButton)
        pauseRunButton = view.findViewById(R.id.pauseRunButton)
        timerTextView = view.findViewById(R.id.timerTextView)


        startRunButton.setOnClickListener {
            startRun()
        }
        pauseRunButton.setOnClickListener {
            pauseRun()
        }

        requestLocationPermission()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction().replace(R.id.map_container, mapFragment).commit()

        mapFragment.getMapAsync(this)
        createLocationCallback()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                }
            } else {

            }
        }
    }
    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
    private fun startTimer() {
        startTime = SystemClock.uptimeMillis()
        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime
                updatedTime = timeSwapBuff + timeInMilliseconds
                secs = (updatedTime / 1000).toInt()
                mins = secs / 60
                secs %= 60
                timerTextView.text = String.format("%02d:%02d", mins, secs)
                handler.postDelayed(this, 0)
            }
        })
    }

    private fun createLocationCallback() {
        val pathPoints = mutableListOf<LatLng>()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    pathPoints.add(latLng)
                    mMap.addPolyline(PolylineOptions().addAll(pathPoints))
                    updateCameraToBounds(pathPoints)
                }
            }
        }
    }
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startRun() {
        isRunning = true
        startLocationUpdates()
        if (timeSwapBuff == 0L) {
            startTime = SystemClock.uptimeMillis()
        } else {
            startTime = SystemClock.uptimeMillis() - timeSwapBuff
        }
        startTimer()
    }

    private fun pauseRun() {
        isRunning = false
        stopLocationUpdates()
        timeSwapBuff += timeInMilliseconds
        handler.removeCallbacksAndMessages(null)
    }




    override fun onStart() {
        super.onStart()
        if (isRunning) {
            startLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRunning) {
            stopLocationUpdates()
        }
    }

    private fun updateCameraToBounds(pathPoints: List<LatLng>) {
        if (pathPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            for (point in pathPoints) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            val center = bounds.center
            val zoomLevel = 16f

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel))
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapRun.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapRun().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}