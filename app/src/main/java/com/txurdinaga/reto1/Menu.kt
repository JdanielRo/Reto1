package com.txurdinaga.reto1

data class Menu(
    val celiaco: Boolean,
    val calorias: Int,
    val precio: Double,
    val cantidad: Int,
    val id_menu: String,
    val tipo_comida: String,
    val platos: ArrayList<Long>
) {

}
