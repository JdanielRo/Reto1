package com.txurdinaga.reto1

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Pedidos : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pedidos, container, false)
        val linearLayout = view.findViewById<LinearLayout>(R.id.linearLayoutScrollPedidos)

        // Repite la inclusión del diseño 5 veces
        for (i in 1..7) {
            // Infla el diseño item_layout.xml
            val itemLayout = inflater.inflate(R.layout.fragment_pedidos_platos, container, false)

            // Agrega el diseño al LinearLayout
            linearLayout.addView(itemLayout)
            val space = Space(requireContext())
            if(i==7){
                space.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.space_height_ultima) // Cambia esto según tu altura deseada
                )
            }else{
                // Agrega un espacio entre cada diseño

                space.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.space_height) // Cambia esto según tu altura deseada
                )
            }

            linearLayout.addView(space)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Pedidos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

