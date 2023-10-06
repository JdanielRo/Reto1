package com.txurdinaga.reto1


import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale


class Main : AppCompatActivity() {


    lateinit var navigation: BottomNavigationView

    private val onNavMenuListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {

            R.id.itemHome -> {
                supportFragmentManager.commit{
                    replace(R.id.frameContainer, Home())
                    addToBackStack(null)

                }
                return@OnNavigationItemSelectedListener true
            }


            R.id.itemPedidos -> {
                supportFragmentManager.commit {
                    replace(R.id.frameContainer, Pedidos())
                    addToBackStack(null)

                }
                return@OnNavigationItemSelectedListener true
            }


            R.id.itemCarrito -> {
                supportFragmentManager.commit {
                    replace(R.id.frameContainer, Carrito())
                    addToBackStack(null)
                }
                return@OnNavigationItemSelectedListener true
            }

            R.id.itemMiCuenta -> {
                supportFragmentManager.commit{
                    replace(R.id.frameContainer, MiCuenta())
                    addToBackStack(null)

                }
                return@OnNavigationItemSelectedListener true
            }

            // Agrega más casos para otros elementos del menú de navegación si es necesario

        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        navigation = findViewById(R.id.navMenu)
        navigation.setOnNavigationItemSelectedListener(onNavMenuListener)

        supportFragmentManager.commit {
            replace(R.id.frameContainer, Home())
            addToBackStack(null)
        }

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

    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
