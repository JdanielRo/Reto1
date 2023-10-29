package com.txurdinaga.reto1

import Extra
import Plato
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [Carrito.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarritoPagar(
    carritoUsuarioRe: ArrayList<Pedido>,  // Lista de pedidos del carrito del usuario
) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Inicialización de lista pedidos del carrito del usuario
    private var carritoUsuario:ArrayList<Pedido> = carritoUsuarioRe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    // Variable para la vista raíz del fragmento
    private lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño del fragmento
        rootView =  inflater.inflate(R.layout.carrito_pagar, container, false)

        // Obtener referencias a los elementos visuales
        val btnPagar = rootView.findViewById<Button>(R.id.btnPagar)
        val radioGroup = rootView.findViewById<RadioGroup>(R.id.radioGroup)
        val textView4 = rootView.findViewById<TextView>(R.id.textView4)
        val spinner = rootView.findViewById<Spinner>(R.id.spinner)
        val inputTime = rootView.findViewById<EditText>(R.id.editTextTime)

        // Configuración inicial de visibilidad de elementos
        textView4.visibility = View.GONE
        spinner.visibility = View.GONE
        inputTime.visibility = View.GONE

        // Listener para el cambio en la selección del radio button
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton -> {
                    textView4.text = "Escoja la dirección:"
                    inputTime.visibility = View.GONE
                    textView4.visibility = View.VISIBLE
                    spinner.visibility = View.VISIBLE
                    direcciones()  // Llamar a la función para obtener las direcciones del usuario
                }
                R.id.radioButton2 -> {
                    textView4.text = "Hora de entrega:"
                    textView4.visibility = View.VISIBLE
                    spinner.visibility = View.GONE
                    inputTime.visibility = View.VISIBLE
                }
                R.id.radioButton3 -> {
                    textView4.text = "Hora de reserva:"
                    textView4.visibility = View.VISIBLE
                    spinner.visibility = View.GONE
                    inputTime.visibility = View.VISIBLE
                }
            }
        }

        // Acción de botón de pagar
        btnPagar.setOnClickListener {
            val layoutParent = rootView.findViewById<ConstraintLayout>(R.id.parentLayout)
            layoutParent.removeAllViews()

            // Crear TextView dinamicamente
            val newTextView1 = TextView(requireContext())
            newTextView1.text = "\n       Pedido realizado correctamente. \n \n       Su pedido está siendo procesado. \n       " +
                    "Muchas gracias por su compra."
            newTextView1.textSize = 20f
            // Establecer el estilo del texto en negrita
            newTextView1.setTypeface(null, Typeface.BOLD)

            layoutParent.addView(newTextView1)

            //Crear animacion dinamicamente
            val animationView = LottieAnimationView(requireContext())
            animationView.id = View.generateViewId()
            animationView.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT // Ajusta la altura según tus necesidades
            )
            animationView.setAnimation(R.raw.animation_lo8gowce) // Asegúrate de que el archivo de animación esté en la carpeta 'res/raw'
            animationView.playAnimation()
            animationView.loop(true)

            // Configurar restricciones de la vista
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT // Ajusta la altura según tus necesidades
            )
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.topMargin = 124 // Aplicar el margen superior en píxeles
            params.bottomMargin = 139 // Aplicar el margen inferior en píxeles

            // Aplicar restricciones a la vista
            animationView.layoutParams = params

            // Añadir la vista al ConstraintLayout
            layoutParent.addView(animationView)


            //Realizar pago y modificar la base de datos
            val db = FirebaseFirestore.getInstance()
            val pedidosdb = db.collection("Pedido")
            val query = pedidosdb.whereEqualTo("idPedido", 0)

            pedidosdb.get()
                .addOnSuccessListener { result ->
                    var maxId = 0
                    for (document in result) {
                        val idPedido = document.getLong("idPedido")
                        if (idPedido != null && idPedido > maxId) {
                            maxId = idPedido.toInt()
                        }
                    }
                    Log.d("MAXID", "$maxId")
                    maxId++

                    query.get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                val pedidoRef = pedidosdb.document(document.id)
                                // Actualizar el campo 'idPedido' a un nuevo valor
                                pedidoRef.update("idPedido", maxId)
                                    .addOnSuccessListener {
                                        //Toast.makeText(requireContext(), "Pedido realizado correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        //Toast.makeText(requireContext(), "No se ha podido realizar el pedido", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                }
                .addOnFailureListener { exception ->
                    Log.w("MiApp", "Error obteniendo documentos")
                }
        }



        return rootView
    }

    // Función para obtener las direcciones del usuario desde Firebase
    private fun direcciones() {
        val auth = FirebaseAuth.getInstance()

        // Obtener el usuario actualmente autenticado
        val user = auth.currentUser

        // Declarar una variable para almacenar el UID del usuario
        var uid: String? = null

        // Verificar si el usuario está autenticado
        if (user != null) {
            // Obtén el UID del usuario autenticado
            uid = user.uid
        }


        val db = FirebaseFirestore.getInstance()

        var listaDatos = mutableListOf<String>()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaDatos)

        // Obtener la referencia al documento principal del usuario
        val referenciaDocumentoPadre = db.collection("Usuarios").document("$uid")
        // Obtener la colección de direcciones pertenecientes al usuario
        val referenciaColeccionHija = referenciaDocumentoPadre.collection("Direcciones")

        var datoSeleccionado: String = ""
        // Recuperar los documentos de la colección hija
        referenciaColeccionHija.get()
            .addOnSuccessListener { querySnapshot ->
                // Crea una lista para almacenar los datos

                for (document in querySnapshot.documents) {
                    // Para cada documento en la colección hija, obtener los datos y agregarlos a la lista
                    val dato = document.getString("direccion")
                    dato?.let {
                        listaDatos.add(it)
                    }
                }

                // Configurar el adaptador del spinner con los datos obtenidos
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Obtén una referencia al Spinner en tu diseño XML
                val spinner = rootView.findViewById<Spinner>(R.id.spinner)

                // Configura el adaptador en el Spinner
                spinner.adapter = adapter

                // Configurar un listener de selección si se requiere realizar acciones en la selección
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        datoSeleccionado = listaDatos[position]
                        // Realiza acciones basadas en el dato seleccionado

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Manejar el caso en el que no se ha seleccionado nada
                    }
                }
            }
            .addOnFailureListener { e ->
                // Maneja errores si la operación falla
                Log.e("Error", "Error al obtener datos de la colección hija: $e")
            }
    }


    // Objeto companion que sirve como fábrica para crear una nueva instancia de CarritoPagar
    companion object {
        fun newInstance(param1: String, param2: String, carritoUsuarioRe: ArrayList<Pedido>):
                CarritoPagar {
            val fragment = CarritoPagar(carritoUsuarioRe)
            fragment.carritoUsuario = carritoUsuarioRe
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
