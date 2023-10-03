package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var btnInicioSesion: Button
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextContraseña: EditText
    private lateinit var txtRegistro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Asignar vistas a las variables
        btnInicioSesion = findViewById(R.id.btnInicioSesion)
        editTextCorreo = findViewById(R.id.editTextTextEmailAddress)
        editTextContraseña = findViewById(R.id.editTextTextPassword)
        txtRegistro = findViewById(R.id.textView)

        // Inicializar campos con valores de ejemplo (puedes eliminarlos en producción)
        editTextCorreo.setText("diego@gmail.com")
        editTextContraseña.setText("diego@gmail.com")

        // Configurar clic en el botón de inicio de sesión
        btnInicioSesion.setOnClickListener {
            inicioSesion()
        }

        // Configurar clic en el campo de contraseña (si es necesario)
        editTextContraseña.setOnClickListener {
            inicioSesion()
        }

        // Configurar clic en el campo de correo electrónico (si es necesario)
        editTextCorreo.setOnClickListener {
            inicioSesion()
        }

        // Configurar clic en el texto de registro
        txtRegistro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }

    private fun inicioSesion() {
        val correo = editTextCorreo.text.toString()
        val contraseña = editTextContraseña.text.toString()

        if (correo.isNotEmpty() && contraseña.isNotEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Inicio de sesión exitoso, redirige al usuario a la pantalla Home.
                        val intent = Intent(this, Home::class.java)
                        startActivity(intent)
                    } else {
                        // Fallo el inicio de sesión, muestra un mensaje de error al usuario.
                        val errorMensaje = task.exception?.message ?: "Error al iniciar sesión"
                        Toast.makeText(this, errorMensaje, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Asegúrate de que se ingresen tanto el correo como la contraseña.
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
