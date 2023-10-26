package com.txurdinaga.reto1

import Extra
import Plato
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [Carrito.newInstance] factory method to
 * create an instance of this fragment.
 */
class Carrito(
    carritoUsuarioRe: ArrayList<Pedido>,
    listaPlatosRe: ArrayList<Plato>,
    listaExtrasRe: ArrayList<Extra>
) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var listaPlatos: ArrayList<Plato> = listaPlatosRe
    private var listaExtras: ArrayList<Extra> = listaExtrasRe

    private lateinit var linearLayout: LinearLayout

    private var carritoUsuario: ArrayList<Pedido> = carritoUsuarioRe

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
        val view = inflater.inflate(R.layout.fragment_carrito, container, false)
        // Inflate the layout for this fragment
        linearLayout = view.findViewById(R.id.containerLayout)

        añadirCarritoAlLinearLayout(inflater, container, calcularIdMenuMasAlto())
        return view
    }

    private fun calcularIdMenuMasAlto(): Int {
        var numero: Int = 0
        for (pedido in carritoUsuario) {
            if (pedido.idMenu > numero) {
                numero = pedido.idMenu
            }
        }
        return numero + 1
    }

    private fun añadirCarritoAlLinearLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        calcularIdMenuMasAlto: Int
    ) {

        for (i in 0 until carritoUsuario.size) {
            if (carritoUsuario[i].idPedido == 0) {
                if (carritoUsuario[i].idMenu == 0) {
                    if(carritoUsuario[i].idExtra == ""){
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[i].idPlato) {
                                val itemLayout =
                                    inflater.inflate(R.layout.layout_plato, container, false)
                                val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                                val imgCerrarDescripcion =
                                    itemLayout.findViewById<ImageView>(R.id.imageView5)
                                val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
                                val txtPrecioPlatoPedidos =
                                    itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                                val layoutMostrarPrecioCantidad =
                                    itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                                val txtDescripcionPlatoPedidos =
                                    itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                                val txtDescripcionPlato =
                                    itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                                val txtNombrePlato =
                                    itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                                txtNombrePlato.text = plato.nombre
                                txtDescripcionPlatoPedidos.text = plato.descripcion
                                txtPrecioPlatoPedidos.text = plato.precio.toString()

                                txtDescripcionPlato.setOnClickListener {
                                    layoutMostrarPrecioCantidad.visibility = View.GONE
                                    txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                                    imgCerrarDescripcion.visibility = View.VISIBLE
                                }

                                imgCerrarDescripcion.setOnClickListener {
                                    layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                                    txtDescripcionPlatoPedidos.visibility = View.GONE
                                    imgCerrarDescripcion.visibility = View.GONE
                                }

                                txtDescripcionPlatoPedidos.visibility = View.GONE
                                layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                                imgCerrarDescripcion.visibility = View.GONE
                                linearLayout.addView(itemLayout)
                            }

                        }
                    }else{
                        for (extra in listaExtras) {
                            if (extra.idExtra == carritoUsuario[i].idExtra) {
                                val itemLayout =
                                    inflater.inflate(R.layout.layout_plato, container, false)
                                val checkBox = itemLayout.findViewById<CheckBox>(R.id.checkBox)
                                val imgCerrarDescripcion =
                                    itemLayout.findViewById<ImageView>(R.id.imageView5)
                                val spinner = itemLayout.findViewById<Spinner>(R.id.spinner)
                                val txtPrecioPlatoPedidos =
                                    itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                                val layoutMostrarPrecioCantidad =
                                    itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                                val txtDescripcionPlatoPedidos =
                                    itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                                val txtDescripcionPlato =
                                    itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                                val txtNombrePlato =
                                    itemLayout.findViewById<TextView>(R.id.txtNombrePlato)

                                txtNombrePlato.text = extra.nombre
                                txtDescripcionPlatoPedidos.text = extra.descripcion
                                txtPrecioPlatoPedidos.text = extra.precio.toString()

                                txtDescripcionPlato.setOnClickListener {
                                    layoutMostrarPrecioCantidad.visibility = View.GONE
                                    txtDescripcionPlatoPedidos.visibility = View.VISIBLE
                                    imgCerrarDescripcion.visibility = View.VISIBLE
                                }

                                imgCerrarDescripcion.setOnClickListener {
                                    layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                                    txtDescripcionPlatoPedidos.visibility = View.GONE
                                    imgCerrarDescripcion.visibility = View.GONE
                                }

                                txtDescripcionPlatoPedidos.visibility = View.GONE
                                layoutMostrarPrecioCantidad.visibility = View.VISIBLE
                                imgCerrarDescripcion.visibility = View.GONE
                                linearLayout.addView(itemLayout)
                            }

                        }
                    }
                }


            }
        }
        for(j in 1 until calcularIdMenuMasAlto){
            val itemLayout =
                inflater.inflate(R.layout.plantilla_menu, container, false)
            var layoutItem = itemLayout.findViewById<LinearLayout>(R.id.contenedorMenu)
            for (k in 0 until carritoUsuario.size) {
                if (carritoUsuario[k].idPedido == 0 && carritoUsuario[k].idMenu == j) {
                    if(carritoUsuario[k].idExtra == ""){
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "Entrante") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                nombrePlato.text = plato.nombre
                                layoutItem.addView(itemLayoutMenu)
                                break
                            }

                        }
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "PlatoPrincipal") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                nombrePlato.text = plato.nombre
                                layoutItem.addView(itemLayoutMenu)
                                break
                            }

                        }
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "Guarnición") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                nombrePlato.text = plato.nombre
                                layoutItem.addView(itemLayoutMenu)
                                break
                            }

                        }
                    }else{
                        for (extra in listaExtras) {
                            if (extra.idExtra == carritoUsuario[k].idExtra && extra.tipo == "postre") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                nombrePlato.text = extra.nombre
                                layoutItem.addView(itemLayoutMenu)
                                break
                            }

                        }
                        for (extra in listaExtras) {
                            if (extra.idExtra == carritoUsuario[k].idExtra && extra.tipo == "bebida") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                nombrePlato.text = extra.nombre
                                layoutItem.addView(itemLayoutMenu)
                                break
                            }

                        }
                    }
                }
            }
            linearLayout.addView(itemLayout)
        }
    }


    companion object {
        fun newInstance(
            param1: String,
            param2: String,
            carritoUsuarioRe: ArrayList<Pedido>,
            listaPlatosRe: ArrayList<Plato>,
            listaExtrasRe: ArrayList<Extra>
        ):
                Carrito {
            val fragment = Carrito(carritoUsuarioRe, listaPlatosRe, listaExtrasRe)
            fragment.carritoUsuario = carritoUsuarioRe
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}