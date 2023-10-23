package com.txurdinaga.reto1

import java.io.Serializable

class Pedido(
    val idPedido: Int,
    val idUsuario: String,
    val idMenu: Int,
    val idPlato: String,
    val idExtra: String,
    val cantidad: Int
)  : Serializable