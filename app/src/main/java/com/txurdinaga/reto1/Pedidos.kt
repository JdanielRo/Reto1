package com.txurdinaga.reto1

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log // Importa Log para el manejo de errores
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import com.google.firebase.firestore.QuerySnapshot

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Pedidos : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseFirestore.getInstance()

    private lateinit var linearLayout: LinearLayout

    private val listaPlatos: MutableList<Plato> = mutableListOf()
    private val listaMenus: MutableList<Menu> = mutableListOf()

    private val values = listOf(1000, 1500, 2000, 2500)

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
        linearLayout = view.findViewById(R.id.linearLayoutScrollPedidos)

        mostrarPanelSuperior(inflater, container, "menus")
        obtenerPlatos()
        obtenerMenus()
        return view
    }

    private fun mostrarPanelSuperior(
        inflater: LayoutInflater,
        container: ViewGroup?,
        tipo: String
    ) {
        linearLayout.removeAllViews()
        val itemLayout = inflater.inflate(R.layout.layout_pedidos_menu_superior, container, false)
        linearLayout.addView(itemLayout)

        val btnMostrarMenus = itemLayout.findViewById<Button>(R.id.btnPedidosMenus)
        val btnMostrarPlatos = itemLayout.findViewById<Button>(R.id.btnPedidosPlatos)

        val seekBar = itemLayout.findViewById<SeekBar>(R.id.seekBar)
        val selectedValue = itemLayout.findViewById<TextView>(R.id.txtCaloriasSeleccionar)

        seekBar.max = values.size - 1
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedValue.text = "${R.string.calorias} ${values[progress].toString()}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        btnMostrarMenus.setOnClickListener {
            if (listaMenus.isNotEmpty()) {
                mostrarMenus(inflater, container)
            }
        }
        btnMostrarPlatos.setOnClickListener {
            if (listaPlatos.isNotEmpty()) {
                mostrarPlatos(inflater, container)
            }
        }
    }

    fun obtenerPlatos() {
        var suma = 1
        db.collection("Platos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val celiaco = document.getBoolean("celiaco") ?: false
                    val calorias = document.getLong("calorias")?.toInt() ?: 0
                    val precio = document.getDouble("precio") ?: 0.0
                    val cantidad = document.getLong("cantidad")?.toInt() ?: 0
                    val id_plato = document.id.toIntOrNull() ?: 0
                    val plato =
                        Plato(nombre, descripcion, celiaco, calorias, precio, cantidad, id_plato)
                    println("$suma.- $plato")
                    listaPlatos.add(plato)
                    suma++
                }
            }
            .addOnFailureListener { e ->
                // Maneja el error apropiadamente, por ejemplo, muestra un mensaje al usuario.
                Log.e(TAG, "Error al obtener platos: ${e.message}", e)
            }
    }

    fun obtenerMenus() {
        db.collection("Menus")
            .get()
            .addOnSuccessListener { resultMenus ->
                db.collection("Platos")
                    .get()
                    .addOnSuccessListener { resultPlatos ->
                        for (document in resultMenus) {
                            val celiaco = document.getBoolean("celiaco") ?: false
                            val calorias = document.getLong("calorias")?.toInt() ?: 0
                            val platos = document.get("platos") as ArrayList<Long>
                            val precio: Double = calcularPrecioMenu(resultPlatos, platos)
                            val cantidad: Int = calcularCantidadMenu(resultPlatos, platos)
                            val id_menu = document.id
                            val tipo_comida = document.getString("tipo_comida") ?: ""
                            val menu = Menu(
                                celiaco,
                                calorias,
                                precio,
                                cantidad,
                                id_menu,
                                tipo_comida,
                                platos
                            )
                            println(menu)
                            listaMenus.add(menu)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Maneja el error apropiadamente, por ejemplo, muestra un mensaje al usuario.
                        Log.e(TAG, "Error al obtener platos: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                // Maneja el error apropiadamente, por ejemplo, muestra un mensaje al usuario.
                Log.e(TAG, "Error al obtener menús: ${e.message}", e)
            }
    }

    private fun calcularCantidadMenu(resultPlatos: QuerySnapshot, platos: ArrayList<Long>): Int {
        var cantidadFinal: Int = 0
        var numero_de_cantidad: Int = 0
        for (platoId in platos) {
            for (document in resultPlatos) {
                if (document.id == platoId.toString()) {
                    if (numero_de_cantidad == 0) {
                        cantidadFinal = document.getLong("cantidad")?.toInt() ?: 0
                    } else if (cantidadFinal > document.getLong("cantidad")?.toInt() ?: 0) {
                        cantidadFinal = document.getLong("cantidad")?.toInt() ?: 0
                    }
                    numero_de_cantidad++
                    break
                }
            }
        }
        return cantidadFinal
    }

    private fun calcularPrecioMenu(resultPlatos: QuerySnapshot, platos: ArrayList<Long>): Double {
        var totalPrecio: Double = 0.0
        for (platoId in platos) {
            for (document in resultPlatos) {
                if (document.id == platoId.toString()) {
                    totalPrecio += document.getDouble("precio") ?: 0.0
                    break
                }
            }
        }
        return totalPrecio
    }

    private fun mostrarPlatos(inflater: LayoutInflater, container: ViewGroup?) {
        mostrarPanelSuperior(inflater, container, "platos")
        for (plato in listaPlatos) {
            val itemLayout = inflater.inflate(R.layout.layout_pedidos_platos, container, false)
            val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlatos)
            val txtCaloriaPlato = itemLayout.findViewById<TextView>(R.id.txtCaloriaPlato)
            val txtPrecioPlato = itemLayout.findViewById<TextView>(R.id.txtPrecioPlatos)
            val spinner = itemLayout.findViewById<Spinner>(R.id.spinnerNumbers)

            txtNombrePlato.text = plato.nombre
            txtCaloriaPlato.text = plato.calorias.toString()
            txtPrecioPlato.text = "${plato.precio}€"

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                (1..plato.cantidad).map { it.toString() }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedNumber = parent?.getItemAtPosition(position).toString()
                    //Log.d(TAG, "Numero de Plato: ${plato.id_plato}, número seleccionado: $selectedNumber")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            linearLayout.addView(itemLayout)
        }
    }

    private fun mostrarMenus(inflater: LayoutInflater, container: ViewGroup?) {
        mostrarPanelSuperior(inflater, container, "menus")


        for (menu in listaMenus) {
            val itemLayout = inflater.inflate(R.layout.layout_pedidos_menus, container, false)

            val txtMenu1plato = itemLayout.findViewById<TextView>(R.id.txtMenu1plato)
            val txtMenu2plato = itemLayout.findViewById<TextView>(R.id.txtMenu2plato)
            val txtMenu3plato = itemLayout.findViewById<TextView>(R.id.txtMenu3plato)
            /*val imgMenu1plato = itemLayout.findViewById<TextView>(R.id.imgMenu1plato)
            val imgMenu2plato = itemLayout.findViewById<TextView>(R.id.imgMenu2plato)
            val imgMenu3plato = itemLayout.findViewById<TextView>(R.id.imgMenu3plato)*/
            val txtCaloriaMenu = itemLayout.findViewById<TextView>(R.id.txtCaloriaMenu)
            val txtPrecioMenu = itemLayout.findViewById<TextView>(R.id.txtPrecioMenu)

            txtCaloriaMenu.text = menu.calorias.toString()
            txtPrecioMenu.text = "${menu.precio}€"
            txtMenu1plato.text = "Primer Plato: ${cogerTextoMenu(menu.platos[0].toInt())}"
            txtMenu2plato.text = "Segundo Plato: ${cogerTextoMenu(menu.platos[1].toInt())}"
            txtMenu3plato.text = "Postre: ${cogerTextoMenu(menu.platos[2].toInt())}"
            /*imgMenu1plato.text = cogerImgMenu(1, resultPlatos)
            imgMenu2plato.text = cogerImgMenu(2, resultPlatos)
            imgMenu3plato.text = cogerImgMenu(3, resultPlatos)*/
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                (1..menu.cantidad).map { it.toString() }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner = itemLayout.findViewById<Spinner>(R.id.spinnerNumbersMenu)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedNumber = parent?.getItemAtPosition(position).toString()
                    //Log.d(TAG, "Numero de Menu: ${menu.id_menu}, número seleccionado: $selectedNumber")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            linearLayout.addView(itemLayout)
        }
    }


    private fun cogerTextoMenu(idPlato: Int): String {
        var texto: String = ""
        for (plato in listaPlatos) {
            if (plato.id_plato == idPlato) {
                texto = plato.nombre
                break
            }
        }
        return texto
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

