package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class Registro : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordComprobar: EditText
    private lateinit var btnRegistro: Button
    private lateinit var txtLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro)

        editTextEmail = findViewById(R.id.editTextEmailRegistro)
        editTextPassword = findViewById(R.id.editTextPasswordRegistro)
        editTextPasswordComprobar = findViewById(R.id.editTextPasswordRegistroComprobar)
        btnRegistro = findViewById(R.id.btnRegistro)
        txtLogin = findViewById(R.id.textViewRegistroToLogin)

        btnRegistro.setOnClickListener {
            var gmail = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()
            var passwordcomprobar = editTextPasswordComprobar.text.toString()


            FirebaseAuth.getInstance().createUserWithEmailAndPassword(gmail, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, Home::class.java)
                        Toast.makeText(
                            this,
                            getString(R.string.registroCompletado),
                            Toast.LENGTH_LONG
                        ).show()
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
        }

        txtLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

}