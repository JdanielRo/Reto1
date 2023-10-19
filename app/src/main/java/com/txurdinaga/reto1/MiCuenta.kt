package com.txurdinaga.reto1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MiCuenta : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    //private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

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
        editTextNombre.isEnabled = false
        editTextApellidos.isEnabled = false
        editTextCorreoElectronico.isEnabled = false
        editTextFechaNacimiento.isEnabled = false


        db.collection("Usuarios")
            .document("$uid")
            .get()
            .addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot.exists()) {
                    // El documento existe, obtén los datos
                    val datos = documentSnapshot.data

                    // Ahora puedes acceder a los campos de datos como un mapa
                    val nombre = datos?.get("Nombre") as? String
                    val apellidos = datos?.get("Apellidos") as? String
                    val correo = datos?.get("Correo") as? String
                    val direccion = datos?.get("Direccion") as? String
                    val fechaNacimiento = datos?.get("FechaNacimiento") as? String
                    val telefono = datos?.get("Telefono") as? String

                    // Establece los valores en los EditText
                    editTextNombre.setText(nombre)
                    editTextApellidos.setText(apellidos)
                    editTextCorreoElectronico.setText(correo)
                    editTextDireccion.setText(direccion)
                    editTextFechaNacimiento.setText(fechaNacimiento)
                    editTextTelefono.setText(telefono)

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

        view.findViewById<Button>(R.id.btnGuardarCambios).setOnClickListener(){
            val nuevoNombre = editTextNombre.text.toString()
            val nuevoApellidos = editTextApellidos.text.toString()
            val nuevoCorreo = editTextCorreoElectronico.text.toString()
            val nuevoFechaNacimiento = editTextFechaNacimiento.text.toString()
            val nuevoTelefono = editTextTelefono.text.toString()
            val nuevoDireccion = editTextDireccion.text.toString()

            val nuevosDatos = HashMap<String, Any>()
            nuevosDatos["Nombre"] = nuevoNombre
            nuevosDatos["Apellidos"] = nuevoApellidos
            nuevosDatos["Correo"] = nuevoCorreo
            nuevosDatos["FechaNacimiento"] = nuevoFechaNacimiento
            nuevosDatos["Telefono"] = nuevoTelefono
            nuevosDatos["Direccion"] = nuevoDireccion

            db.collection("Usuarios")
                .document("$uid")
                .update(nuevosDatos)
                .addOnSuccessListener {

                    //Quitar los focus en caso de que se hayan actualizado los datos
                    editTextTelefono.clearFocus()
                    editTextDireccion.clearFocus()

                    // Los datos se actualizaron con éxito
                    Toast.makeText(requireContext(), R.string.datos_actualizados, Toast.LENGTH_SHORT).show()


                }
                .addOnFailureListener { e ->
                    // Maneja el error en caso de que ocurra
                    Log.w("MiCuenta", "Error al actualizar los datos", e)
                    // Puedes registrar el error o mostrar un mensaje de error al usuario, o realizar cualquier acción de manejo de errores necesaria.
                }

        }


        //RESTABLECER CONTRASEÑA
        val restablecerPass = view.findViewById<TextView>(R.id.textViewRestablecerPass)

        restablecerPass.setOnClickListener{

            val intent = Intent(requireContext(), NewPassword::class.java)
            startActivity(intent)

        }


        //LOG OUT
        mAuth = Firebase.auth
        val buttonLogOut = view.findViewById<Button>(R.id.buttonLogOut)
        buttonLogOut.setOnClickListener{

            mAuth.signOut()

            val irLogin = Intent(requireContext(), SplashScreen::class.java)
            startActivity(irLogin)


        }


        //BORRAR CUENTA
        val buttonDeleteAccount = view.findViewById<Button>(R.id.buttonDeleteAccount)

        buttonDeleteAccount.setOnClickListener{

            val usuario = Firebase.auth.currentUser!!
            usuario.delete()
                ?.addOnSuccessListener {
                    // El usuario se eliminó con éxito
                    Toast.makeText(requireContext(), R.string.cuenta_borrada, Toast.LENGTH_SHORT).show()

                    // Realiza acciones adicionales si es necesario

                    val updates = HashMap<String, Any>()
                    updates["Correo"] = ""

                    db.collection("Usuarios")
                        .document("$uid")
                        .update(updates)
                        .addOnSuccessListener {
                            // El campo se ha borrado con éxito
                            val irLogin = Intent(requireContext(), Login::class.java)
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
        fun newInstance(param1: String, param2: String) =
            MiCuenta().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
