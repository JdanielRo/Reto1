package com.txurdinaga.reto1

import android.content.ContentValues.TAG
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference // Importa la clase DocumentReference
import android.util.Log // Importa Log para el manejo de errores
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Pedidos : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseFirestore.getInstance()

    private lateinit var linearLayout: LinearLayout

    private val listaPlatos: MutableList<Plato> = mutableListOf() // Lista para almacenar los platos

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
        val view = inflater.inflate(R.layout.fragment_pedidos, container, false)
        linearLayout = view.findViewById(R.id.linearLayoutScrollPedidos)


        // Limpia el linearLayout antes de agregar nuevos elementos
        linearLayout.removeAllViews()

        val itemLayout = inflater.inflate(R.layout.layout_pedidos_menu_superior, container, false)
        linearLayout.addView(itemLayout)

        // Llamar a la función para obtener y mostrar los platos
        obtenerPlatos(inflater, container)

        return view
    }

    private fun obtenerPlatos(inflater: LayoutInflater, container: ViewGroup?) {
        db.collection("Platos")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    // Obtener el nombre del plato desde el documento
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val idMenuReference = document.get("id_menu") as DocumentReference? // Obtener la referencia como DocumentReference
                    val celiaco = document.getBoolean("celiaco") ?: false
                    val calorias = document.getLong("calorias")?.toInt() ?: 0
                    val precio = document.getDouble("precio") ?: 0.0
                    val cantidad = document.getLong("cantidad")?.toInt() ?: 0

                    // Obtener el id del menú si la referencia no es nula
                    var id_menu: String = ""
                    if (idMenuReference != null) {
                        id_menu = idMenuReference.id
                    }

                    val plato = Plato(nombre, descripcion, id_menu, celiaco, calorias, precio, cantidad)

                    listaPlatos.add(plato)
                }
                mostrarPlatos(inflater, container)
            }
            .addOnFailureListener { e ->
                // Maneja errores aquí, por ejemplo, imprime el mensaje de error
                Log.e(TAG, "Error al obtener platos: ${e.message}", e)
            }
    }

    private fun mostrarPlatos(inflater: LayoutInflater, container: ViewGroup?) {
        // Genera una lista de números del 1 al 50 como cadenas de texto

        for (plato in listaPlatos) {
            val itemLayout = inflater.inflate(R.layout.layout_pedidos_platos, container, false)
            val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlatos)
            val txtCaloriaPlato = itemLayout.findViewById<TextView>(R.id.txtCaloriaPlato)
            val txtPrecioPlato = itemLayout.findViewById<TextView>(R.id.txtPrecioPlatos)
            val spinner = itemLayout.findViewById<Spinner>(R.id.spinnerNumbers)

            txtNombrePlato.text = plato.nombre
            txtCaloriaPlato.text = plato.calorias.toString()
            txtPrecioPlato.text = "${plato.precio}€"

            // Configura el Adapter para el Spinner
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (1..plato.cantidad).map { it.toString() })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Agrega un listener para el Spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedNumber = parent?.getItemAtPosition(position).toString()
                    // Haz algo con el número seleccionado, por ejemplo, imprimirlo
                    Log.d(TAG, "Número seleccionado: $selectedNumber")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Maneja la situación en la que no se ha seleccionado nada
                }
            }

            linearLayout.addView(itemLayout)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Pedidos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
