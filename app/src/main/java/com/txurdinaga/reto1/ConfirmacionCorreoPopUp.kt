package com.txurdinaga.reto1

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ConfirmacionCorreoPopUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_correo_pop_up)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        val ancho = metrics.widthPixels
        val alto = metrics.heightPixels
        window.setLayout(((ancho*0.85).toInt()),((alto*0.50).toInt()))
        var Enlazar = findViewById<Button>(R.id.enlazar)
        var Cerrar = findViewById<Button>(R.id.cancelar)
        var correo = findViewById<EditText>(R.id.CorreoEnlazar)
        correo.setText(currentUser?.email.toString())
        var contrasenya = findViewById<EditText>(R.id.ContraEnlazar)

        Enlazar.setOnClickListener(){
            val correofor= currentUser?.email.toString()
            val contrasena = contrasenya.text.toString()
            val credential1 = EmailAuthProvider.getCredential(correofor, contrasena)
            currentUser!!.linkWithCredential(credential1)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        Toast.makeText(this, "Cuenta de Google vinculada a ${user?.displayName}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Main::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error al vincular la cuenta de Google", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Main::class.java))
                        finish()
                    }
                }
        }
        Cerrar.setOnClickListener(){
            Toast.makeText(this, "La siguiente vez que se inicie sesion se le volvera a pedir la confirmacion de correo, ya que no lo ha echo ", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Main::class.java))
        }
    }
}