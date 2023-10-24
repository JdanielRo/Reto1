package com.txurdinaga.reto1

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Locale


class NewPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_password)

        //CAMBIO DE IDIOMA

        val englishButton = findViewById<ImageView>(R.id.imageViewEnglish)
        val spanishButton = findViewById<ImageView>(R.id.imageViewEspañol)
        val euskeraButton = findViewById<ImageView>(R.id.imageViewEuskera)

        englishButton.setOnClickListener {
            // Cambiar el idioma a inglés
            setAppLocale("en")
            recreate() // Reiniciar la actividad para aplicar el cambio de idioma
        }

        spanishButton.setOnClickListener {
            // Cambiar el idioma a español
            setAppLocale("es")
            recreate() // Reiniciar la actividad para aplicar el cambio de idioma
        }

        euskeraButton.setOnClickListener {
            // Cambiar el idioma a euskera
            setAppLocale("eu")
            recreate() // Reiniciar la actividad para aplicar el cambio de idioma
        }

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
                        val intent = Intent(this, SplashScreen::class.java)
                        startActivity(intent)

                    } else {
                        // Ocurrió un error al enviar el enlace
                        val mensaje = getString(R.string.toast_else)
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        findViewById<ImageView>(R.id.imageViewX).setOnClickListener{onBackPressed()}
    }

    //Función para el cambio de idiomas
    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
