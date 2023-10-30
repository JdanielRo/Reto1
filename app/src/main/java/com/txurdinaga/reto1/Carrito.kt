package com.txurdinaga.reto1

import Extra
import Plato
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import kotlin.math.roundToInt
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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
        añadirCarritoAlLinearLayout(inflater, container, calcularIdMenuMasAlto(), view)
        val imgPagarCarrito: AppCompatImageButton = view.findViewById(R.id.imgPagarCarrito)
        //imgPagarCarrito.isEnabled = true
        // Acción de botón de pagar
        imgPagarCarrito.setOnClickListener {

            //imgPagarCarrito.isEnabled = false

            Log.d("MiTag", "Tamaño de carritoUsuario: " + carritoUsuario.size)
            linearLayout.removeAllViews()
            if (carritoUsuario.size == 0) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("Debes añadir un plato o un menu al carrito para poder realizar el pago")
                    .setTitle("ERROR")
                builder.setPositiveButton("Aceptar") { dialog, id ->
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                addSecondXMLViews()
                imgPagarCarrito.visibility = View.GONE
            }
            // Llamar a la función para agregar la vista de pago
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

    @SuppressLint("MissingInflatedId")
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
                                val numberPicker: NumberPicker =
                                    itemLayout.findViewById(R.id.numberPicker)
                                numberPicker.minValue = 1
                                numberPicker.maxValue = (plato.stock * 0.1).roundToInt()
                                numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                                    val diferencia =
                                        newVal - oldVal // Obtén la diferencia entre el nuevo y el antiguo valor
                                    precioTotal += plato.precio * diferencia // Calcula el cambio en el precio total
                                    calcularTotal(view) // Actualiza la vista del precio total
                                }

                                val imgCerrarDescripcion =
                                    itemLayout.findViewById<ImageView>(R.id.imageView5)
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

                                val imgPlato =
                                    itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                                cargarImagenFirebase(imgPlato, plato.nombre)
                                imgEliminarPlatoMenu.setOnClickListener {
                                    db.collection("Pedido")
                                        .whereEqualTo("idUsuario", auth.currentUser?.uid)
                                        .whereEqualTo("idPedido", 0)
                                        .whereEqualTo("idPlato", plato.idPlato)
                                        .whereEqualTo("idMenu", 0)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            for (document in documents) {
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
                                val numberPicker: NumberPicker =
                                    itemLayout.findViewById(R.id.numberPicker)
                                numberPicker.minValue = 1
                                numberPicker.maxValue = (extra.stock * 0.1).roundToInt()
                                numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                                    val diferencia =
                                        newVal - oldVal // Obtén la diferencia entre el nuevo y el antiguo valor
                                    precioTotal += extra.precio * diferencia // Calcula el cambio en el precio total
                                    calcularTotal(view) // Actualiza la vista del precio total
                                }
                                val imgCerrarDescripcion =
                                    itemLayout.findViewById<ImageView>(R.id.imageView5)
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
                                val imgPlato =
                                    itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)
                                cargarImagenFirebase(imgPlato, extra.nombre)
                                imgEliminarPlatoMenu.setOnClickListener {
                                    db.collection("Pedido")
                                        .whereEqualTo("idUsuario", auth.currentUser?.uid)
                                        .whereEqualTo("idPedido", 0)
                                        .whereEqualTo("idExtra", extra.idExtra)
                                        .whereEqualTo("idMenu", 0)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            for (document in documents) {
                                                db.collection("Pedido").document(document.id)
                                                    .delete()
                                                    .addOnSuccessListener {
                                                        println("Se ha borrado correctamente, ${carritoUsuario[i]}")
                                                        carritoUsuario.removeAt(i)
                                                        añadirCarritoAlLinearLayout(
                                                            inflater,
                                                            container,
                                                            calcularIdMenuMasAlto(),
                                                            view
                                                        )
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // Manejo de errores si la eliminación falla
                                                        println("Error al eliminar el documento: $e")
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Manejo de errores si la consulta falla
                                            println("Error al realizar la consulta: $e")
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
            val numberPicker: NumberPicker =
                itemLayout.findViewById(R.id.numberPicker)
            numberPicker.minValue = 1
            var stockminimo = 10000
            numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                val diferencia =
                    newVal - oldVal // Obtén la diferencia entre el nuevo y el antiguo valor
                precioTotal += sumarPreciosMenu * diferencia // Calcula el cambio en el precio total
                calcularTotal(view) // Actualiza la vista del precio total
            }
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
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = plato.descripcion
                                val imgPlato =
                                    itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)
                                if (imgPlato != null && plato.nombre.isNotEmpty()) {
                                    cargarImagenFirebase(imgPlato, plato.nombre)
                                } else {
                                    Log.e("FirebaseStorage", "Error: imagen o nombre nulos")
                                }


                                if (plato.stock < stockminimo) {
                                    stockminimo = plato.stock
                                }
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
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = plato.descripcion
                                val imgPlato =
                                    itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)
                                if (imgPlato != null && plato.nombre.isNotEmpty()) {
                                    cargarImagenFirebase(imgPlato, plato.nombre)
                                } else {
                                    Log.e("FirebaseStorage", "Error: imagen o nombre nulos")
                                }
                                if (plato.stock < stockminimo) {
                                    stockminimo = plato.stock
                                }
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
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = plato.descripcion
                                val imgPlato =
                                    itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)
                                if (imgPlato != null && plato.nombre.isNotEmpty()) {
                                    cargarImagenFirebase(imgPlato, plato.nombre)
                                } else {
                                    Log.e("FirebaseStorage", "Error: imagen o nombre nulos")
                                }
                                if (plato.stock < stockminimo) {
                                    stockminimo = plato.stock
                                }
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
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = extra.descripcion
                                val imgPlato =
                                    itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)
                                if (imgPlato != null && extra.nombre.isNotEmpty()) {
                                    cargarImagenFirebase(imgPlato, extra.nombre)
                                } else {
                                    Log.e("FirebaseStorage", "Error: imagen o nombre nulos")
                                }
                                if (extra.stock < stockminimo) {
                                    stockminimo = extra.stock
                                }
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
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = extra.descripcion
                                val imgPlato =
                                    itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)
                                if (imgPlato != null && extra.nombre.isNotEmpty()) {
                                    cargarImagenFirebase(imgPlato, extra.nombre)
                                } else {
                                    Log.e("FirebaseStorage", "Error: imagen o nombre nulos")
                                }
                                if (extra.stock < stockminimo) {
                                    stockminimo = extra.stock
                                }
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
            numberPicker.maxValue = (stockminimo * 0.1).roundToInt()
            val df = DecimalFormat("#.##")
            df.roundingMode = java.math.RoundingMode.CEILING
            sumarPreciosMenu *= 0.9
            txtPrecio.text = "${df.format(sumarPreciosMenu)}€"
            if (contador == 5) {
                precioTotal += sumarPreciosMenu
                linearLayout.addView(itemLayout)
            }

        }
        calcularTotal(view)
    }

    private fun calcularTotal(view: View) {
        var precioTotalCarrito = view.findViewById<TextView>(R.id.precioTotalCarrito)
        val df = DecimalFormat("#.##")
        df.roundingMode = java.math.RoundingMode.CEILING
        precioTotalCarrito.text = "${df.format(precioTotal)}€"
    }


    private fun cargarImagenFirebase(imagen: ImageView, nombre: String) {
        val storage = Firebase.storage
        val storageReference = storage.reference
        val imageRef = storageReference.child("${nombre}.jpg")

        imageRef.downloadUrl
            .addOnSuccessListener { uri ->
                // uri contiene la URL de descarga de la imagen
                val imageUrl = uri.toString()

                // Utiliza una biblioteca como Glide para cargar la imagen en un ImageView
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(imagen) // 'imagen' es tu ImageView
            }
            .addOnFailureListener { exception ->
                // Manejar errores, por ejemplo, si la imagen no se pudo descargar
                Log.e("FirebaseStorage", "Error al descargar la imagen: $exception")
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