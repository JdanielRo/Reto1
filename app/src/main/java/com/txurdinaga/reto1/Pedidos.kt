package com.txurdinaga.reto1

import Extra
import Plato
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import java.net.URLDecoder
import kotlin.math.roundToInt


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Pedidos(
    carritoUsuarioRe: ArrayList<Pedido>,
    listaPlatosRe: ArrayList<Plato>,
    listaExtrasRe: ArrayList<Extra>
) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listaPlatos: ArrayList<Plato> = listaPlatosRe
    private var listaExtras: ArrayList<Extra> = listaExtrasRe
    private var carritoUsuario: ArrayList<Pedido> = carritoUsuarioRe

    private var seccion: String =
        "Entrantes"
    private lateinit var linearLayout: LinearLayout

    private var datosSubidos: Boolean = true

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    private var seleccionCheckBox: ArrayList<Boolean> = ArrayList()

    private lateinit var enviarIdPlatoACarrito: Array<MutableList<String>>
    private lateinit var enviarCantidadPlatoACarrito: Array<MutableList<Int>>

    private var listaPlatosEntrantes: ArrayList<Plato> = ArrayList()
    private var listaPlatosPrincipales: ArrayList<Plato> = ArrayList()
    private var listaPlatosGuarnicion: ArrayList<Plato> = ArrayList()
    private var listaPlatosPostre: ArrayList<Extra> = ArrayList()
    private var listaPlatosBebida: ArrayList<Extra> = ArrayList()

    private var tipo: Tipo = Tipo.MENU

    private var numerodeplatosSeccion: Int = 0

    private lateinit var switch: Switch

    private lateinit var nombreSeccion: TextView

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
        //LinearLayout para instertar todos los elementos
        linearLayout = view.findViewById(R.id.linearLayoutScrollPedidos)

        nombreSeccion = view.findViewById(R.id.txtNombreApartadoPedidos)

        //Hacemos que el array tenga 5 de tamaño ya que hay 5 secciones en el MENU/CARTA
        enviarIdPlatoACarrito = Array(5) { mutableListOf() }
        enviarCantidadPlatoACarrito = Array(5) { mutableListOf() }

        //De las 2 listas de platos y menus que recibimos, hacemos que se dividan en su correspondiente lista por cada seccion
        dividirListas()

        //Switch para seleccionar MENU/CARTA
        switch = view.findViewById<Switch>(R.id.switch2)
        switch.setOnCheckedChangeListener { _, isChecked ->
            tipo = if (isChecked) Tipo.CARTA else Tipo.MENU
            cargarPedidos(inflater, container)
        }

        //Cargas todos los pedidos
        cargarPedidos(inflater, container)

        return view
    }


    private fun dividirListas() {
        for (plato in listaPlatos) {
            when (plato.tipo) {
                "Entrante" -> listaPlatosEntrantes.add(plato)
                "PlatoPrincipal" -> listaPlatosPrincipales.add(plato)
                "Guarnición" -> listaPlatosGuarnicion.add(plato)
            }
        }
        for (extra in listaExtras) {

            when (extra.tipo) {
                "postre" ->
                    listaPlatosPostre.add(extra)

                "bebida" ->
                    listaPlatosBebida.add(extra)

            }
        }
    }

    //Vacia el array bidimensional "enviarIdPlatoACarrito" cada vez que cambia de MENU/CARTA
    private fun vaciarEnvioACarrito() {
        for (i in enviarIdPlatoACarrito.indices) {
            enviarIdPlatoACarrito[i].clear()
            enviarCantidadPlatoACarrito[i].clear()

        }
    }


    private fun cargarPedidos(inflater: LayoutInflater, container: ViewGroup?) {
        linearLayout.removeAllViews()
        numerodeplatosSeccion = 0
        for (i in 0 until seleccionCheckBox.size) {
            seleccionCheckBox[i] = false
        }
        switch.setOnCheckedChangeListener { _, isChecked ->
            for (i in 0 until seleccionCheckBox.size) {
                seleccionCheckBox[i] = false
            }
            tipo = if (isChecked) Tipo.CARTA else Tipo.MENU
            println(tipo)
            vaciarEnvioACarrito()
            cargarPedidos(inflater, container)
        }
        var seccionEnviarCarrito: Int = 0
        nombreSeccion.text = seccion
        when (seccion) {
            //Se añade cada Plato/Extra al layout de cada tipo de seccion
            "Entrantes" -> {
                seccionEnviarCarrito = 0
                seleccionCheckBox.ensureCapacity(listaPlatosEntrantes.size)
                for (plato in listaPlatosEntrantes) {
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato =itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker =
                        itemLayout.findViewById(R.id.numberPicker)
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility= View.GONE
                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE

                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)

                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }
                //Cada vez que queremos cambiar de seccion se comprueba se es correcta el cambio y si es asi muestra la siguiente seccion y si no error
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)
                btnAtras.visibility = View.GONE
                btnSiguiente.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Platos Principales"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }

                }
                linearLayout.addView(itemLayout)
            }

            "Platos Principales" -> {
                seccionEnviarCarrito = 1
                seleccionCheckBox.ensureCapacity(listaPlatosPrincipales.size)
                for (plato in listaPlatosPrincipales) {
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato =itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker =
                        itemLayout.findViewById(R.id.numberPicker)
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility= View.GONE
                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE

                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)

                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }
                //Cada vez que queremos cambiar de seccion se comprueba se es correcta el cambio y si es asi muestra la siguiente seccion y si no error
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)
                btnAtras.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Entrantes"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }

                }
                btnSiguiente.setOnClickListener {

                    if (comprobarContinuarMenu()) {
                        seccion = "Guarniciones"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }
                }
                linearLayout.addView(itemLayout)
            }

            "Guarniciones" -> {
                seccionEnviarCarrito = 2
                seleccionCheckBox.ensureCapacity(listaPlatosGuarnicion.size)
                for (plato in listaPlatosGuarnicion) {
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato =itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker =
                        itemLayout.findViewById(R.id.numberPicker)
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility= View.GONE
                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE

                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)

                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }
                //Cada vez que queremos cambiar de seccion se comprueba se es correcta el cambio y si es asi muestra la siguiente seccion y si no error

                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)
                btnAtras.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Platos Principales"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }

                }
                btnSiguiente.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Postres"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }
                }

                linearLayout.addView(itemLayout)
            }

            "Postres" -> {
                seccionEnviarCarrito = 3
                seleccionCheckBox.ensureCapacity(listaPlatosPostre.size)
                for (plato in listaPlatosPostre) {
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato =itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker =
                        itemLayout.findViewById(R.id.numberPicker)
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility= View.GONE
                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE

                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)

                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }
                //Cada vez que queremos cambiar de seccion se comprueba se es correcta el cambio y si es asi muestra la siguiente seccion y si no error
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)
                btnAtras.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Guarniciones"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }

                }
                btnSiguiente.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Bebidas"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }
                }
                linearLayout.addView(itemLayout)
            }

            "Bebidas" -> {
                seccionEnviarCarrito = 4
                seleccionCheckBox.ensureCapacity(listaPlatosPostre.size)
                for (plato in listaPlatosBebida) {
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato =itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker =
                        itemLayout.findViewById(R.id.numberPicker)
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility= View.GONE
                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE

                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)

                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }
                //Cada vez que queremos cambiar de seccion se comprueba se es correcta el cambio y si es asi muestra la siguiente seccion y si no error
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)
                btnSiguiente.text = getString(R.string.añadir_carrito)
                btnAtras.setOnClickListener {
                    if (comprobarContinuarMenu()) {
                        seccion = "Postres"
                        cargarPedidos(inflater, container)
                    } else {
                        mostrarErrorContinuarMenu()
                    }
                }
                btnSiguiente.setOnClickListener {
                    if (comprobarAlAñadirAlCarrito()) {
                        //                      *****EJEMPLO SNACKBAR*****
                        val rootView = requireActivity().findViewById<View>(android.R.id.content)
                        /*Snackbar.make(
                            rootView,
                            "Este es un mensaje Snackbar",
                            Snackbar.LENGTH_SHORT
                        ).show()*/

                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("¿Deseas añadir  $tipo al carrito?")
                            .setTitle("Mensaje")
                        builder.setPositiveButton(R.string.aceptar) { dialog, id ->
                            enviarPedidoALaLista()
                            if (datosSubidos) {
                                for (list in enviarIdPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                for (list in enviarCantidadPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                seccion = "Entrantes"
                                cargarPedidos(inflater, container)
                            } else {
                                for (list in enviarIdPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                for (list in enviarCantidadPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                for (i in 0 until seleccionCheckBox.size) {
                                    seleccionCheckBox[i] = false
                                }
                                seccion = "Entrantes"
                                cargarPedidos(inflater, container)
                            }
                        }
                        builder.setNegativeButton(R.string.cancelar) { dialog, id ->
                            dialog.cancel()
                        }
                        val dialog = builder.create()
                        dialog.show()

                    } else {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Error")
                        var hayTrue: Boolean = false
                        for (i in 0 until enviarIdPlatoACarrito.size) {
                            if (enviarIdPlatoACarrito[i].size == 2) {
                                hayTrue = true
                                break
                            }
                        }
                        if (hayTrue) {
                            mostrarErrorContinuarMenu()
                        } else {
                            builder.setMessage(getString(R.string.debe_seleccionar))

                            builder.setPositiveButton(getString(R.string.aceptar)) { dialog, which ->
                                dialog.cancel()
                            }
                        }
                        val dialog: AlertDialog = builder.create()
                        dialog.show()

                    }


                }
                linearLayout.addView(itemLayout)
            }
        }
    }

    private fun cargarImagenFirebase(imagen: ImageView, nombre: String) {
        val storageReference = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://reto1-d31c7.appspot.com/${nombre}.jpg")

        storageReference.downloadUrl.addOnSuccessListener { uri ->
            // uri contiene la URL de descarga de la imagen
            val imageUrl = uri.toString()

            // Utiliza una biblioteca como Glide para cargar la imagen en un ImageView
            Glide.with(requireContext())
                .load(imageUrl)
                .into(imagen) // 'imagen' es tu ImageView
        }.addOnFailureListener { exception ->
            // Manejar errores, por ejemplo, si la imagen no se pudo descargar
        }
    }

    private fun mostrarErrorContinuarMenu() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        if (tipo == Tipo.MENU) {

            builder.setMessage(getString(R.string.no_mas))

            builder.setPositiveButton(getString(R.string.aceptar)) { dialog, which ->
                dialog.cancel()
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun comprobarContinuarMenu(): Boolean {
        var continuar: Boolean = true
        if (tipo == Tipo.MENU) {
            var numeroDeTrue: Int = 0
            for (i in 0 until seleccionCheckBox.size) {
                if (seleccionCheckBox[i]) {
                    numeroDeTrue++
                    if (numeroDeTrue == 2) {
                        continuar = false
                        break
                    }

                }
            }

        }

        return continuar
    }

    private fun enviarPedidoALaLista() {
        val idPedido = 0
        val idUsuario: String = auth.currentUser?.uid.toString()
        var idMenu = 0
        var cantidad = 0
        if (tipo == Tipo.MENU) {
            idMenu = comprobarNumeroMenu()
        }
        for (i in 0 until 3) {
            for (j in 0 until enviarIdPlatoACarrito[i].size) {
                val idPlato: String = enviarIdPlatoACarrito[i][j]
                val idExtra: String = ""
                cantidad = enviarCantidadPlatoACarrito[i][j]
                val pedidobd = hashMapOf(
                    "idPedido" to idPedido,
                    "idUsuario" to idUsuario,
                    "idMenu" to idMenu,
                    "idPlato" to idPlato,
                    "idExtra" to idExtra,
                    "cantidad" to cantidad
                )
                db.collection("Pedido")
                    .add(pedidobd)
                    .addOnSuccessListener { documentReference ->
                        // Si el pedido se añade correctamente se añade a la lista
                        var pedido =
                            Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
                        carritoUsuario.add(pedido)
                    }
                    .addOnFailureListener { e ->
                        // Si no, te da error
                        datosSubidos = false
                    }

            }


        }
        for (i in 3 until 5) {
            for (j in 0 until enviarIdPlatoACarrito[i].size) {
                val idPlato: String = ""
                val idExtra: String = enviarIdPlatoACarrito[i][j]
                cantidad = enviarCantidadPlatoACarrito[i][j]
                val pedidobd = hashMapOf(
                    "idPedido" to idPedido,
                    "idUsuario" to idUsuario,
                    "idMenu" to idMenu,
                    "idPlato" to idPlato,
                    "idExtra" to idExtra,
                    "cantidad" to cantidad
                )
                db.collection("Pedido")
                    .add(pedidobd)
                    .addOnSuccessListener { documentReference ->
                        // Si el pedido se añade correctamente se añade a la lista
                        var pedido =
                            Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
                        carritoUsuario.add(pedido)
                    }
                    .addOnFailureListener { e ->
                        // Si no, te da error
                        datosSubidos = false
                    }
            }
        }
    }

    //Comprueba cual es el numero del menu mas alto para añadir el siguiente menu correspondiente
    private fun comprobarNumeroMenu(): Int {
        var numero: Int = 0
        for (pedido in carritoUsuario) {
            if (pedido.idMenu > numero) {
                numero = pedido.idMenu
            }
        }
        return numero + 1
    }

    //Comprobacion si se puede añadir el pedido al carrito devolviendo un booleano
    private fun comprobarAlAñadirAlCarrito(): Boolean {
        var enviar: Boolean = true
        if (tipo == Tipo.MENU) {
            for (i in 0 until enviarIdPlatoACarrito.size) {
                if (enviarIdPlatoACarrito[i].size != 1) {
                    //SI en algun momento del array hay alguna fila con ningún o más de uno elemento error porque no hay en esa fila el tamaño de variable correspondiente que seria 1(el id del Plato/Extra dependiendo de la seccion)
                    enviar = false
                    break
                }
            }
        } else {
            for (i in 0 until enviarIdPlatoACarrito.size) {
                //En este caso se comprueba unicamente si hay algun elemento en el array bidimensional independientemente de la fila
                if (enviarIdPlatoACarrito[i].size == 0) {
                    enviar = false
                } else {
                    enviar = true
                    break
                }
            }
        }


        return enviar
    }

    private fun cargarCheckBox(
        checkBox: CheckBox,
        index: Int,
        seccionEnviarCarrito: Int,
        plato: Extra
    ) {
        if (index >= seleccionCheckBox.size) {
            // Principalmente crea todos y los añade como false ya que al crearse el checkbox esta en false
            seleccionCheckBox.add(false)
        } else {
            //En este caso se hace este when para que cuando cambiemos de secciones que se puedan mantener los checkbox si estan selecciconados o no
            var fila = 0
            when (seccion) {
                "Entrantes" -> {
                    fila = 0
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idExtra) {
                            checkBox.isChecked = true
                            seleccionCheckBox[numerodeplatosSeccion] = true
                            break
                        }
                    }
                }

                "Platos Principales" -> {
                    fila = 1
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idExtra) {
                            checkBox.isChecked = true
                            seleccionCheckBox[numerodeplatosSeccion] = true
                            break
                        }
                    }
                }

                "Guarniciones" -> {
                    fila = 2
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idExtra) {
                            checkBox.isChecked = true
                            seleccionCheckBox[numerodeplatosSeccion] = true
                            break
                        }
                    }
                }

                "Postres" -> {
                    fila = 3
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idExtra) {
                            checkBox.isChecked = true
                            seleccionCheckBox[numerodeplatosSeccion] = true
                            break
                        }
                    }
                }

                "Bebidas" -> {
                    fila = 4
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idExtra) {
                            checkBox.isChecked = true
                            seleccionCheckBox[numerodeplatosSeccion] = true
                            break
                        }
                    }
                }
            }
        }
        if (checkBox.isChecked) {
            //Del array de booleanos ponemos el que selecciona como deberia ser si true/false
            seleccionCheckBox[numerodeplatosSeccion] = true
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            //Del array de booleanos ponemos el que selecciona como deberia ser si true/false
            if (index < seleccionCheckBox.size) {
                seleccionCheckBox[index] = isChecked
            } else {
                seleccionCheckBox.add(isChecked)
            }
            //Se elimina/añade el id del Plato/Extra del array bidimensional
            if (isChecked) {
                enviarIdPlatoACarrito[seccionEnviarCarrito].add(plato.idExtra)
                enviarCantidadPlatoACarrito[seccionEnviarCarrito].add(plato.stock)
            } else {
                enviarIdPlatoACarrito[seccionEnviarCarrito].remove(plato.idExtra)
                enviarCantidadPlatoACarrito[seccionEnviarCarrito].remove(plato.stock)
            }
        }

    }

    private fun cargarCheckBox(
        checkBox: CheckBox,
        index: Int,
        seccionEnviarCarrito: Int,
        plato: Plato
    ) {
        if (index >= seleccionCheckBox.size) {
            // Principalmente crea todos y los añade como false ya que al crearse el checkbox esta en false
            seleccionCheckBox.add(false)
        } else {
            //En este caso se hace este when para que cuando cambiemos de secciones que se puedan mantener los checkbox si estan selecciconados o no

            var fila = 0
            when (seccion) {
                "Entrantes" -> {
                    fila = 0
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idPlato) {
                            checkBox.isChecked = true
                            break
                        }
                    }
                }

                "Platos Principales" -> {
                    fila = 1
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idPlato) {
                            checkBox.isChecked = true
                            break
                        }
                    }
                }

                "Guarniciones" -> {
                    fila = 2
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idPlato) {
                            checkBox.isChecked = true
                            break
                        }
                    }
                }

                "Postres" -> {
                    fila = 3
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idPlato) {
                            checkBox.isChecked = true
                            break
                        }
                    }
                }

                "Bebidas" -> {
                    fila = 4
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        if (enviarIdPlatoACarrito[fila][i] == plato.idPlato) {
                            checkBox.isChecked = true
                            break
                        }
                    }
                }
            }

        }
        if (checkBox.isChecked) {
            //Del array de booleanos ponemos el que selecciona como deberia ser si true/false
            seleccionCheckBox[numerodeplatosSeccion] = true
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            //Del array de booleanos ponemos el que selecciona como deberia ser si true/false
            if (index < seleccionCheckBox.size) {
                seleccionCheckBox[index] = isChecked
            } else {
                seleccionCheckBox.add(isChecked)
            }
            if (isChecked) {
                //Se elimina/añade el id del Plato/Extra del array bidimensional
                enviarIdPlatoACarrito[seccionEnviarCarrito].add(plato.idPlato)
                enviarCantidadPlatoACarrito[seccionEnviarCarrito].add(plato.stock)
            } else {
                enviarIdPlatoACarrito[seccionEnviarCarrito].remove(plato.idPlato)
                enviarCantidadPlatoACarrito[seccionEnviarCarrito].remove(plato.stock)
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(
            param1: String,
            param2: String,
            carritoUsuarioRe: ArrayList<Pedido>,
            listaPlatosRe: ArrayList<Plato>,
            listaExtrasRe: ArrayList<Extra>
        ): Pedidos {
            val fragment = Pedidos(carritoUsuarioRe, listaPlatosRe, listaExtrasRe)
            fragment.listaPlatos = listaPlatosRe
            fragment.listaExtras = listaExtrasRe
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}