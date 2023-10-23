
package com.txurdinaga.reto1

import Extra
import Plato
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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

    private var carritoUsuario:ArrayList<Pedido> = carritoUsuarioRe

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_carrito, container, false)
    }

    companion object {
        fun newInstance(param1: String, param2: String, carritoUsuarioRe: ArrayList<Pedido>, listaPlatosRe: ArrayList<Plato>,
                        listaExtrasRe: ArrayList<Extra>):
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