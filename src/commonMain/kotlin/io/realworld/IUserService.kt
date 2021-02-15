package io.realworld

import io.realworld.model.User
import io.kvision.annotations.KVService

@KVService
interface IUserService {
    suspend fun login(email: String?, password: String?): User
    suspend fun register(username: String?, email: String?, password: String?): User
    suspend fun user(): User
    suspend fun settings(image: String?, username: String?, bio: String?, email: String?, password: String?): User
    suspend fun profile(username: String): User
    suspend fun profileFollow(username: String, follow: Boolean = true): User
}
