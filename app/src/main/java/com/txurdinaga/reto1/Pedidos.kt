package com.txurdinaga.reto1

import Plato
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Pedidos(listaPlatos: ArrayList<Plato>) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listaPlatos: ArrayList<Plato> = ArrayList() // Inicializa la lista de platos

    lateinit var linearLayout: LinearLayout

    var tipo: Tipo = Tipo.MENU

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

        val switch = view.findViewById<Switch>(R.id.switch2)
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tipo = Tipo.CARTA
                cargarPedidos(inflater, container)
            } else {
                tipo = Tipo.MENU
                cargarPedidos(inflater, container)
            }
        }

        cargarPedidos(inflater, container)

        return view
    }

    private fun cargarPedidos(inflater: LayoutInflater, container: ViewGroup?) {
        linearLayout.removeAllViews()

        for (plato in listaPlatos) {
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
            } else {
                layoutMostrarPrecioCantidad.visibility = View.GONE
                imgCerrarDescripcion.visibility = View.GONE
                checkBox.visibility = View.GONE
                radioButton.visibility = View.VISIBLE
            }

            linearLayout.addView(itemLayout)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String, listaPlatos: ArrayList<Plato>): Pedidos {
            val fragment = Pedidos(listaPlatos)
            fragment.listaPlatos = listaPlatos
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}
