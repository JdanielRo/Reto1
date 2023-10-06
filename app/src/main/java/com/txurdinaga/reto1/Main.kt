package com.txurdinaga.reto1


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class Main : AppCompatActivity() {

    lateinit var navigation: BottomNavigationView
    private lateinit var sharedPref: SharedPreferences
    private var currentFragment: Fragment? = null

    private val onNavMenuListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.itemHome -> {
                    showFragment(Home())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.itemPedidos -> {
                    showFragment(Pedidos())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.itemCarrito -> {
                    showFragment(Carrito())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.itemMiCuenta -> {
                    showFragment(MiCuenta())
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

        sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        val savedLanguage = sharedPref.getString("language", "es") ?: "es"
        setAppLocale(savedLanguage)//muestra el idioma guardado

        //Que por defecto la app se abra en home
        if (savedInstanceState == null) {
            showFragment(Home())
        }

        val englishButton = findViewById<ImageView>(R.id.imageViewEnglish)
        val spanishButton = findViewById<ImageView>(R.id.imageViewEspañol)
        val euskeraButton = findViewById<ImageView>(R.id.imageViewEuskera)

        englishButton.setOnClickListener {
            setAndApplyLanguage("en")
        }

        spanishButton.setOnClickListener {
            setAndApplyLanguage("es")
        }

        euskeraButton.setOnClickListener {
            setAndApplyLanguage("eu")
        }
    }

    //Configurar el idioma
    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    //Cambia el idioma de la aplicacion y lo confirma
    private fun setAndApplyLanguage(languageCode: String) {
        sharedPref.edit().putString("language", languageCode).apply()
        setAppLocale(languageCode)
        updateResources(languageCode)
        recreate() // Reiniciar la actividad para aplicar el cambio de idioma
    }

    //Actualizar los recursos string al idioma que selecciones
    private fun updateResources(languageCode: String) {
        val locale = Locale(languageCode)
        val resources: Resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        baseContext.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    //Esta función se utiliza para mostrar un fragmento en la actividad, para que no cambie al cambiar el idioma
    private fun showFragment(fragment: Fragment) {
        if (currentFragment != null) {
            supportFragmentManager.beginTransaction().remove(currentFragment!!).commit()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .addToBackStack(null)
            .commit()
        currentFragment = fragment
    }
}

