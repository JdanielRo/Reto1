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

    private var seccion: String = "Entrantes"

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pedidos, container, false)
        //LinearLayout para instertar todos los elementos
        linearLayout = view.findViewById(R.id.linearLayoutScrollPedidos)

        nombreSeccion = view.findViewById(R.id.txtNombreApartadoPedidos)

        //Hacemos que el array tenga 5 de tamaño ya que hay 5 secciones en el MENU/CARTA
        enviarIdPlatoACarrito = Array(5) { mutableListOf() }

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
        // Itera sobre la lista de platos y los organiza en sublistas según su tipo
        for (plato in listaPlatos) {
            when (plato.tipo) {
                "Entrante" -> listaPlatosEntrantes.add(plato) // Si el tipo es "Entrante", añádelo a la lista de entrantes
                "PlatoPrincipal" -> listaPlatosPrincipales.add(plato) // Si el tipo es "PlatoPrincipal", añádelo a la lista de platos principales
                "Guarnición" -> listaPlatosGuarnicion.add(plato) // Si el tipo es "Guarnición", añádelo a la lista de guarniciones
            }
        }

        // Itera sobre la lista de extras y los organiza en sublistas según su tipo
        for (extra in listaExtras) {
            when (extra.tipo) {
                "postre" -> listaPlatosPostre.add(extra) // Si el tipo es "postre", añádelo a la lista de postres
                "bebida" -> listaPlatosBebida.add(extra) // Si el tipo es "bebida", añádelo a la lista de bebidas
            }
        }
    }


    //Vacia el array bidimensional "enviarIdPlatoACarrito" cada vez que cambia de MENU/CARTA
    private fun vaciarEnvioACarrito() {
        for (i in enviarIdPlatoACarrito.indices) {
            enviarIdPlatoACarrito[i].clear()

        }
    }


    private fun cargarPedidos(inflater: LayoutInflater, container: ViewGroup?) {
        // Limpia todos los elementos de un LinearLayout
        linearLayout.removeAllViews()

        // Reinicia la variable numerodeplatosSeccion a cero
        numerodeplatosSeccion = 0

        // Reinicia el estado de los elementos en la lista seleccionCheckBox
        for (i in 0 until seleccionCheckBox.size) {
            seleccionCheckBox[i] = false
        }

        // Configura un listener para el cambio de estado del Switch
        switch.setOnCheckedChangeListener { _, isChecked ->
            // Reinicia el estado de los elementos en la lista seleccionCheckBox cuando el Switch cambia
            for (i in 0 until seleccionCheckBox.size) {
                seleccionCheckBox[i] = false
            }

            // Establece la variable 'tipo' basada en el estado del Switch (Carta o Menú)
            tipo = if (isChecked) Tipo.CARTA else Tipo.MENU

            // Limpia el contenido de algún envío anterior al carrito
            vaciarEnvioACarrito()

            // Carga los pedidos nuevamente, utilizando el inflater y el contenedor dados
            cargarPedidos(inflater, container)
        }

        // Inicializa una variable 'seccionEnviarCarrito' con un valor desconocido (debe definirse previamente)
        var seccionEnviarCarrito: Int = 0

        // Asigna el valor de 'seccion' al texto del elemento 'nombreSeccion'
        nombreSeccion.text = seccion
        when (seccion) {
            //Se añade cada Plato/Extra al layout de cada tipo de seccion
            // Cuando la sección actual es "Entrantes"
            "Entrantes" -> {
                // Configuramos la sección para enviar al carrito (valor 0 para "Entrantes")
                seccionEnviarCarrito = 0

                // Aseguramos que la lista 'seleccionCheckBox' tenga suficiente capacidad
                seleccionCheckBox.ensureCapacity(listaPlatosEntrantes.size)

                // Recorremos la lista de platos en la sección "Entrantes"
                for (plato in listaPlatosEntrantes) {
                    // Inflamos un diseño de elemento de plato desde un archivo XML
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)

                    // Obtenemos referencias a elementos dentro del diseño
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker = itemLayout.findViewById(R.id.numberPicker)

                    // Configuramos el rango del NumberPicker basado en el stock del plato
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility = View.GONE

                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    // Configuramos los elementos con los datos del plato actual
                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    // Agregamos un listener para el clic en la descripción del plato
                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    // Agregamos un listener para el botón de cierre de descripción
                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Configuramos la visibilidad de elementos basados en el tipo (CARTA o MENÚ)
                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE
                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Cargamos el CheckBox y agregamos el diseño del plato al LinearLayout
                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)
                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }

                // Agregamos un diseño para los botones "Atrás" y "Siguiente"
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)

                // Configuramos la visibilidad del botón "Atrás" (en este caso, está oculto)
                btnAtras.visibility = View.GONE

                // Agregamos un listener para el botón "Siguiente"
                btnSiguiente.setOnClickListener {
                    // Comprobamos si es posible avanzar a la siguiente sección del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Platos Principales" y recargamos los pedidos
                        seccion = "Platos Principales"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos el diseño al LinearLayout
                linearLayout.addView(itemLayout)
            }


            // Cuando la sección actual es "Platos Principales"
            "Platos Principales" -> {
                // Configuramos la sección para enviar al carrito (valor 1 para "Platos Principales")
                seccionEnviarCarrito = 1

                // Aseguramos que la lista 'seleccionCheckBox' tenga suficiente capacidad
                seleccionCheckBox.ensureCapacity(listaPlatosPrincipales.size)

                // Recorremos la lista de platos en la sección "Platos Principales"
                for (plato in listaPlatosPrincipales) {
                    // Inflamos un diseño de elemento de plato desde un archivo XML
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)

                    // Obtenemos referencias a elementos dentro del diseño
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker = itemLayout.findViewById(R.id.numberPicker)

                    // Configuramos el rango del NumberPicker basado en el stock del plato
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility = View.GONE

                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    // Configuramos los elementos con los datos del plato actual
                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    // Agregamos un listener para el clic en la descripción del plato
                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    // Agregamos un listener para el botón de cierre de descripción
                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Configuramos la visibilidad de elementos basados en el tipo (CARTA o MENÚ)
                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE
                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Cargamos el CheckBox y agregamos el diseño del plato al LinearLayout
                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)
                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }

                // Agregamos un diseño para los botones "Atrás" y "Siguiente"
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)

                // Agregamos un listener para el botón "Atrás"
                btnAtras.setOnClickListener {
                    // Comprobamos si es posible retroceder a la sección anterior del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Entrantes" y recargamos los pedidos
                        seccion = "Entrantes"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos un listener para el botón "Siguiente"
                btnSiguiente.setOnClickListener {
                    // Comprobamos si es posible avanzar a la siguiente sección del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Guarniciones" y recargamos los pedidos
                        seccion = "Guarniciones"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos el diseño al LinearLayout
                linearLayout.addView(itemLayout)
            }


            // Cuando la sección actual es "Guarniciones"
            "Guarniciones" -> {
                // Configuramos la sección para enviar al carrito (valor 2 para "Guarniciones")
                seccionEnviarCarrito = 2

                // Aseguramos que la lista 'seleccionCheckBox' tenga suficiente capacidad
                seleccionCheckBox.ensureCapacity(listaPlatosGuarnicion.size)

                // Recorremos la lista de platos en la sección "Guarniciones"
                for (plato in listaPlatosGuarnicion) {
                    // Inflamos un diseño de elemento de plato desde un archivo XML
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)

                    // Obtenemos referencias a elementos dentro del diseño
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker = itemLayout.findViewById(R.id.numberPicker)

                    // Configuramos el rango del NumberPicker basado en el stock del plato
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility = View.GONE

                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    // Configuramos los elementos con los datos del plato actual
                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    // Agregamos un listener para el clic en la descripción del plato
                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    // Agregamos un listener para el botón de cierre de descripción
                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Configuramos la visibilidad de elementos basados en el tipo (CARTA o MENÚ)
                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE
                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Cargamos el CheckBox y agregamos el diseño del plato al LinearLayout
                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)
                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }

                // Agregamos un diseño para los botones "Atrás" y "Siguiente"
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)

                // Agregamos un listener para el botón "Atrás"
                btnAtras.setOnClickListener {
                    // Comprobamos si es posible retroceder a la sección anterior del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Platos Principales" y recargamos los pedidos
                        seccion = "Platos Principales"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos un listener para el botón "Siguiente"
                btnSiguiente.setOnClickListener {
                    // Comprobamos si es posible avanzar a la siguiente sección del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Postres" y recargamos los pedidos
                        seccion = "Postres"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos el diseño al LinearLayout
                linearLayout.addView(itemLayout)
            }


            // Cuando la sección actual es "Guarniciones"
            "Postres" -> {
                // Configuramos la sección para enviar al carrito (valor 2 para "Guarniciones")
                seccionEnviarCarrito = 3

                // Aseguramos que la lista 'seleccionCheckBox' tenga suficiente capacidad
                seleccionCheckBox.ensureCapacity(listaPlatosGuarnicion.size)

                // Recorremos la lista de platos en la sección "Guarniciones"
                for (plato in listaPlatosPostre) {
                    // Inflamos un diseño de elemento de plato desde un archivo XML
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)

                    // Obtenemos referencias a elementos dentro del diseño
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker = itemLayout.findViewById(R.id.numberPicker)

                    // Configuramos el rango del NumberPicker basado en el stock del plato
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility = View.GONE

                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    // Configuramos los elementos con los datos del plato actual
                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    // Agregamos un listener para el clic en la descripción del plato
                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    // Agregamos un listener para el botón de cierre de descripción
                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Configuramos la visibilidad de elementos basados en el tipo (CARTA o MENÚ)
                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE
                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Cargamos el CheckBox y agregamos el diseño del plato al LinearLayout
                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)
                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }

                // Agregamos un diseño para los botones "Atrás" y "Siguiente"
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)

                // Agregamos un listener para el botón "Atrás"
                btnAtras.setOnClickListener {
                    // Comprobamos si es posible retroceder a la sección anterior del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Platos Principales" y recargamos los pedidos
                        seccion = "Guarniciones"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos un listener para el botón "Siguiente"
                btnSiguiente.setOnClickListener {
                    // Comprobamos si es posible avanzar a la siguiente sección del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Postres" y recargamos los pedidos
                        seccion = "Bebidas"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos el diseño al LinearLayout
                linearLayout.addView(itemLayout)
            }

            // Cuando la sección actual es "Bebidas"
            "Bebidas" -> {
                // Configuramos la sección para enviar al carrito (valor 4 para "Bebidas")
                seccionEnviarCarrito = 4

                // Aseguramos que la lista 'seleccionCheckBox' tenga suficiente capacidad
                seleccionCheckBox.ensureCapacity(listaPlatosBebida.size)

                // Recorremos la lista de platos en la sección "Bebidas"
                for (plato in listaPlatosBebida) {
                    // Inflamos un diseño de elemento de plato desde un archivo XML
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)

                    // Obtenemos referencias a elementos dentro del diseño
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                    val numberPicker: NumberPicker = itemLayout.findViewById(R.id.numberPicker)

                    // Configuramos el rango del NumberPicker basado en el stock del plato
                    numberPicker.minValue = 1
                    numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                    numberPicker.visibility = View.GONE

                    val txtPrecioPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad =
                        itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato =
                        itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                    val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                    // Configuramos los elementos con los datos del plato actual
                    txtNombrePlato.text = plato.nombre
                    txtDescripcionPlatoPedidos.text = plato.descripcion
                    txtPrecioPlatoPedidos.text = "${plato.precio}€"
                    cargarImagenFirebase(imgPlato, plato.nombre)

                    // Agregamos un listener para el clic en la descripción del plato
                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        }
                    }

                    // Agregamos un listener para el botón de cierre de descripción
                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Configuramos la visibilidad de elementos basados en el tipo (CARTA o MENÚ)
                    if (tipo == Tipo.CARTA) {
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        imgCerrarDescripcion.visibility = View.GONE
                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    // Cargamos el CheckBox y agregamos el diseño del plato al LinearLayout
                    cargarCheckBox(checkBox, numerodeplatosSeccion, seccionEnviarCarrito, plato)
                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }

                // Agregamos un diseño para los botones "Atrás" y "Siguiente"
                val itemLayout =
                    inflater.inflate(R.layout.layout_pedidos_siguiente_atras, container, false)
                val btnAtras = itemLayout.findViewById<Button>(R.id.btnAtras)
                val btnSiguiente = itemLayout.findViewById<Button>(R.id.btnSiguiente)

                // Cambiamos el texto del botón "Siguiente" a "Añadir al carrito"
                btnSiguiente.text = getString(R.string.añadir_carrito)

                // Agregamos un listener para el botón "Atrás"
                btnAtras.setOnClickListener {
                    // Comprobamos si es posible retroceder a la sección anterior del menú
                    if (comprobarContinuarMenu()) {
                        // Cambiamos la sección actual a "Postres" y recargamos los pedidos
                        seccion = "Postres"
                        cargarPedidos(inflater, container)
                    } else {
                        // Mostramos un mensaje de error si no es posible continuar
                        mostrarErrorContinuarMenu()
                    }
                }

                // Agregamos un listener para el botón "Siguiente" (Añadir al carrito)
                btnSiguiente.setOnClickListener {
                    if (comprobarAlAñadirAlCarrito()) {
                        // Verifica si se cumplen las condiciones para agregar al carrito
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("¿Deseas añadir $tipo al carrito?").setTitle("Mensaje")

                        builder.setPositiveButton(R.string.aceptar) { dialog, id ->
                            // Cuando se presiona el botón "Aceptar" en el diálogo de confirmación
                            enviarPedidoALaLista()
                            if (datosSubidos) {
                                // Si los datos se han subido con éxito al servidor
                                for (list in enviarIdPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                seccion = "Entrantes"  // Cambia la sección de la vista a "Entrantes"
                                cargarPedidos(inflater, container)  // Recarga la vista de pedidos
                            } else {
                                // Si no se pudieron subir los datos al servidor
                                for (list in enviarIdPlatoACarrito) {
                                    list.removeAll { true }
                                }
                                for (i in 0 until seleccionCheckBox.size) {
                                    seleccionCheckBox[i] = false
                                }
                                seccion = "Entrantes"  // Cambia la sección de la vista a "Entrantes"
                                cargarPedidos(inflater, container)  // Recarga la vista de pedidos
                            }
                        }

                        builder.setNegativeButton(R.string.cancelar) { dialog, id ->
                            dialog.cancel()  // Cierra el cuadro de diálogo al presionar "Cancelar"
                        }
                        val dialog = builder.create()
                        dialog.show()  // Muestra el cuadro de diálogo de confirmación

                    }else {
                        // Si no se puede agregar al carrito, realizar comprobaciones adicionales
                        var hayTrue: Boolean = false
                        // Recorrer la lista de 'enviarIdPlatoACarrito' para verificar selecciones
                        for (i in 0 until enviarIdPlatoACarrito.size) {
                            if (enviarIdPlatoACarrito[i].size == 2) {
                                // Encontrar al menos una selección (se guardan como pares de valores)
                                hayTrue = true
                                break
                            }
                        }
                        val builder = AlertDialog.Builder(context)
                        if (hayTrue) {
                            mostrarErrorContinuarMenu() // Muestra un mensaje de error porque no se pueden agregar elementos al carrito
                        } else {
                            builder.setTitle("Error")
                            builder.setMessage(getString(R.string.debe_seleccionar))  // Muestra un mensaje indicando que se debe seleccionar al menos un elemento

                            builder.setPositiveButton(getString(R.string.aceptar)) { dialog, which ->
                                dialog.cancel()  // Cierra el cuadro de diálogo de error al presionar "Aceptar"
                            }
                        }
                        val dialog: AlertDialog = builder.create()
                        dialog.show()  // Muestra el cuadro de diálogo de error
                    }
                }

                // Agregamos el diseño al LinearLayout
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
            Glide.with(requireContext()).load(imageUrl).into(imagen) // 'imagen' es tu ImageView
        }.addOnFailureListener { exception ->
            // Manejar errores, por ejemplo, si la imagen no se pudo descargar
        }
    }

    private fun mostrarErrorContinuarMenu() {
        // Crea un constructor de cuadro de diálogo
        val builder = AlertDialog.Builder(context)

        // Establece el título del cuadro de diálogo como "Error"
        builder.setTitle("Error")

        // Verifica si el tipo es un menú
        if (tipo == Tipo.MENU) {
            // Si es un menú, establece el mensaje del cuadro de diálogo como "No se pueden agregar más elementos al menú"
            builder.setMessage(getString(R.string.no_mas))

            // Configura un botón en el cuadro de diálogo llamado "Aceptar" que cierra el cuadro de diálogo al hacer clic
            builder.setPositiveButton(getString(R.string.aceptar)) { dialog, which ->
                dialog.cancel()  // Cierra el cuadro de diálogo al presionar "Aceptar"
            }
        }

        // Crea el cuadro de diálogo
        val dialog: AlertDialog = builder.create()

        // Muestra el cuadro de diálogo de error
        dialog.show()
    }


    private fun comprobarContinuarMenu(): Boolean {
        // Inicializa la variable 'continuar' como verdadera
        var continuar: Boolean = true

        // Verifica si el tipo es un menú
        if (tipo == Tipo.MENU) {
            // Inicializa la variable 'numeroDeTrue' para contar cuántos elementos están marcados como verdaderos
            var numeroDeTrue: Int = 0

            // Itera a través del arreglo 'seleccionCheckBox' para verificar si los elementos están marcados como verdaderos
            for (i in 0 until seleccionCheckBox.size) {
                if (seleccionCheckBox[i]) {
                    numeroDeTrue++

                    // Si se encuentran dos elementos marcados como verdaderos, se establece 'continuar' como falso y se sale del ciclo
                    if (numeroDeTrue == 2) {
                        continuar = false
                        break
                    }
                }
            }
        }

        // Devuelve el valor de 'continuar' (verdadero si se puede continuar, falso si no)
        return continuar
    }

    private fun enviarPedidoALaLista() {
        val idPedido = 0 // ID del pedido (puedes personalizarlo)
        val idUsuario: String = auth.currentUser?.uid.toString() // ID del usuario actual
        var idMenu = 0 // ID del menú (si el tipo es MENÚ)
        var cantidad = 0 // Cantidad inicial de los platos

        // Si el tipo de menú es MENÚ, se obtiene el ID del menú llamando a la función comprobarNumeroMenu()
        if (tipo == Tipo.MENU) {
            idMenu = comprobarNumeroMenu()
        }

        // Se itera a través de las primeras tres categorías (0, 1, 2)
        for (i in 0 until 3) {
            for (j in 0 until enviarIdPlatoACarrito[i].size) {
                val idPlato: String = enviarIdPlatoACarrito[i][j]
                val idExtra: String = ""

                // Se crea un objeto de pedido con los datos
                val pedidobd = hashMapOf(
                    "idPedido" to idPedido,
                    "idUsuario" to idUsuario,
                    "idMenu" to idMenu,
                    "idPlato" to idPlato,
                    "idExtra" to idExtra,
                    "cantidad" to cantidad
                )

                // Se agrega el pedido a la colección "Pedido" en la base de datos
                db.collection("Pedido").add(pedidobd).addOnSuccessListener { documentReference ->
                    // Si el pedido se añade correctamente a la base de datos, se añade a la lista local 'carritoUsuario'
                    var pedido = Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
                    carritoUsuario.add(pedido)
                }.addOnFailureListener { e ->
                    // Si no se puede agregar el pedido, se marca 'datosSubidos' como falso
                    datosSubidos = false
                }
            }
        }

        // Se itera a través de las categorías 3 y 4 (correspondientes a "Postres" y "Bebidas")
        for (i in 3 until 5) {
            for (j in 0 until enviarIdPlatoACarrito[i].size) {
                val idPlato: String = ""
                val idExtra: String = enviarIdPlatoACarrito[i][j]

                // Se crea un objeto de pedido con los datos
                val pedidobd = hashMapOf(
                    "idPedido" to idPedido,
                    "idUsuario" to idUsuario,
                    "idMenu" to idMenu,
                    "idPlato" to idPlato,
                    "idExtra" to idExtra,
                    "cantidad" to cantidad
                )

                // Se agrega el pedido a la colección "Pedido" en la base de datos
                db.collection("Pedido").add(pedidobd).addOnSuccessListener { documentReference ->
                    // Si el pedido se añade correctamente a la base de datos, se añade a la lista local 'carritoUsuario'
                    var pedido = Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
                    carritoUsuario.add(pedido)
                }.addOnFailureListener { e ->
                    // Si no se puede agregar el pedido, se marca 'datosSubidos' como falso
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
        checkBox: CheckBox, index: Int, seccionEnviarCarrito: Int, plato: Extra
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
            } else {
                enviarIdPlatoACarrito[seccionEnviarCarrito].remove(plato.idExtra)
            }
        }

    }

    private fun cargarCheckBox(
        checkBox: CheckBox, index: Int, seccionEnviarCarrito: Int, plato: Plato
    ) {
        if (index >= seleccionCheckBox.size) {
            // Principalmente crea todos y los añade como false ya que al crearse el checkbox esta en false
            seleccionCheckBox.add(false)
        } else {
            //En este caso se hace este when para que cuando cambiemos de secciones que se puedan mantener los checkbox si estan selecciconados o no

            var fila = 0
            // Utiliza un "when" para mantener los checkboxes marcados o desmarcados al cambiar de sección
            when (seccion) {
                "Entrantes" -> {
                    fila = 0
                    // Comprueba si el ID del extra está en la lista de extras seleccionados y marca el checkbox en consecuencia
                    for (i in 0 until enviarIdPlatoACarrito[fila].size) {
                        // Comprueba si el elemento en enviarIdPlatoACarrito[fila] en la posición 'i' es igual a plato.idPlato.
                        if (enviarIdPlatoACarrito[fila][i] == plato.idPlato) {
                            // Si la condición se cumple, establece la casilla de verificación (checkBox) como seleccionada.
                            checkBox.isChecked = true
                            // Sale del bucle, ya que se ha encontrado una coincidencia.
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
            // Si el CheckBox está marcado
            // Actualiza la lista de selección para este ítem a true
            seleccionCheckBox[numerodeplatosSeccion] = true
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Establece un listener para el CheckBox que se dispara cuando su estado cambia

            // Verifica si el índice (index) está dentro del rango válido en la lista seleccionCheckBox
            if (index < seleccionCheckBox.size) {
                // Si el índice ya existe en la lista, actualiza su valor con el estado actual del CheckBox
                seleccionCheckBox[index] = isChecked
            } else {
                // Si el índice no existe en la lista, agrégalo con el estado actual del CheckBox
                seleccionCheckBox.add(isChecked)
            }

            if (isChecked) {
                // Si el CheckBox está marcado
                // Agrega el ID del plato correspondiente a la lista bidimensional enviarIdPlatoACarrito
                // en la posición seccionEnviarCarrito
                enviarIdPlatoACarrito[seccionEnviarCarrito].add(plato.idPlato)
            } else {
                // Si el CheckBox está desmarcado
                // Elimina el ID del plato de la misma lista bidimensional enviarIdPlatoACarrito
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
            // Crea una nueva instancia de la clase Pedidos y asigna los valores iniciales
            // de carritoUsuario, listaPlatos y listaExtras.
            val fragment = Pedidos(carritoUsuarioRe, listaPlatosRe, listaExtrasRe)

            // Asigna la lista de platos y extras a la instancia del fragmento.
            fragment.listaPlatos = listaPlatosRe
            fragment.listaExtras = listaExtrasRe

            // Crea un Bundle (conjunto de argumentos) para pasar datos adicionales al fragmento.
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)

            // Asigna los argumentos al fragmento.
            fragment.arguments = args

            // Devuelve la instancia del fragmento Pedidos con los argumentos y valores iniciales adecuados.
            return fragment
        }
    }

}