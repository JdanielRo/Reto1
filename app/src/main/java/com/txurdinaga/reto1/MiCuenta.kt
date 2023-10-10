package com.txurdinaga.reto1

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*

A simple [Fragment] subclass.
Use the [Home.newInstance] factory method to
create an instance of this fragment.
*/
class MiCuenta : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        val rootView = inflater.inflate(R.layout.fragment_mi_cuenta, container, false)

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

        val editTextNombre = rootView.findViewById<EditText>(R.id.editTextNombre)
        val editTextApellidos = rootView.findViewById<EditText>(R.id.editTextApellidos)
        val editTextCorreoElectronico = rootView.findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextFechaNacimiento = rootView.findViewById<EditText>(R.id.editTextDate)
        val editTextTelefono = rootView.findViewById<EditText>(R.id.editTextPhone)
        val editTextDireccion = rootView.findViewById<EditText>(R.id.editTextPostalAddress)

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

        // Ahora 'uid' contiene el UID del usuario autenticado (o será nulo si el usuario no está autenticado)

        // Continúa con el resto de tu código, usando 'uid' según tus necesidades

        return rootView
    }


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
