package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.btnInicioSesion)

        loginButton.setOnClickListener(View.OnClickListener {
            val enteredPassword = passwordEditText.text.toString()
            


            if (enteredPassword == correctPassword) {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.putExtra("usuario", correctPassword)
                startActivity(intent)
            } else {
                val incorrectPasswordMessage = getString(R.string.incorrect_password_message)
                Toast.makeText(this, incorrectPasswordMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}