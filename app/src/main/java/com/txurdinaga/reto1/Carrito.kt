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
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
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



    private var precioTotal: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var arrayIdPedido: ArrayList<String> = arrayListOf()

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
                builder.setMessage(getString(R.string.anadir_algo))
                    .setTitle("ERROR")
                builder.setPositiveButton(R.string.aceptar) { dialog, id ->
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
        // Limpia la vista actual del LinearLayout
        linearLayout.removeAllViews()

        // Inicializa el precio total a 0
        precioTotal = 0.0

        for (i in 0 until carritoUsuario.size) {// Recorre los elementos en el carrito del usuario
            if (carritoUsuario[i].idPedido == 0) {
                if (carritoUsuario[i].idMenu == 0) {
                    if (carritoUsuario[i].idExtra == "") {
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[i].idPlato) {

                                val itemLayout = inflater.inflate(R.layout.item_plato, container, false)

                                val textViewCantidad: TextView = itemLayout.findViewById(R.id.txtCantidad)

                                val btnRestar: Button = itemLayout.findViewById(R.id.btnRestar)
                                val btnSumar: Button = itemLayout.findViewById(R.id.btnSumar)


                                btnRestar.setOnClickListener{
                                    textViewCantidad.text = (textViewCantidad.text.toString().toInt() - 1).toString()
                                    precioTotal -= plato.precio // Calcula el cambio en el precio total
                                    calcularTotal(view) // Actualiza la vista del precio total
                                    btnSumar.isEnabled = true
                                    if(!(conprobarnumeroRestar(textViewCantidad.text.toString().toInt(), plato))){
                                        btnRestar.isEnabled = false
                                    }

                                }
                                btnSumar.setOnClickListener{
                                    textViewCantidad.text = (textViewCantidad.text.toString().toInt() + 1).toString()
                                    precioTotal += plato.precio // Calcula el cambio en el precio total
                                    calcularTotal(view) // Actualiza la vista del precio total
                                    btnRestar.isEnabled = true
                                    if(!(conprobarnumeroSumar(textViewCantidad.text.toString().toInt(), plato))){
                                        btnSumar.isEnabled = false
                                    }

                                }

                                val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                                val txtPrecioPlatoPedidos = itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                                val layoutMostrarPrecioCantidad = itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                                val txtDescripcionPlatoPedidos = itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                                val txtDescripcionPlato = itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                                val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)
                                val imgEliminarPlatoMenu = itemLayout.findViewById<ImageView>(R.id.imgEliminarPlatoMenu)
                                val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)

                                cargarImagenFirebase(imgPlato, plato.nombre)

                                imgEliminarPlatoMenu.setOnClickListener {
                                    db.collection("Pedido")
                                        .whereEqualTo("idUsuario", auth.currentUser?.uid)
                                        .whereEqualTo("idPedido", 0)
                                        .whereEqualTo("idPlato", plato.idPlato)
                                        .whereEqualTo("idMenu", 0)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            if (!documents.isEmpty) { // Verificar si la lista de documentos no está vacía
                                                val document = documents.documents[0]
                                                // Eliminar cada documento que coincida con los criterios de consulta
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
                                                    }
                                            } else {
                                                // Manejar el caso en el que no hay documentos
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Manejo de errores si la consulta falla
                                        }
                                }

                                txtNombrePlato.text = plato.nombre
                                txtDescripcionPlatoPedidos.text = plato.descripcion
                                txtPrecioPlatoPedidos.text = "${plato.precio}€"

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

                                val itemLayout = inflater.inflate(R.layout.item_plato, container, false)

                                val textViewCantidad: TextView = itemLayout.findViewById(R.id.txtCantidad)

                                val btnRestar: Button = itemLayout.findViewById(R.id.btnRestar)
                                val btnSumar: Button = itemLayout.findViewById(R.id.btnSumar)


                                btnRestar.setOnClickListener{
                                    textViewCantidad.text = (textViewCantidad.text.toString().toInt() - 1).toString()
                                    precioTotal -= extra.precio // Calcula el cambio en el precio total
                                    calcularTotal(view) // Actualiza la vista del precio total
                                    btnSumar.isEnabled = true
                                    if(!(conprobarnumeroRestar(textViewCantidad.text.toString().toInt(), extra))){
                                        btnRestar.isEnabled = false
                                    }

                                }
                                btnSumar.setOnClickListener{
                                    textViewCantidad.text = (textViewCantidad.text.toString().toInt() + 1).toString()
                                    precioTotal += extra.precio // Calcula el cambio en el precio total
                                    calcularTotal(view) // Actualiza la vista del precio total
                                    btnRestar.isEnabled = true
                                    if(!(conprobarnumeroSumar(textViewCantidad.text.toString().toInt(), extra))){
                                        btnSumar.isEnabled = false
                                    }

                                }

                                val imgCerrarDescripcion = itemLayout.findViewById<ImageView>(R.id.imageView5)
                                val txtPrecioPlatoPedidos = itemLayout.findViewById<TextView>(R.id.txtPrecioPlatoPedidos)
                                val layoutMostrarPrecioCantidad = itemLayout.findViewById<LinearLayout>(R.id.layoutMostrarPrecioCantidad)
                                val txtDescripcionPlatoPedidos = itemLayout.findViewById<TextView>(R.id.txtDescripcionPlatoPedidos)
                                val txtDescripcionPlato = itemLayout.findViewById<TextView>(R.id.txtDescripcionPlato)
                                val txtNombrePlato = itemLayout.findViewById<TextView>(R.id.txtNombrePlato)
                                val imgEliminarPlatoMenu = itemLayout.findViewById<ImageView>(R.id.imgEliminarPlatoMenu)
                                val imgPlato = itemLayout.findViewById<ImageView>(R.id.imgPlatoLayout)

                                cargarImagenFirebase(imgPlato, extra.nombre)

                                imgEliminarPlatoMenu.setOnClickListener {
                                    db.collection("Pedido")
                                        .whereEqualTo("idUsuario", auth.currentUser?.uid)
                                        .whereEqualTo("idPedido", 0)
                                        .whereEqualTo("idExtra", extra.idExtra)
                                        .whereEqualTo("idMenu", 0)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            if (!documents.isEmpty) { // Verificar si la lista de documentos no está vacía
                                                val document = documents.documents[0]
                                                // Eliminar cada documento que coincida con los criterios de consulta
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
                                                    }
                                            } else {
                                                // Manejar el caso en el que no hay documentos
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Manejo de errores si la consulta falla
                                        }

                                }

                                txtNombrePlato.text = extra.nombre
                                txtDescripcionPlatoPedidos.text = extra.descripcion
                                txtPrecioPlatoPedidos.text = "${extra.precio}€"

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

            val itemLayout = inflater.inflate(R.layout.plantilla_menu, container, false)
            var txtPrecio = itemLayout.findViewById<TextView>(R.id.txtPrecioMenuCarrito)
            var itemLayoutContenedor = itemLayout.findViewById<LinearLayout>(R.id.contenedorMenu)
            var sumarPreciosMenu: Double = 0.0
            val imgEliminarPlatoMenu = itemLayout.findViewById<ImageView>(R.id.imgEliminarPlatoMenuCarrito)
            val numberPicker: NumberPicker = itemLayout.findViewById(R.id.numberPicker)

            numberPicker.minValue = 1

            var stockminimo = 10000

            numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                val diferencia = newVal - oldVal // Obtén la diferencia entre el nuevo y el antiguo valor
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

            // Recorre los elementos en el carrito del usuario
            for (k in 0 until carritoUsuario.size) {
                if (carritoUsuario[k].idPedido == 0 && carritoUsuario[k].idMenu == j) {
                    // Verifica si el elemento en el carrito es un menú con una coincidencia en el "idMenu"

                    if (carritoUsuario[k].idExtra == "") {
                        // Este bloque se ejecuta si el elemento en el carrito es un plato

                        // Recorre la lista de platos
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "Entrante") {
                                // Verifica si el plato coincide con el "idPlato" y tiene un tipo "Entrante"

                                // Crea una vista de menú para el plato
                                val itemLayoutMenu = inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = plato.descripcion
                                val imgPlato = itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)

                                // Carga la imagen del plato desde Firebase
                                if (imgPlato != null && plato.nombre.isNotEmpty()) {
                                    cargarImagenFirebase(imgPlato, plato.nombre)
                                } else {
                                    Log.e("FirebaseStorage", "Error: imagen o nombre nulos")
                                }

                                // Actualiza el stock mínimo si es necesario
                                if (plato.stock < stockminimo) {
                                    stockminimo = plato.stock
                                }

                                // Suma el precio del plato al precio total del menú
                                sumarPreciosMenu += plato.precio

                                // Configura el nombre del plato
                                nombrePlato.text = plato.nombre

                                // Incrementa el contador
                                contador += 1

                                // Agrega la vista del plato al contenedor del menú
                                itemLayoutContenedor.addView(itemLayoutMenu)

                                // Sale del bucle para evitar agregar más platos
                                break
                            }
                        }

                        // Repite el proceso anterior para platos principales y guarniciones
                        for (plato in listaPlatos) {
                            if (plato.idPlato == carritoUsuario[k].idPlato && plato.tipo == "PlatoPrincipal") {

                                val itemLayoutMenu = inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = plato.descripcion
                                val imgPlato = itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)

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

                                val itemLayoutMenu = inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = plato.descripcion
                                val imgPlato = itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)

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

                                val itemLayoutMenu = inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = extra.descripcion
                                val imgPlato = itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)

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

                                val itemLayoutMenu = inflater.inflate(R.layout.item_menu, container, false)
                                val nombrePlato = itemLayoutMenu.findViewById<TextView>(R.id.NombrePlatoItemMenuCarrito)
                                val txtDescripcion: TextView = itemLayoutMenu.findViewById(R.id.txtDescripcion)
                                txtDescripcion.text = extra.descripcion
                                val imgPlato = itemLayoutMenu.findViewById<ImageView>(R.id.imgPlatoLayoutMenuCarrito)

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

            // Configura el valor máximo para el NumberPicker según el stock mínimo
            numberPicker.maxValue = (stockminimo * 0.1).roundToInt()

            // Realiza cálculos y formatea el precio total del menú
            val df = DecimalFormat("#.##")
            df.roundingMode = java.math.RoundingMode.CEILING
            sumarPreciosMenu *= 0.9
            txtPrecio.text = "${df.format(sumarPreciosMenu)}€"

            // Si se han agregado 5 elementos al menú, agrega la vista del menú al LinearLayout
            if (contador == 5) {
                precioTotal += sumarPreciosMenu
                linearLayout.addView(itemLayout)
            }

        }
        calcularTotal(view)// Llama a una función para calcular el precio total general
    }

    private fun conprobarnumeroSumar(int: Int, plato: Plato): Boolean {
        var devolver = true
        if((plato.stock * 0.1).roundToInt() == int){
            devolver = false
        }
        return devolver
    }
    private fun conprobarnumeroRestar(int: Int, plato: Plato): Boolean {
        var devolver = true
        if(1 == int){
            devolver = false
        }
        return devolver
    }
    private fun conprobarnumeroSumar(int: Int, extra: Extra): Boolean {
        var devolver = true
        if((extra.stock * 0.1).roundToInt() == int){
            devolver = false
        }
        return devolver
    }
    private fun conprobarnumeroRestar(int: Int, extra: Extra): Boolean {
        var devolver = true
        if(1 == int){
            devolver = false
        }
        return devolver
    }

    private fun calcularTotal(view: View) {
        // Busca y obtiene una referencia al TextView que muestra el precio total en la vista
        var precioTotalCarrito = view.findViewById<TextView>(R.id.precioTotalCarrito)

        // Crea un formato decimal con dos decimales
        val df = DecimalFormat("#.##")
        df.roundingMode = java.math.RoundingMode.CEILING

        // Formatea el precioTotal (variable global) con el formato decimal y lo asigna al TextView
        precioTotalCarrito.text = "${df.format(precioTotal)}€"
    }



    private fun cargarImagenFirebase(imagen: ImageView, nombre: String) {
        // Obtiene una referencia al servicio de Firebase Storage
        val storage = Firebase.storage
        val storageReference = storage.reference

        // Crea una referencia a la imagen en Firebase Storage utilizando el nombre proporcionado
        val imageRef = storageReference.child("${nombre}.jpg")

        // Intenta descargar la URL de la imagen desde Firebase Storage
        imageRef.downloadUrl
            .addOnSuccessListener { uri ->
                // El bloque se ejecuta si la descarga de la URL es exitosa
                // 'uri' contiene la URL de descarga de la imagen

                // Convierte la URL en una cadena de texto
                val imageUrl = uri.toString()

                // Utiliza una biblioteca llamada Glide para cargar la imagen desde la URL en el ImageView proporcionado
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(imagen) // 'imagen' es el ImageView donde se mostrará la imagen
            }
            .addOnFailureListener { exception ->
                // El bloque se ejecuta si la descarga de la URL de la imagen falla
                // Aquí puedes manejar los errores, como si la imagen no se pudo descargar
                Log.e("FirebaseStorage", "Error al descargar la imagen: $exception")
            }
    }



    companion object {
        // Esta es una función estática que se puede llamar en la clase 'Carrito' para crear una nueva instancia del fragmento.
        fun newInstance(
            param1: String,
            param2: String,
            carritoUsuarioRe: ArrayList<Pedido>,
            listaPlatosRe: ArrayList<Plato>,
            listaExtrasRe: ArrayList<Extra>
        ):
                Carrito {
            // Crea una nueva instancia del fragmento 'Carrito' con los parámetros proporcionados.
            val fragment = Carrito(carritoUsuarioRe, listaPlatosRe, listaExtrasRe)

            // Asigna el valor del parámetro 'carritoUsuarioRe' al atributo 'carritoUsuario' de la instancia del fragmento.
            fragment.carritoUsuario = carritoUsuarioRe

            // Crea un objeto Bundle para pasar datos a través de argumentos.
            val args = Bundle()

            // Agrega los valores de los parámetros 'param1' y 'param2' al Bundle.
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)

            // Asigna los argumentos al fragmento.
            fragment.arguments = args

            // Devuelve la instancia del fragmento 'Carrito' configurada con los parámetros.
            return fragment
        }
    }

}