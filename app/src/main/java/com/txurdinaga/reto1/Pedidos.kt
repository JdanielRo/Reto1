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
    private var seccion: String = "Entrantes"//, "PlatosPrincipales", "Guarnicion", "Postre", "Bebida")
    private lateinit var linearLayout: LinearLayout

    private var listaPlatosEntrantes: ArrayList<Plato> = ArrayList()
    private var listaPlatosPrincipales: ArrayList<Plato> = ArrayList()
    private var listaPlatosGuarnicion: ArrayList<Plato> = ArrayList()
    private var listaPlatosPostre: ArrayList<Plato> = ArrayList()
    private var listaPlatosBebida: ArrayList<Plato> = ArrayList()

    private var tipo: Tipo = Tipo.MENU

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

        dividirListas()

        val switch = view.findViewById<Switch>(R.id.switch2)
        switch.setOnCheckedChangeListener { _, isChecked ->
            tipo = if (isChecked) Tipo.CARTA else Tipo.MENU
            cargarPedidos(inflater, container)
        }

        cargarPedidos(inflater, container)

        return view
    }

    private fun dividirListas() {
        for (plato in listaPlatos){
            println("${plato.tipo}")
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
        var radioGroup: RadioGroup? = null // Inicializa radioGroup como un RadioGroup nulo

        if (tipo == Tipo.MENU) {
            radioGroup = view?.findViewById(R.id.radioGroupPedidos) // Asigna el RadioGroup desde la vista
        }
        when (seccion) {
            "Entrantes" -> {
                for (plato in listaPlatosEntrantes) {
                    println(plato)
                    val itemLayout = inflater.inflate(R.layout.layout_plato, container, false)
                    val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                    val radioButton = itemLayout.findViewById<RadioButton>(R.id.radioButton2)
                    val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                    val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
                    val txtPrecioPlatoPedidos = itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                    val layoutMostrarPrecioCantidad = itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                    val txtDescripcionPlatoPedidos = itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                    val txtDescripcionPlato = itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)

                    txtDescripcionPlato.setOnClickListener {
                        if (tipo == Tipo.CARTA) {
                            layoutMostrarPrecioCantidad.visibility = View.GONE
                            txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                            imgCerrarDescripcion.visibility = View.VISIBLE
                        } else {

                        }
                    }

                    imgCerrarDescripcion.setOnClickListener {
                        layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                    }
                    imgCerrarDescripcion.visibility = View.GONE

                    if (tipo == Tipo.CARTA) {
                        checkBox.visibility = View.VISIBLE
                        radioButton.visibility = View.GONE
                        txtDescripcionPlatoPedidos.visibility = View.GONE
                        añadirBotonAlGrupo(radioGroup, radioButton)
                    } else {
                        layoutMostrarPrecioCantidad.visibility = View.GONE
                        imgCerrarDescripcion.visibility = View.GONE
                        checkBox.visibility = View.GONE
                        radioButton.visibility = View.VISIBLE
                    }

                    linearLayout.addView(itemLayout)
                }
            }
            // Maneja otras secciones aquí
        }
    }

    private fun añadirBotonAlGrupo(radioGroup: RadioGroup?, radioButton: RadioButton) {
        radioGroup?.addView(radioButton)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String, listaPlatosRe: ArrayList<Plato>, listaExtrasRe: ArrayList<Extra>): Pedidos {
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