package com.txurdinaga.reto1

import Extra
import Plato
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Pedidos(listaPlatosRe: ArrayList<Plato>, listaExtrasRe: ArrayList<Extra>) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listaPlatos: ArrayList<Plato> = listaPlatosRe
    private var listaExtras: ArrayList<Extra> = listaExtrasRe
    private var seccion: String =
        "Entrantes"
    private lateinit var linearLayout: LinearLayout

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

        for (i in 0 until listaPlatos.size) {
            println(listaPlatos[i])
        }
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
            vaciarEnvioACarrito()
            cargarPedidos(inflater, container)
        }
        for (i in 0 until seleccionCheckBox.size) {
            println(seleccionCheckBox[i])
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
                    seccion = "Platos Principales"
                    cargarPedidos(inflater, container)
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
                    seccion = "Entrantes"
                    cargarPedidos(inflater, container)
                }
                btnSiguiente.setOnClickListener {
                    seccion = "Guarniciones"
                    cargarPedidos(inflater, container)
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
                    seccion = "Platos Principales"
                    cargarPedidos(inflater, container)
                }
                btnSiguiente.setOnClickListener {
                    seccion = "Postres"
                    cargarPedidos(inflater, container)
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
                    seccion = "Guarniciones"
                    cargarPedidos(inflater, container)
                }
                btnSiguiente.setOnClickListener {
                    seccion = "Bebidas"
                    cargarPedidos(inflater, container)
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
                btnAtras.setOnClickListener {
                    seccion = "Postres"
                    cargarPedidos(inflater, container)
                }
                /*btnSiguiente.setOnClickListener {
                    seccion = "Entrante"
                    cargarPedidos(inflater, container)
                }*/
                linearLayout.addView(itemLayout)
            }
        }
    }

    private fun cargarCheckBox(
        checkBox: CheckBox,
        index: Int,
        seccionEnviarCarrito: Int,
        plato: Extra
    ) {
        if (index >= seleccionCheckBox.size) {
            seleccionCheckBox.add(false)
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

            for (i in 0 until seleccionCheckBox.size) {
                println(seleccionCheckBox[i])
            }
            for (i in 0 until enviarIdPlatoACarrito.size) {
                for (j in 0 until enviarIdPlatoACarrito[i].size) {
                    println(enviarIdPlatoACarrito[i][j])
                }
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
            for (i in 0 until seleccionCheckBox.size) {
                println(seleccionCheckBox[i])
            }
            for (i in 0 until enviarIdPlatoACarrito.size) {
                for (j in 0 until enviarIdPlatoACarrito[i].size) {
                    println(enviarIdPlatoACarrito[i][j])
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(
            param1: String,
            param2: String,
            listaPlatosRe: ArrayList<Plato>,
            listaExtrasRe: ArrayList<Extra>
        ): Pedidos {
            val fragment = Pedidos(listaPlatosRe, listaExtrasRe)
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