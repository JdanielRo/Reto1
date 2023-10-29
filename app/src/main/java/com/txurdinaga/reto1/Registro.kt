package com.txurdinaga.reto1

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Registro : AppCompatActivity() {

    private lateinit var editTextNombreRegistro: EditText
    private lateinit var editTextApellidosRegistro: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordComprobar: EditText
    private lateinit var editTextDateRegistro: EditText
    private lateinit var btnRegistro: Button
    private lateinit var txtLogin: TextView
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro)

        auth = FirebaseAuth.getInstance()

        editTextNombreRegistro = findViewById(R.id.editTextNombreRegistro)
        editTextApellidosRegistro = findViewById(R.id.editTextApellidosRegistro)
        editTextEmail = findViewById(R.id.editTextEmailRegistro)
        editTextPassword = findViewById(R.id.editTextPasswordRegistro)
        editTextPasswordComprobar = findViewById(R.id.editTextPasswordRegistroComprobar)
        editTextDateRegistro = findViewById(R.id.editTextDateRegistro)
        btnRegistro = findViewById(R.id.btnRegistro)
        txtLogin = findViewById(R.id.textViewRegistroToLogin)

        btnRegistro.setOnClickListener {

            val gmail = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val passwordcomprobar = editTextPasswordComprobar.text.toString()
            val nombre = editTextNombreRegistro.text.toString()
            val apellidos = editTextApellidosRegistro.text.toString()
            val fechaNacimiento = editTextDateRegistro.text.toString()
            //val fechaNacimientoDate = fechaNacimiento.toDate()



            if(nombre.isNotEmpty() && apellidos.isNotEmpty() &&  password.isNotEmpty() &&  passwordcomprobar.isNotEmpty() &&  fechaNacimiento.isNotEmpty() ){

                if(password == passwordcomprobar){

                    //if(calcularEdad(fechaNacimiento)>=18) {


                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(gmail, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {

                                    val irLogin = Intent(this, Login::class.java)

                                    Toast.makeText(
                                        this,
                                        getString(R.string.registroCompletado),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    guardarDatosUsuario()
                                    startActivity(irLogin)

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

                        /*}else{

                            Toast.makeText(this, getString(R.string.mayor_edad), Toast.LENGTH_SHORT).show()
                        }*/

                    }else{

                        Toast.makeText(this, getString(R.string.contraseñas_no_iguales), Toast.LENGTH_SHORT).show()

                    }

                }else{

                    Toast.makeText(this, getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show()
                }



        }

        txtLogin.setOnClickListener {onBackPressed()
        }
    }

    /*// Función para calcular la edad a partir de la fecha de nacimiento
     @RequiresApi(Build.VERSION_CODES.O)
     private fun calcularEdad(fechaNacimiento: String): Int {
        // Define el formato de la fecha como "dd/MM/yyyy"
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Parsea la fecha de nacimiento a un objeto LocalDate
        val fechaNac = LocalDate.parse(fechaNacimiento, formato)

        // Obtiene la fecha actual como LocalDate
        val fechaHoy = LocalDate.now()

        // Calcula la diferencia de años entre la fecha de nacimiento y la fecha actual
        val edad = Period.between(fechaNac, fechaHoy).years

        return edad
    }*/

    override fun onBackPressed() {
        // No realizar ninguna acción
        // Esto deshabilita la funcionalidad del botón de retroceso
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

        var nombre = editTextNombreRegistro.text.toString()
        var apellidos = editTextApellidosRegistro.text.toString()
        var gmail = editTextEmail.text.toString()
        val fechaNacimiento = editTextDateRegistro.text.toString()
        // Define los datos que deseas agregar al documento
        val datos = hashMapOf(
            "Nombre" to "$nombre",
            "Apellidos" to "$apellidos",
            "Correo" to "$gmail",
            "Telefono" to "",
            "FechaNacimiento" to "$fechaNacimiento",
            "idUsuario" to "$uid",
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

        editTextNombreRegistro.setText("Diego")
        editTextApellidosRegistro.setText("Navarro")
        editTextEmail.setText("diegutxo2002@gmail.com")
        editTextPassword.setText("000000")
        editTextPasswordComprobar.setText("000000")
        editTextDateRegistro.setText("11/11/1999")

        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("Usuarios").document("$uid")

        documentReference.collection("Direcciones")

    }

}
