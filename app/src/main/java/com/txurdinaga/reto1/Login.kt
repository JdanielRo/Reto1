package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val btnInicioSesion = findViewById<Button>(R.id.btnInicioSesion)
        val editTextCorreo = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val editTextContraseña = findViewById<EditText>(R.id.editTextTextPassword)

        btnInicioSesion.setOnClickListener {
            val correo = editTextCorreo.text.toString()
            val contraseña = editTextContraseña.text.toString()

            if (correo.isNotEmpty() && contraseña.isNotEmpty()) {
                auth.signInWithEmailAndPassword(correo, contraseña)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Inicio de sesión exitoso, redirige al usuario a la pantalla Home.
                            val intent = Intent(this, Home::class.java)
                            startActivity(intent)
                        } else {
                            // Fallo el inicio de sesión, muestra un mensaje de error al usuario.
                            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Asegúrate de que se ingresen tanto el correo como la contraseña.
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

    }
}