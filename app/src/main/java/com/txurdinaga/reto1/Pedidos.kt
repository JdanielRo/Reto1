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
        "Entrantes"//, "PlatosPrincipales", "Guarnicion", "Postre", "Bebida")
    private lateinit var linearLayout: LinearLayout

    private var seleccionCheckBox: ArrayList<Boolean> = ArrayList()

    private var listaPlatosEntrantes: ArrayList<Plato> = ArrayList()
    private var listaPlatosPrincipales: ArrayList<Plato> = ArrayList()
    private var listaPlatosGuarnicion: ArrayList<Plato> = ArrayList()
    private var listaPlatosPostre: ArrayList<Plato> = ArrayList()
    private var listaPlatosBebida: ArrayList<Plato> = ArrayList()

    private var tipo: Tipo = Tipo.MENU

    private var numerodeplatosSeccion: Int = 0

    private lateinit var switch: Switch

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

        // Asegúrate de inicializar listaPlatos y listaExtras en algún lugar antes de usarlos.

        for (i in 0 until listaPlatos.size) {
            println(listaPlatos[i])
        }

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
                "Postre" -> listaPlatosPostre.add(plato)
                "Bebida" -> listaPlatosBebida.add(plato)
            }
        }
    }

    private fun cargarPedidos(inflater: LayoutInflater, container: ViewGroup?) {
        linearLayout.removeAllViews()
        numerodeplatosSeccion = 0
        seleccionCheckBox.ensureCapacity(listaPlatosEntrantes.size)
        switch.setOnCheckedChangeListener { _, isChecked ->
            for (i in 0 until seleccionCheckBox.size) {
                seleccionCheckBox[i] = false
            }
            cargarPedidos(inflater, container)
        }
        for (i in 0 until seleccionCheckBox.size) {
            println(seleccionCheckBox[i])
        }
        when (seccion) {
            "Entrantes" -> {
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
                        imgCerrarDescripcion.visibility = View.VISIBLE

                    } else {
                        txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }

                    cargarCheckBox(checkBox, numerodeplatosSeccion)

                    linearLayout.addView(itemLayout)
                    numerodeplatosSeccion += 1
                }
            }
        }
    }

    private fun cargarCheckBox(checkBox: CheckBox, index: Int) {
        if (index >= seleccionCheckBox.size) {
            seleccionCheckBox.add(false)
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (index < seleccionCheckBox.size) {
                seleccionCheckBox[index] = isChecked
            } else {
                seleccionCheckBox.add(isChecked)
            }

            for (i in 0 until seleccionCheckBox.size) {
                println(seleccionCheckBox[i])
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