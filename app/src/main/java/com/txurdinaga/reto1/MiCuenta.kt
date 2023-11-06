package com.txurdinaga.reto1

import Usuario
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MiCuenta(usuarioRe: Usuario) : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    //private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    private var usuario: Usuario = usuarioRe

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
        val view = inflater.inflate(R.layout.fragment_mi_cuenta, container, false)

        // Inicializa Firebase Authentication
        val auth = FirebaseAuth.getInstance()

        // Obtén el usuario actualmente autenticado
        val user = auth.currentUser

        // Declara una variable para almacenar el UID del usuario
        var uid: String? = null

        // Verifica si el usuario está autenticado
        if (user != null) {
            // Obtén el UID del usuario autenticado
            uid = user.uid
        }


        val db = FirebaseFirestore.getInstance()

        val editTextNombre = view.findViewById<EditText>(R.id.editTextNombre)
        val editTextApellidos = view.findViewById<EditText>(R.id.editTextApellidos)
        val editTextCorreoElectronico = view.findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextFechaNacimiento = view.findViewById<EditText>(R.id.editTextDate)
        val editTextTelefono = view.findViewById<EditText>(R.id.editTextPhone)
        val editTextDireccion = view.findViewById<EditText>(R.id.editTextPostalAddress)

        //Para que no deje modificar
        /*editTextNombre.isEnabled = false
        editTextApellidos.isEnabled = false*/
        editTextCorreoElectronico.isEnabled = false
        editTextFechaNacimiento.isEnabled = false


        db.collection("Usuarios")
            .document("$uid")
            .get()
            .addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot.exists()) {
                    // El documento existe, obtén los datos
                    val datos = documentSnapshot.data
                    val direccion = datos?.get("Direccion") as? String
                    // Establece los valores en los EditText
                    editTextNombre.setText(usuario.nombre)
                    editTextApellidos.setText(usuario.apellido)
                    editTextCorreoElectronico.setText(usuario.correo)
                    editTextDireccion.setText(direccion)
                    editTextFechaNacimiento.setText(usuario.fechaNacimiento)
                    editTextTelefono.setText(usuario.telefono)

                    // Haz lo que necesites con los datos
                } else {
                    // El documento no existe
                    // Puedes mostrar un mensaje o tomar alguna acción apropiada
                    Log.d("MiCuenta", "El documento no existe.")
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores si ocurre alguno al obtener el documento
                // Puedes mostrar un mensaje de error o realizar una acción apropiada
                Log.e("MiCuenta", "Error al obtener el documento: $e")
            }


        //MODIFICAR DATOS

        view.findViewById<Button>(R.id.btnGuardarCambios).setOnClickListener() {

            val nuevoTelefono = editTextTelefono.text.toString()
            val nuevoNombre = editTextNombre.text.toString()
            val nuevoAplellido = editTextApellidos.text.toString()

            val nuevosDatos = HashMap<String, Any>()
            nuevosDatos["Telefono"] = nuevoTelefono
            nuevosDatos["Nombre"] = nuevoNombre
            nuevosDatos["Apellidos"] = nuevoAplellido

            db.collection("Usuarios")
                .document("$uid")
                .update(nuevosDatos)
                .addOnSuccessListener {

                    //Quitar los focus en caso de que se hayan actualizado los datos
                    editTextTelefono.clearFocus()

                    // Los datos se actualizaron con éxito
                    Toast.makeText(
                        requireContext(),
                        R.string.datos_actualizados,
                        Toast.LENGTH_SHORT
                    ).show()


                }
                .addOnFailureListener { e ->
                    // Maneja el error en caso de que ocurra
                    Log.w("MiCuenta", "Error al actualizar los datos", e)
                    // Puedes registrar el error o mostrar un mensaje de error al usuario, o realizar cualquier acción de manejo de errores necesaria.
                }
            val irLogin = Intent(requireContext(), SplashScreen::class.java)
            startActivity(irLogin)
        }


        //****************************DIRECCIONES********************************

        var listaDatos = mutableListOf<String>()

        // Crea un ArrayAdapter para el Spinner
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaDatos)

        view.findViewById<Button>(R.id.buttonAgregarDireccion).setOnClickListener {

            val nuevoDireccion = editTextDireccion.text.toString()

            val documentReference = db.collection("Usuarios").document("$uid")
            val direccionesCollectionRef = documentReference.collection("Direcciones")

            // Obtener los documentos actuales en la colección "Direcciones"
            direccionesCollectionRef.get()
                .addOnSuccessListener { documents ->
                    if (documents.size() < 5) {
                        // Si el número de documentos es menor que 5, puedes agregar uno nuevo
                        val nuevaColeccion = HashMap<String, Any>()
                        nuevaColeccion["direccion"] = nuevoDireccion

                        // Agregar la nueva colección
                        direccionesCollectionRef.add(nuevaColeccion)

                        Toast.makeText(
                            requireContext(),
                            R.string.direccion_agregada,
                            Toast.LENGTH_SHORT
                        ).show()
                        editTextDireccion.setText("")
                        listaDatos.add(nuevoDireccion)
                        adapter.notifyDataSetChanged()


                    } else {
                        // Si ya hay 5 documentos, muestra un mensaje de error o realiza otra acción
                        Toast.makeText(
                            requireContext(),
                            R.string.limite_direcciones,
                            Toast.LENGTH_SHORT
                        ).show()
                        editTextDireccion.setText("")
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar errores si la operación falla
                    Log.e("Error", "Error al obtener documentos: $e")
                }

        }


        val referenciaDocumentoPadre = db.collection("Usuarios").document("$uid")
        val referenciaColeccionHija = referenciaDocumentoPadre.collection("Direcciones")

        var datoSeleccionado: String = ""
        // Recupera los documentos de la colección hija
        referenciaColeccionHija.get()
            .addOnSuccessListener { querySnapshot ->
                // Crea una lista para almacenar los datos

                for (document in querySnapshot.documents) {
                    // Para cada documento en la colección hija, obtén los datos y agrégalos a la lista
                    val dato = document.getString("direccion") // Ajusta esto según tus datos
                    dato?.let {
                        listaDatos.add(it)
                    }
                }



                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Obtén una referencia al Spinner en tu diseño XML
                val spinner = view.findViewById<Spinner>(R.id.spinner)

                // Configura el adaptador en el Spinner
                spinner.adapter = adapter

                // Configura un oyente de selección si deseas realizar acciones al seleccionar un dato
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        datoSeleccionado = listaDatos[position]
                        // Realiza acciones basadas en el dato seleccionado

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Manejar el caso en el que no se ha seleccionado nada
                    }
                }
            }
            .addOnFailureListener { e ->
                // Maneja errores si la operación falla
                Log.e("Error", "Error al obtener datos de la colección hija: $e")
            }


        view.findViewById<Button>(R.id.buttonBorrarDireccion).setOnClickListener {

            referenciaColeccionHija
                .whereEqualTo("direccion", datoSeleccionado)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        // Encuentra el documento que deseas eliminar
                        val referenciaDocumentoAEliminar = document.reference

                        // Elimina el documento
                        referenciaDocumentoAEliminar.delete()
                            .addOnSuccessListener {
                                // Documento eliminado con éxito
                                listaDatos.remove(datoSeleccionado)
                                adapter.notifyDataSetChanged()

                                Log.e("Success", "Se elimino la direccion")
                            }
                            .addOnFailureListener { e ->
                                // Maneja errores si la eliminación falla
                                Log.e("Error", "Error al eliminar direccion: $e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Maneja errores si la consulta falla
                    Log.e("Error", "Error al obtener datos de la colección hija: $e")
                }

        }


        //RESTABLECER CONTRASEÑA
        val restablecerPass = view.findViewById<TextView>(R.id.textViewRestablecerPass)

        restablecerPass.setOnClickListener {

            val intent = Intent(requireContext(), NewPassword::class.java)
            startActivity(intent)

        }


        //LOG OUT
        mAuth = Firebase.auth
        val buttonLogOut = view.findViewById<Button>(R.id.buttonLogOut)
        buttonLogOut.setOnClickListener {

            mAuth.signOut()

            val irLogin = Intent(requireContext(), SplashScreen::class.java)
            startActivity(irLogin)


        }


        //BORRAR CUENTA
        val buttonDeleteAccount = view.findViewById<Button>(R.id.buttonDeleteAccount)

        buttonDeleteAccount.setOnClickListener {

            val usuario = Firebase.auth.currentUser!!
            usuario.delete()
                ?.addOnSuccessListener {
                    // El usuario se eliminó con éxito
                    Toast.makeText(requireContext(), R.string.cuenta_borrada, Toast.LENGTH_SHORT)
                        .show()

                    // Realiza acciones adicionales si es necesario

                    val updates = HashMap<String, Any>()
                    updates["Correo"] = ""

                    db.collection("Usuarios")
                        .document("$uid")
                        .update(updates)
                        .addOnSuccessListener {
                            // El campo se ha borrado con éxito
                            val irLogin = Intent(requireContext(), SplashScreen::class.java)
                            startActivity(irLogin)

                            // Realiza acciones adicionales si es necesario
                        }
                        .addOnFailureListener { e ->
                            // Maneja errores si ocurre alguno al borrar el campo
                            // Puedes mostrar un mensaje de error o realizar cualquier acción de manejo de errores necesaria
                            Log.e("BorrarCampo", "Error al borrar el campo: $e")
                        }


                }
                ?.addOnFailureListener { e ->
                    // Maneja errores si ocurre alguno al eliminar el usuario
                    // Puedes mostrar un mensaje de error o realizar cualquier acción de manejo de errores necesaria
                    Log.e("EliminarUsuario", "Error al eliminar el usuario: $e")
                }


        }



        return view
    }


    /*private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Crea un cliente de inicio de sesión de Google con las opciones configuradas
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }*/


    companion object {
        fun newInstance(
            param1: String,
            param2: String,
            usuarioRe: Usuario)
        : MiCuenta {
            val fragment = MiCuenta(usuarioRe)
            fragment.usuario = usuarioRe
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
