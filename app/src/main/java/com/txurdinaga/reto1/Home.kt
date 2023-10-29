package com.txurdinaga.reto1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var googleMap: GoogleMap

    // Este método se llama cuando se crea la vista del fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // Obtiene el fragmento del mapa y registra el callback
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // Registra el callback para la carga asincrónica del mapa

        return root
    }

    // Este método se llama cuando el mapa está listo para ser utilizado
    // En tu función onMapReady
    override fun onMapReady(map: GoogleMap) {
        googleMap = map // Configura el objeto GoogleMap

        // Configura la ubicación y el marcador
        val latitude = 43.25776503411578
        val longitude = -2.902460547792678
        val location = LatLng(latitude, longitude)
        val marker = MarkerOptions().position(location).title("Mi Ubicación") // Crea un marcador

        // Agrega el marcador al mapa
        val googleMapMarker = googleMap.addMarker(marker)

        // Mueve la cámara a la ubicación y establece un nivel de zoom
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        // Configura el listener para el marcador
        googleMap.setOnMarkerClickListener { clickedMarker ->
            if (clickedMarker != null && clickedMarker == googleMapMarker) {
                // Abre Google Maps para dirigirte a la ubicación
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=$latitude,$longitude")
                )
                intent.setPackage("com.google.android.apps.maps") // Asegura que se abra en Google Maps
                startActivity(intent)
                true // Indica que se ha manejado el clic en el marcador
            } else {
                false // Permite que el comportamiento predeterminado del marcador continúe
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}