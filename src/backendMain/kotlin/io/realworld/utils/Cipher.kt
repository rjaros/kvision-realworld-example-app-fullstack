package io.realworld.utils

import com.auth0.jwt.algorithms.Algorithm

object Cipher {

    val algorithm: Algorithm = Algorithm.HMAC256("a-secret")

    @Suppress("DEPRECATION")
    fun encrypt(data: String): ByteArray = algorithm.sign(data.toByteArray())

}
