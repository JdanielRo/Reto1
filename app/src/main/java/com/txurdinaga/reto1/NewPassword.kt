package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class NewPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_password)

        val auth = FirebaseAuth.getInstance()
        val emailID = findViewById<EditText>(R.id.emailContraNueva)

        findViewById<Button>(R.id.buttonConfirmar).setOnClickListener{

            val correo = emailID.text.toString()

            auth.sendPasswordResetEmail(correo)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // El enlace de restablecimiento se ha enviado correctamente
                        val mensaje = getString(R.string.toast_if)
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                        // Redirige al usuario a la pantalla de inicio de sesión (LoginActivity)

                        val mAuth = Firebase.auth
                        mAuth.signOut()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)

                    } else {
                        // Ocurrió un error al enviar el enlace
                        val mensaje = getString(R.string.toast_else)
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
