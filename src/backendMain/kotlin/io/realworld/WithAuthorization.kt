package io.realworld

import io.realworld.dao.UserDao
import io.realworld.jwt.JwtProvider
import io.realworld.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.server.ServerRequest
import io.kvision.remote.ServiceException

val logger: Logger = LoggerFactory.getLogger(WithAuthorization::class.java)

interface WithAuthorization {
    val jwtProvider: JwtProvider
    val serverRequest: ServerRequest
    val userDao: UserDao

    suspend fun getUser(): User? {
        return serverRequest.headers().firstHeader("Authorization")?.drop(6)?.let { token ->
            try {
                jwtProvider.decodeJWT(token).subject?.let {
                    userDao.findByUsername(it)?.copy(password = null, token = token)
                }
            } catch (e: Exception) {
                logger.error(e.message, e)
                null
            }
        }
    }

    suspend fun <T> withUser(block: suspend (User) -> T): T {
        return getUser()?.let {
            block(it)
        } ?: throw ServiceException("Unauthorized")
    }

    suspend fun <T> withOptionalUser(block: suspend (User?) -> T): T {
        return block(getUser())
    }
}
