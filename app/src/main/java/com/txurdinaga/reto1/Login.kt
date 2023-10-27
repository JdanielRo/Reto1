package com.txurdinaga.reto1

import Extra
import Plato
import Usuario
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale


class Login : AppCompatActivity() {

    // Declaraciones de variables
    private lateinit var auth: FirebaseAuth
    private lateinit var btnInicioSesion: Button
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var txtRegistro: TextView
    private var listaPlatos: ArrayList<Plato> = ArrayList()
    private var listaExtras: ArrayList<Extra> = ArrayList()
    private var usuario: Usuario = Usuario()
    private var carritoUsuario: ArrayList<Pedido> = ArrayList()
    private val db = FirebaseFirestore.getInstance()

    // Función de creación de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de variables y obtención de datos
        auth = FirebaseAuth.getInstance()
        listaPlatos = intent.getSerializableExtra("platos") as ArrayList<Plato>
        listaExtras = intent.getSerializableExtra("extras") as ArrayList<Extra>

        // Verificación de usuario existente
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Carga de datos y redirección a la actividad principal
            setContentView(R.layout.splash_screen)
            GlobalScope.launch(Dispatchers.IO) {
                obtenerDatosUsuario()
                obtenerPedidosUsuario()
                val intent = Intent(this@Login, Main::class.java)
                intent.putExtra("platos", listaPlatos)
                intent.putExtra("extras", listaExtras)
                intent.putExtra("usuario", usuario)
                intent.putExtra("carrito", carritoUsuario)
                startActivity(intent)
                finish()
            }
        } else {
            // Configuración de la vista de inicio de sesión
            setContentView(R.layout.login)

            // Asignación de vistas a variables
            btnInicioSesion = findViewById(R.id.btnInicioSesion)
            editTextCorreo = findViewById(R.id.editTextTextEmailAddress)
            editTextContrasena = findViewById(R.id.editTextTextPassword)
            txtRegistro = findViewById(R.id.textViewNoTienesCuenta)

            // Configuración de los clics en botones y campos de texto
            btnInicioSesion.setOnClickListener { inicioSesion() }
            editTextContrasena.setOnClickListener { inicioSesion() }
            txtRegistro.setOnClickListener { startActivity(Intent(this, Registro::class.java)) }

            // Configuración del cambio de idioma
            val englishButton = findViewById<ImageView>(R.id.imageViewEnglish)
            val spanishButton = findViewById<ImageView>(R.id.imageViewEspañol)
            val euskeraButton = findViewById<ImageView>(R.id.imageViewEuskera)

            englishButton.setOnClickListener {
                setAppLocale("en")
                recreate()
            }

            spanishButton.setOnClickListener {
                setAppLocale("es")
                recreate()
            }

            euskeraButton.setOnClickListener {
                setAppLocale("eu")
                recreate()
            }

            findViewById<TextView>(R.id.textViewPreguntaLogin).setOnClickListener {
                val olvidarContra = Intent(this, NewPassword::class.java)
                startActivity(olvidarContra)
            }
        }
    }

    // Función para obtener los pedidos del usuario
    private suspend fun obtenerPedidosUsuario() {
        val idUsuario = auth.currentUser?.uid
        val result = db.collection("Pedido")
            .whereEqualTo("idUsuario", idUsuario)
            .whereEqualTo("idPedido", 0)
            .get()
            .await()
        for (document in result) {
            val idUsuario = document.getString("idUsuario") ?: ""
            val idPlato = document.getString("idPlato") ?: ""
            val idExtra = document.getString("idExtra") ?: ""
            val cantidad = document.getLong("cantidad")?.toInt() ?: 0
            val idPedido = document.getLong("idPedido")?.toInt() ?: 0
            val idMenu = document.getLong("idMenu")?.toInt() ?: 0
            val carrito = Pedido(idPedido, idUsuario, idMenu, idPlato, idExtra, cantidad)
            carritoUsuario.add(carrito)
        }
    }

    // Función para obtener los datos del usuario
    private suspend fun obtenerDatosUsuario() {
        val idUsuario = auth.currentUser?.uid
        val result = db.collection("Usuarios")
            .whereEqualTo("idUsuario", "$idUsuario")
            .get()
            .await()
        if (!result.isEmpty) {
            val document = result.documents[0]
            val idUsuario = document.getString("IdUsuario") ?: ""
            val nombre = document.getString("Nombre") ?: ""
            val apellido = document.getString("Apellidos") ?: ""
            val correo = document.getString("Correo") ?: ""
            val telefono = document.getString("Telefono") ?: ""
            val direccion = document.getString("Direccion") ?: ""
            val fechaNacimiento = document.getString("FechaNacimiento") ?: ""
            usuario = Usuario(idUsuario, nombre, apellido, correo, telefono, direccion, fechaNacimiento)
        }
    }

    // Función para el inicio de sesión
    private fun inicioSesion() {
        val correo = editTextCorreo.text.toString()
        val contrasenya = editTextContrasena.text.toString()

        if (correo.isNotEmpty() && contrasenya.isNotEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, contrasenya)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch(Dispatchers.IO) {
                            obtenerDatosUsuario()
                            obtenerPedidosUsuario()
                            val intent = Intent(this@Login, Main::class.java)
                            intent.putExtra("platos", listaPlatos)
                            intent.putExtra("extras", listaExtras)
                            intent.putExtra("usuario", usuario)
                            intent.putExtra("carrito", carritoUsuario)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val errorMensaje = task.exception?.message ?: "Error al iniciar sesión"
                        Toast.makeText(this, errorMensaje, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para el cambio de idioma
    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
