package com.txurdinaga.reto1

data class Plato(
    val nombre: String,
    val descripcion: String,
    val celiaco: Boolean,
    val calorias: Int,
    val precio: Double,
    val cantidad: Int,
    val id_plato :Int/*,
    val imageUrl: String // Agrega un campo para la URL de la imagen*/
)
