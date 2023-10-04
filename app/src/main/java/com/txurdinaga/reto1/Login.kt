package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class Login : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var btnInicioSesion: Button
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextContraseña: EditText
    private lateinit var txtRegistro: TextView
    private lateinit var botonRegisterGoogle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Main::class.java))
            finish()
        }


        // Asignar vistas a las variables
        btnInicioSesion = findViewById(R.id.btnInicioSesion)
        editTextCorreo = findViewById(R.id.editTextTextEmailAddress)
        editTextContraseña = findViewById(R.id.editTextTextPassword)
        txtRegistro = findViewById(R.id.textView)
        botonRegisterGoogle = findViewById(R.id.signInWithGoogleButton)

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

        // Configurar clic en el texto de registro
        txtRegistro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
        botonRegisterGoogle.setOnClickListener { iniciarSesionGoogle() }
    }

    private fun iniciarSesionGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesión con Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser

            // Si el usuario no está autenticado con correo electrónico,
            // inicia sesión o regístrate con la cuenta de Google.
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {


                        val correo = editTextCorreo.text.toString()
                        val contrasena = editTextContraseña.text.toString()
                        val credential1 = EmailAuthProvider.getCredential(correo, contrasena)
                        currentUser!!.linkWithCredential(credential1)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = task.result?.user
                                    Toast.makeText(this, "Cuenta de Google vinculada a ${user?.displayName}", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, Main::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Error al vincular la cuenta de Google", Toast.LENGTH_SHORT).show()
                                }
                            }



                    val user = auth.currentUser
                    Toast.makeText(this, "Inició sesión como ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Main::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun inicioSesion() {
        val correo = editTextCorreo.text.toString()
        val contraseña = editTextContraseña.text.toString()

        if (correo.isNotEmpty() && contraseña.isNotEmpty()) {
            //val credential = GoogleAuthProvider.getCredential(googleIdToken, null)

            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // Inicio de sesión exitoso, redirige al usuario a la pantalla Home.
                        val intent = Intent(this, Main::class.java)
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
