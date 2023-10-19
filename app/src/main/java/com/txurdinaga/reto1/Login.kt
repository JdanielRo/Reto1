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
import java.util.Locale


class Login : AppCompatActivity() {

    /*companion object {
        private const val RC_SIGN_IN = 9001
    }*/

    private lateinit var auth: FirebaseAuth
    private lateinit var btnInicioSesion: Button
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var txtRegistro: TextView
    private var listaPlatos: ArrayList<Plato> = ArrayList()
    private var listaExtras: ArrayList<Extra> = ArrayList()
    private var usuario :Usuario = Usuario()
    private var carritoUsuario: ArrayList<Pedido> = ArrayList()
    private val db = FirebaseFirestore.getInstance()
    //private lateinit var botonRegisterGoogle: Button

    //lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()

        val listaPlatos = intent.getSerializableExtra("platos") as ArrayList<Plato>
        val listaExtras = intent.getSerializableExtra("extras") as ArrayList<Extra>
        Log.d("MiApp", "esta en el login el extra${listaExtras[0].nombre}")
        Log.d("MiApp", "esta en el login el plato ${listaPlatos[0].nombre}")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            obtenerDatosUsuario()
            obtenerPedidosUsuario()
            val intent = Intent(this, Main::class.java)
            intent.putExtra("platos", listaPlatos)
            intent.putExtra("extras", listaExtras)
            intent.putExtra("usuario", usuario)
            intent.putExtra("carrito", carritoUsuario)
            startActivity(intent)
            finish()
        }


        // Asignar vistas a las variables
        btnInicioSesion = findViewById(R.id.btnInicioSesion)
        editTextCorreo = findViewById(R.id.editTextTextEmailAddress)
        editTextContrasena = findViewById(R.id.editTextTextPassword)
        txtRegistro = findViewById(R.id.textViewNoTienesCuenta)
        //editTextCorreo.setText("dani@gmail.com")
        //editTextContraseña.setText("123456")
        //botonRegisterGoogle = findViewById(R.id.signInWithGoogleButton)

        // Inicializar campos con valores de ejemplo (puedes eliminarlos en producción)
        editTextCorreo.setText("2grupotxurdinaga@gmail.com")
        editTextContrasena.setText("Grupo!2!Grupo")

        // Configurar clic en el botón de inicio de sesión
        btnInicioSesion.setOnClickListener {
            inicioSesion()
        }

        // Configurar clic en el campo de contraseña (si es necesario)
        editTextContrasena.setOnClickListener {
            inicioSesion()
        }

        // Configurar clic en el texto de registro
        txtRegistro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // botonRegisterGoogle.setOnClickListener { iniciarSesionGoogle() }


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

        //¿HAS OLVIDADO TU CONTRASEÑA?
        findViewById<TextView>(R.id.textViewPreguntaLogin).setOnClickListener{

            val olvidarContra = Intent(this, NewPassword::class.java)
            startActivity(olvidarContra)

        }


    }

    private fun obtenerPedidosUsuario() {
        val idUsuario = auth.currentUser?.uid
        db.collection("Pedido")
            .whereEqualTo("idUsuario", idUsuario)
            .whereEqualTo("idPedido", null)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val idUsuario = document.getString("idUsuario") ?: ""
                    val idPlato = document.getString("idPlato") ?: ""
                    val cantidad = document.getString("cantidad")?.toIntOrNull() ?: 0
                    val idPedido = document.getString("idPedido")?.toIntOrNull() ?: 0
                    val idMenu =document.getString("idMenu")?.toIntOrNull() ?: 0
                    val carrito = Pedido(idPedido,idUsuario,idMenu,idPlato,cantidad)
                    carritoUsuario.add(carrito)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MiApp", "Error al obtener Usuario: ${e.message}", e)
            }

    }

    private fun obtenerDatosUsuario() {
        val idUsuario = auth.currentUser?.uid
        db.collection("Usuarios")
            .whereEqualTo("idUsuario", "$idUsuario")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]

                    val idUsuario = document.getString("IdUsuario") ?: ""
                    val nombre = document.getString("Nombre") ?: ""
                    val apellido = document.getString("Apellido") ?: ""
                    val correo = document.getString("Correo") ?: ""
                    val telefono = document.getString("Telefono") ?: ""
                    val direccion = document.getString("Direccion") ?: ""
                    val fechaNacimiento = document.getString("FechaNacimiento") ?: ""

                    usuario = Usuario(idUsuario, nombre, apellido, correo, telefono, direccion, fechaNacimiento)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MiApp", "Error al obtener Usuario: ${e.message}", e)
            }

    }

    /*private fun iniciarSesionGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }*/

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }*/

    /*private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    guardarDatosUsuario()
                    if (user == null || user.providerData.none { it.providerId == EmailAuthProvider.PROVIDER_ID }) {
                        startActivity(Intent(this, ConfirmacionCorreoPopUp::class.java))
                    }else{
                    Toast.makeText(this, "Inició sesión como ${user?.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, Main::class.java))
                    finish()

                }
                } else {
                    Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
                }
            }



    }*/

    /*private fun guardarDatosUsuario() {
        // Obtén el usuario actualmente autenticado
        val user = auth.currentUser
        val uid = user?.uid
        //val email = user?.email
        //val displayName = user?.displayName
        //val partes = displayName!!.split(" ")
        //val nombre: String? = partes[0]
        //val apellidos: String? = partes.subList(1, partes.size).joinToString(" ")


        // Define los datos que deseas agregar al documento
        val datos = hashMapOf(
            "Nombre" to "",
            "Apellidos" to "",
            "Correo" to "",
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
    }*/

    private fun inicioSesion() {
        val correo = editTextCorreo.text.toString()
        val contrasenya = editTextContrasena.text.toString()

        if (correo.isNotEmpty() && contrasenya.isNotEmpty()) {
            //val credential = GoogleAuthProvider.getCredential(googleIdToken, null)

            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, contrasenya)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // Inicio de sesión exitoso, redirige al usuario a la pantalla Home.
                        val intent = Intent(this, Main::class.java)

                        intent.putExtra("platos", listaPlatos)
                        intent.putExtra("extras", listaExtras)

                        startActivity(intent)
                    } else {
                        // Fallo el inicio de sesión, muestra un mensaje de error al usuario.
                        val errorMensaje = task.exception?.message ?: "Error al iniciar sesión"
                        Toast.makeText(this, errorMensaje, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Asegúrate de que se ingresen tanto el correo como la contraseña.
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
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
