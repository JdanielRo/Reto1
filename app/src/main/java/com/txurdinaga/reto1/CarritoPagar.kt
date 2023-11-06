package com.txurdinaga.reto1

import Extra
import Plato
import android.app.AlertDialog
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
    private var carritoUsuario: ArrayList<Pedido> = carritoUsuarioRe

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
        rootView = inflater.inflate(R.layout.carrito_pagar, container, false)

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
            //Verifica si algún botón de opción (RadioButton) dentro de un grupo de botones de opción (RadioGroup) ha sido seleccionado.
            if (radioGroup.checkedRadioButtonId != -1) {
                val layoutParent = rootView.findViewById<ConstraintLayout>(R.id.parentLayout)

                // Eliminar todos los elementos del layout 'layoutParent'
                layoutParent.removeAllViews()

                // Crear un nuevo TextView dinámicamente
                val newTextView1 = TextView(requireContext())
                newTextView1.text =
                    "Pedido realizado correctamente. Su pedido está siendo procesado. Muchas gracias por su compra."
                newTextView1.textSize = 20f
                newTextView1.setTypeface(null, Typeface.BOLD)

                // Agregar el TextView al 'layoutParent'
                layoutParent.addView(newTextView1)

                // Crear una animación Lottie dinámicamente
                val animationView = LottieAnimationView(requireContext())
                animationView.id = View.generateViewId() // Genera un ID único para la vista

                // Configura los parámetros de diseño para la vista de animación
                animationView.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, // Ancho coincide con el parent (layout)
                    ConstraintLayout.LayoutParams.MATCH_PARENT // Altura coincide con el parent (layout)
                )

                // Establece la animación Lottie que se debe reproducir
                animationView.setAnimation(R.raw.animation_lo8gowce)

                // Inicia la reproducción de la animación y configura para que se repita
                animationView.playAnimation()
                animationView.loop(true)

                // Configura las restricciones de la vista de animación dentro del parent layout
                val params = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, // Ancho coincide con el parent
                    ConstraintLayout.LayoutParams.MATCH_PARENT // Altura coincide con el parent
                )
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID // Alinea la parte superior con el parent
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID // Alinea la parte inferior con el parent
                params.topMargin = 124 // Aplica un margen superior de 124 píxeles
                params.bottomMargin = 139 // Aplica un margen inferior de 139 píxeles

                // Aplica las restricciones de diseño a la vista de animación
                animationView.layoutParams = params

                // Agrega la vista de animación al 'layoutParent'
                layoutParent.addView(animationView)


                // Realizar pago y modificar la base de datos
                val db = FirebaseFirestore.getInstance()
                val pedidosdb = db.collection("Pedido")
                val query = pedidosdb.whereEqualTo("idPedido", 0)

                // Obtener los documentos de la colección "Pedido" en Firestore
                pedidosdb.get()
                    .addOnSuccessListener { result ->
                        // Inicializar una variable para rastrear el ID de pedido más alto
                        var maxId = 0

                        // Iterar a través de los documentos en el resultado de la consulta
                        for (document in result) {
                            // Obtener el valor del campo "idPedido" como un número entero (Long)
                            val idPedido = document.getLong("idPedido")

                            // Verificar si el valor de "idPedido" es mayor que el ID de pedido actual (maxId)
                            if (idPedido != null && idPedido > maxId) {
                                // Actualizar maxId con el nuevo valor más alto encontrado
                                maxId = idPedido.toInt()
                            }
                        }

                        // Registrar el ID de pedido más alto encontrado
                        Log.d("MAXID", "$maxId")

                        // Incrementar el ID de pedido máximo en uno para garantizar un nuevo ID único
                        maxId++

                        // Realizar una segunda consulta utilizando 'query' para encontrar documentos con ID de pedido igual a 0
                        query.get()
                            .addOnSuccessListener { result ->
                                // Iterar a través de los documentos encontrados en la segunda consulta
                                for (document in result) {
                                    // Crear una referencia al documento actual
                                    val pedidoRef = pedidosdb.document(document.id)

                                    // Actualizar el campo 'idPedido' con el nuevo valor máximo (maxId)
                                    pedidoRef.update("idPedido", maxId)
                                        .addOnSuccessListener {
                                            // Actualizar el ID de pedido en los elementos del carrito (carritoUsuario)
                                            for (pedido in carritoUsuario) {
                                                pedido.idPedido = maxId
                                            }

                                            // Vaciar el carrito (borrar elementos que se han comprado)
                                            carritoUsuario.clear()

                                            // Aquí podrías mostrar un mensaje de éxito al usuario
                                            // Toast.makeText(requireContext(), "Pedido realizado correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            // Aquí podrías mostrar un mensaje de error al usuario
                                            // Toast.makeText(requireContext(), "No se ha podido realizar el pedido", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("MiApp", "Error obteniendo documentos")
                    }
            } else {
                // Mostrar un diálogo de error si no se ha seleccionado un método de entrega
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Debes seleccionar un método de entrega")
                    .setTitle("ERROR")
                builder.setPositiveButton("Aceptar") { dialog, id -> }
                val dialog = builder.create()
                dialog.show()
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
        // Método estático para crear una nueva instancia de CarritoPagar
        fun newInstance(param1: String, param2: String, carritoUsuarioRe: ArrayList<Pedido>): CarritoPagar {
            // Crear una nueva instancia de CarritoPagar
            val fragment = CarritoPagar(carritoUsuarioRe)

            // Asignar la lista de carritoUsuario proporcionada como argumento a la instancia del fragmento
            fragment.carritoUsuario = carritoUsuarioRe

            // Crear un objeto Bundle para pasar argumentos al fragmento
            val args = Bundle()

            // Agregar los parámetros 'param1' y 'param2' al objeto Bundle
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)

            // Asignar el objeto Bundle con los argumentos a la instancia del fragmento
            fragment.arguments = args

            // Devolver la instancia del fragmento configurada con argumentos y lista de carritoUsuario
            return fragment
        }
    }

}

