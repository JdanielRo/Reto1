package com.txurdinaga.reto1

import java.io.Serializable

class Carro (
    val idCarro: String,
    val idUsuario: String,
    val idPlato: List<String>
): Serializable