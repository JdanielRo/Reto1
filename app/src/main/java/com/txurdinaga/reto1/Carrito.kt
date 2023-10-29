package com.txurdinaga.reto1

import Extra
import Plato
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.DecimalFormat

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class Carrito(
    carritoUsuarioRe: ArrayList<Pedido>,
    listaPlatosRe: ArrayList<Plato>,
    listaExtrasRe: ArrayList<Extra>
) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    private var listaPlatos: ArrayList<Plato> = listaPlatosRe
    private var listaExtras: ArrayList<Extra> = listaExtrasRe

    private lateinit var linearLayout: LinearLayout

    private var carritoUsuario: ArrayList<Pedido> = carritoUsuarioRe

    private var stockPlatos: MutableList<Stock> = mutableListOf()
    private var stockExtras: MutableList<Stock> = mutableListOf()

    private var precioTotal: Double = 0.0
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

        CoroutineScope(Dispatchers.IO).launch {
            //No continua hasta que se terminen de ejecutar las dos funciones
            stockPlatos = async { comprobarStock("Plato") }.await()
            stockExtras = async { comprobarStock("Extra") }.await()
        }

        añadirCarritoAlLinearLayout(inflater, container, calcularIdMenuMasAlto(), view)
        val imgPagarCarrito: AppCompatImageButton = view.findViewById(R.id.imgPagarCarrito)
        // Acción de botón de pagar
        imgPagarCarrito.setOnClickListener {
            linearLayout.removeAllViews()
            addSecondXMLViews()  // Llamar a la función para agregar la vista de pago
        }

        return view
    }

    private fun addSecondXMLViews() {
        // Crear una instancia de CarritoPagar y realizar la transacción del fragmento
        val carritoPagarFragment = CarritoPagar.newInstance("param1", "param2", carritoUsuario)

        val transaction = requireActivity().supportFragmentManager.beginTransaction()

        transaction.replace(R.id.layoutParent, carritoPagarFragment)
        transaction.addToBackStack(null)
        transaction.commit()
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
        calcularIdMenuMasAlto: Int,
        view: View
    ) {
        linearLayout.removeAllViews()
        precioTotal = 0.0
        for (i in 0 until carritoUsuario.size) {
            if (carritoUsuario[i].idPedido == 0) {
                if (carritoUsuario[i].idMenu == 0) {
                    if (carritoUsuario[i].idExtra == "") {
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[i].idPlato) {
                                val itemLayout =
                                    inflater.inflate(R.layout.item_plato, container, false)
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
                                val imgEliminarPlatoMenu =
                                    itemLayout.findViewById<ImageView>(R.id.imgEliminarPlatoMenu)

                                calcularStock(spinner)

                                imgEliminarPlatoMenu.setOnClickListener {
                                    db.collection("Pedido")
                                        .whereEqualTo("idUsuario", auth.currentUser?.uid)
                                        .whereEqualTo("idPedido", 0)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            for (document in documents) {
                                                if (document.getString("idPlato") ?: "" == plato.idPlato && document.getLong(
                                                        "idMenu"
                                                    )?.toInt() ?: 0 == 0
                                                ) {
                                                    // Eliminar cada documento que coincida con los criterios de consulta
                                                    db.collection("Pedido").document(document.id)
                                                        .delete()
                                                        .addOnSuccessListener {
                                                            println("Se ha borrado correctamente, ${carritoUsuario[i]}")
                                                            carritoUsuario.remove(carritoUsuario[i])
                                                            añadirCarritoAlLinearLayout(
                                                                inflater,
                                                                container,
                                                                calcularIdMenuMasAlto(),
                                                                view
                                                            )
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Manejo de errores si la eliminación falla
                                                        }
                                                }

                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Manejo de errores si la consulta falla
                                        }
                                }






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
                                precioTotal += plato.precio
                                linearLayout.addView(itemLayout)
                            }

                        }
                    } else {
                        for (extra in listaExtras) {
                            if (extra.idExtra == carritoUsuario[i].idExtra) {
                                val itemLayout =
                                    inflater.inflate(R.layout.item_plato, container, false)
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
                                val imgEliminarPlatoMenu =
                                    itemLayout.findViewById<ImageView>(R.id.imgEliminarPlatoMenu)

                                imgEliminarPlatoMenu.setOnClickListener {
                                    db.collection("Pedido")
                                        .whereEqualTo("idUsuario", auth.currentUser?.uid)
                                        .whereEqualTo("idPedido", 0)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            for (document in documents) {
                                                if (document.getString("idExtra") ?: "" == extra.idExtra && document.getLong(
                                                        "idMenu"
                                                    )?.toInt() ?: 0 == 0
                                                ) {
                                                    // Eliminar cada documento que coincida con los criterios de consulta
                                                    db.collection("Pedido").document(document.id)
                                                        .delete()
                                                        .addOnSuccessListener {
                                                            println("Se ha borrado correctamente, ${carritoUsuario[i]}")
                                                            carritoUsuario.remove(carritoUsuario[i])
                                                            añadirCarritoAlLinearLayout(
                                                                inflater,
                                                                container,
                                                                calcularIdMenuMasAlto(),
                                                                view
                                                            )
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Manejo de errores si la eliminación falla
                                                        }
                                                }

                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Manejo de errores si la consulta falla
                                        }
                                }
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
                                precioTotal += extra.precio
                                linearLayout.addView(itemLayout)
                            }

                        }
                    }
                }


            }

        }
        for (j in 1 until calcularIdMenuMasAlto) {
            val itemLayout =
                inflater.inflate(R.layout.plantilla_menu, container, false)
            var txtPrecio = itemLayout.findViewById<TextView>(R.id.txtPrecioMenuCarrito)
            var itemLayoutContenedor = itemLayout.findViewById<LinearLayout>(R.id.contenedorMenu)
            var sumarPreciosMenu: Double = 0.0
            val imgEliminarPlatoMenu =
                itemLayout.findViewById<ImageView>(R.id.imgEliminarPlatoMenuCarrito)
            var contador = 0
            imgEliminarPlatoMenu.setOnClickListener {
                db.collection("Pedido")
                    .whereEqualTo("idUsuario", auth.currentUser?.uid)
                    .whereEqualTo("idPedido", 0)
                    .whereEqualTo("idMenu", j)
                    .get()
                    .addOnSuccessListener { documents ->

                        for (document in documents) {
                            // Eliminar cada documento que coincida con los criterios de consulta
                            db.collection("Pedido").document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    val iterator = carritoUsuario.iterator()
                                    while (iterator.hasNext()) {
                                        val pedido = iterator.next()
                                        if (pedido.idMenu == j) {
                                            iterator.remove()
                                        }
                                    }
                                    // Eliminación exitosa, realiza las operaciones necesarias
                                    linearLayout.removeView(itemLayout)
                                    añadirCarritoAlLinearLayout(
                                        inflater,
                                        container,
                                        calcularIdMenuMasAlto(),
                                        view
                                    )
                                }
                                .addOnFailureListener { e ->
                                    // Manejo de errores si la eliminación falla
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejo de errores si la consulta falla
                    }
            }

            for (k in 0 until carritoUsuario.size) {
                if (carritoUsuario[k].idPedido == 0 && carritoUsuario[k].idMenu == j) {
                    if (carritoUsuario[k].idExtra == "") {
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "Entrante") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato =
                                    itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                sumarPreciosMenu += plato.precio
                                nombrePlato.text = plato.nombre
                                contador += 1
                                itemLayoutContenedor.addView(itemLayoutMenu)
                                break
                            }

                        }
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "PlatoPrincipal") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato =
                                    itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                sumarPreciosMenu += plato.precio
                                nombrePlato.text = plato.nombre
                                contador += 1
                                itemLayoutContenedor.addView(itemLayoutMenu)
                                break
                            }

                        }
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "Guarnición") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato =
                                    itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                sumarPreciosMenu += plato.precio
                                nombrePlato.text = plato.nombre
                                contador += 1
                                itemLayoutContenedor.addView(itemLayoutMenu)
                                break
                            }

                        }
                    } else {
                        for (extra in listaExtras) {
                            if (extra.idExtra == carritoUsuario[k].idExtra && extra.tipo == "postre") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato =
                                    itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                sumarPreciosMenu += extra.precio
                                nombrePlato.text = extra.nombre
                                contador += 1
                                itemLayoutContenedor.addView(itemLayoutMenu)
                                break
                            }

                        }
                        for (extra in listaExtras) {
                            if (extra.idExtra == carritoUsuario[k].idExtra && extra.tipo == "bebida") {
                                val itemLayoutMenu =
                                    inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato =
                                    itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                sumarPreciosMenu += extra.precio
                                nombrePlato.text = extra.nombre
                                contador += 1
                                itemLayoutContenedor.addView(itemLayoutMenu)
                                break
                            }

                        }
                    }
                }
            }
            txtPrecio.text = "$sumarPreciosMenu€"
            if(contador == 5){
                precioTotal += sumarPreciosMenu
                linearLayout.addView(itemLayout)
            }

        }
        cacularTotal(view)
    }

    private fun calcularStock(spinner: Spinner?) {

    }

    private fun cacularTotal(view: View){
        var precioTotalCarrito = view.findViewById<TextView>(R.id.precioTotalCarrito)
        val df = DecimalFormat("#.##")
        df.roundingMode = java.math.RoundingMode.CEILING
        precioTotalCarrito.text = "${df.format(precioTotal)}€"
    }

    private fun comprobarStock(s: String): ArrayList<Stock>{
        var listaStock: ArrayList<Stock> = ArrayList()
        when(s){
            "Plato" -> {
                db.collection(s)
                    .get()
                    .addOnSuccessListener { result ->

                        for (document in result) {
                            // Eliminar cada documento que coincida con los criterios de consulta
                            var stock: Stock = Stock(document.id, "", document.getLong("stock")?.toInt() ?: 0)
                            listaStock.add(stock)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejo de errores si la consulta falla
                    }
            }
            "Extra" -> {
                db.collection(s)
                    .get()
                    .addOnSuccessListener { result ->

                        for (document in result) {
                            // Eliminar cada documento que coincida con los criterios de consulta
                            var stock: Stock = Stock("", document.id, document.getLong("stock")?.toInt() ?: 0)
                            listaStock.add(stock)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejo de errores si la consulta falla
                    }
            }
        }
        return listaStock
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