package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class Registro : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordComprobar: EditText
    private lateinit var btnRegistro: Button
    private lateinit var txtLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro)

        auth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.editTextEmailRegistro)
        editTextPassword = findViewById(R.id.editTextPasswordRegistro)
        editTextPasswordComprobar = findViewById(R.id.editTextPasswordRegistroComprobar)
        btnRegistro = findViewById(R.id.btnRegistro)
        txtLogin = findViewById(R.id.textViewRegistroToLogin)

        btnRegistro.setOnClickListener {
            var gmail = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()
            var passwordcomprobar = editTextPasswordComprobar.text.toString()

            if(password == passwordcomprobar){

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(gmail, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, Main::class.java)
                            Toast.makeText(
                                this,
                                getString(R.string.registroCompletado),
                                Toast.LENGTH_LONG
                            ).show()
                            guardarDatosUsuario()
                            startActivity(intent)
                        } else {
                            val exception = task.exception
                            when {

                                exception is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.credenciales_no_validas),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                exception is FirebaseAuthWeakPasswordException -> {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.weak_password),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                exception is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.invalid_email),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                exception is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.emailDuplicado),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.registration_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

            }else{

                Toast.makeText(
                    this,
                    getString(R.string.contraseñas_no_iguales),
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

        txtLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun guardarDatosUsuario() {
        // Obtén el usuario actualmente autenticado
        val user = auth.currentUser
        val uid = user?.uid
        //val email = user?.email
        //val displayName = user?.displayName
        //val partes = displayName!!.split(" ")
        //val nombre: String? = partes[0]
        //val apellidos: String? = partes.subList(1, partes.size).joinToString(" ")

        var gmail = editTextEmail.text.toString()
        // Define los datos que deseas agregar al documento
        val datos = hashMapOf(
            "Nombre" to "",
            "Apellidos" to "",
            "Correo" to "$gmail",
            "Telefono" to "",
            "FechaNacimiento" to "",
            "Direccion" to "",
            // Agrega más campos y valores según sea necesario
        )



        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document("$uid")
            .set(datos)
            .addOnSuccessListener {
                // Los datos se han agregado exitosamente
                // Realiza las acciones necesarias en caso de éxito
                Log.d(
                    "Firestore",
                    "Datos agregados con éxito al documento $uid"
                )
            }
            .addOnFailureListener { e ->
                // Maneja los errores en caso de que ocurra algún problema
                // Puedes obtener información adicional sobre el error a través de 'e'
                Log.e(
                    "Firestore",
                    "Error al agregar datos al documento $uid: $e"
                )
            }
    }

}
