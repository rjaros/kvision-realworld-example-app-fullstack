package io.realworld

import io.realworld.dao.UserDao
import io.realworld.jwt.JwtProvider
import io.realworld.model.User
import io.realworld.utils.Cipher
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import io.kvision.remote.ServiceException
import java.util.*

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
actual class UserService(
    override val userDao: UserDao,
    override val jwtProvider: JwtProvider,
    override val serverRequest: ServerRequest
) : IUserService, WithAuthorization {

    private val base64Encoder = Base64.getEncoder()

    override suspend fun login(email: String?, password: String?): User {
        if (email != null && password != null) {
            val encodedPassword = String(base64Encoder.encode(Cipher.encrypt(password)))
            val user = userDao.findByEmailPassword(email, encodedPassword)
            if (user != null) {
                return user.copy(password = null, token = jwtProvider.createJWT(user))
            } else {
                throw ServiceException("email or password is invalid")
            }
        } else {
            throw ServiceException("email or password is invalid")
        }
    }

    override suspend fun register(username: String?, email: String?, password: String?): User {
        val errorMessages = mutableListOf<String>()
        if (username.isNullOrEmpty()) {
            errorMessages += "username can't be blank"
            errorMessages += "username is too short (minimum is 1 character)"
        }
        if (username?.length ?: 0 > 20) {
            errorMessages += "username is too long (maximum is 20 characters)"
        }
        if (email.isNullOrEmpty()) {
            errorMessages += "email can't be blank"
        }
        if (password.isNullOrEmpty()) {
            errorMessages += "password can't be blank"
        }
        if (username != null) {
            if (userDao.findByUsername(username) != null) {
                errorMessages += "username has already been taken"
            }
        }
        if (email != null) {
            if (userDao.findByEmail(email) != null) {
                errorMessages += "email has already been taken"
            }
        }
        if (errorMessages.isEmpty()) {
            val user = User(username = username, email = email)
            userDao.createUser(user.copy(password = String(base64Encoder.encode(Cipher.encrypt(password!!)))))
            return user.copy(token = jwtProvider.createJWT(user))
        } else {
            throw ServiceException(errorMessages.joinToString("\n"))
        }
    }

    override suspend fun user(): User {
        return withUser { it }
    }

    override suspend fun settings(
        image: String?,
        username: String?,
        bio: String?,
        email: String?,
        password: String?
    ): User {
        return withUser { currentUser ->
            val errorMessages = mutableListOf<String>()
            if (username.isNullOrEmpty()) {
                errorMessages += "username can't be blank"
                errorMessages += "username is too short (minimum is 1 character)"
            }
            if (username?.length ?: 0 > 20) {
                errorMessages += "username is too long (maximum is 20 characters)"
            }
            if (email.isNullOrEmpty()) {
                errorMessages += "email can't be blank"
            }
            if (username != null && username != currentUser.username) {
                if (userDao.findByUsername(username) != null) {
                    errorMessages += "username has already been taken"
                }
            }
            if (email != null && email != currentUser.email) {
                if (userDao.findByEmail(email) != null) {
                    errorMessages += "email has already been taken"
                }
            }
            if (errorMessages.isEmpty()) {
                val oldUser = userDao.findByUsername(currentUser.username!!)!!
                val newPassword = password?.let { String(base64Encoder.encode(Cipher.encrypt(it))) } ?: oldUser.password
                val newUser =
                    oldUser.copy(image = image, username = username, bio = bio, email = email, password = newPassword)
                userDao.updateUser(newUser)
                newUser.copy(password = null, token = jwtProvider.createJWT(newUser))
            } else {
                throw ServiceException(errorMessages.joinToString("\n"))
            }
        }
    }

    override suspend fun profile(username: String): User {
        return withOptionalUser { currentUser ->
            userDao.getUserWithFollowingByUsername(username, currentUser) ?: throw ServiceException("user not found")
        }
    }

    override suspend fun profileFollow(username: String, follow: Boolean): User {
        return withUser { currentUser ->
            if (follow) {
                userDao.profileFollow(username, currentUser)
            } else {
                userDao.profileUnfollow(username, currentUser)
            }
        } ?: throw ServiceException("user not found")
    }
}
