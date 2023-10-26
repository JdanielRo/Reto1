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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar


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
        linearLayout = view.findViewById(R.id.linearLayoutScrollPedidos)

        nombreSeccion = view.findViewById<TextView>(R.id.txtNombreApartadoPedidos)
        // Asegúrate de inicializar listaPlatos y listaExtras en algún lugar antes de usarlos.

        enviarIdPlatoACarrito = Array(5) { mutableListOf() }


        dividirListas()

        switch = view.findViewById<Switch>(R.id.switch2)
        switch.setOnCheckedChangeListener { _, isChecked ->
            tipo = if (isChecked) Tipo.CARTA else Tipo.MENU
            cargarPedidos(inflater, container)
        }

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

    private fun vaciarEnvioACarrito() {
        for (i in enviarIdPlatoACarrito.indices) {
            enviarIdPlatoACarrito[i].clear()
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
            "Entrantes" -> {
                seccionEnviarCarrito = 0
                seleccionCheckBox.ensureCapacity(listaPlatosEntrantes.size)
                for (plato in listaPlatosEntrantes) {
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
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
                    txtPrecioPlatoPedidos.text = plato.precio.toString()

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
                    val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
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
                    txtPrecioPlatoPedidos.text = plato.precio.toString()

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
                    val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
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
                    txtPrecioPlatoPedidos.text = plato.precio.toString()

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
                    val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
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
                    txtPrecioPlatoPedidos.text = plato.precio.toString()

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
                    val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
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
                    txtPrecioPlatoPedidos.text = plato.precio.toString()

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
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)
                btnSiguiente.text = "Añadir al Carrito"
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
                        Snackbar.make(
                            rootView,
                            "Este es un mensaje Snackbar",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("¿Deseas añadir el $tipo al carrito?")
                            .setTitle("Mensaje")
                        builder.setPositiveButton("Aceptar") { dialog, id ->
                            enviarPedidoALaLista()
                            if (datosSubidos) {
                                for (list in enviarIdPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                seccion = "Entrantes"
                                cargarPedidos(inflater, container)
                            } else {
                                for (list in enviarIdPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                for (i in 0 until seleccionCheckBox.size) {
                                    seleccionCheckBox[i] = false
                                }
                                seccion = "Entrantes"
                                cargarPedidos(inflater, container)
                            }
                        }
                        builder.setNegativeButton("Cancelar") { dialog, id ->
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
                            builder.setMessage("Se debe seleccionar un plato en cada seccion del menu")

                            builder.setPositiveButton("Aceptar") { dialog, which ->
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

    private fun mostrarErrorContinuarMenu() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        if (tipo == Tipo.MENU) {

            builder.setMessage("No se pueden seleccionar mas de un plato")

            builder.setPositiveButton("Aceptar") { dialog, which ->
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
            cantidad = 1
        }
        for (i in 0 until 3) {
            for (j in 0 until enviarIdPlatoACarrito[i].size) {
                val idPlato: String = enviarIdPlatoACarrito[i][j]
                val idExtra: String = ""
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
                        var pedido =
                            Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
                        carritoUsuario.add(pedido)
                    }
                    .addOnFailureListener { e ->
                        datosSubidos = false
                    }

            }


        }
        for (i in 3 until 5) {
            for (j in 0 until enviarIdPlatoACarrito[i].size) {
                val idPlato: String = ""
                val idExtra: String = enviarIdPlatoACarrito[i][j]
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
                        var pedido =
                            Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
                        carritoUsuario.add(pedido)
                    }
                    .addOnFailureListener { e ->
                        datosSubidos = false
                    }
            }
        }
    }


    private fun comprobarNumeroMenu(): Int {
        var numero: Int = 0
        for (pedido in carritoUsuario) {
            if (pedido.idMenu > numero) {
                numero = pedido.idMenu
            }
        }
        return numero + 1
    }

    private fun comprobarAlAñadirAlCarrito(): Boolean {
        var enviar: Boolean = true
        if (tipo == Tipo.MENU) {
            for (i in 0 until enviarIdPlatoACarrito.size) {
                if (enviarIdPlatoACarrito[i].size != 1) {
                    enviar = false
                    break
                }
            }
        } else {
            for (i in 0 until enviarIdPlatoACarrito.size) {
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
            seleccionCheckBox.add(false)
        } else {
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
            seleccionCheckBox[numerodeplatosSeccion] = true
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (index < seleccionCheckBox.size) {
                seleccionCheckBox[index] = isChecked
            } else {
                seleccionCheckBox.add(isChecked)
            }
            if (isChecked) {
                enviarIdPlatoACarrito[seccionEnviarCarrito].add(plato.idExtra)
            } else {
                enviarIdPlatoACarrito[seccionEnviarCarrito].remove(plato.idExtra)
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
            seleccionCheckBox.add(false)
        } else {
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
            seleccionCheckBox[numerodeplatosSeccion] = true
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (index < seleccionCheckBox.size) {
                seleccionCheckBox[index] = isChecked
            } else {
                seleccionCheckBox.add(isChecked)
            }
            if (isChecked) {
                enviarIdPlatoACarrito[seccionEnviarCarrito].add(plato.idPlato)
            } else {
                enviarIdPlatoACarrito[seccionEnviarCarrito].remove(plato.idPlato)
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