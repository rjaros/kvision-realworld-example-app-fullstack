package io.realworld.dao

import io.realworld.model.User
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service

@Service
class UserDao(val databaseClient: DatabaseClient) {

    suspend fun findById(id: Int): User? {
        return databaseClient.select().from(User::class.java).matching(where("id").`is`(id)).fetch()
            .awaitOneOrNull()
    }

    suspend fun findByUsername(username: String): User? {
        return databaseClient.select().from(User::class.java).matching(where("username").`is`(username)).fetch()
            .awaitOneOrNull()
    }

    suspend fun findByEmail(email: String): User? {
        return databaseClient.select().from(User::class.java).matching(where("email").`is`(email)).fetch()
            .awaitOneOrNull()
    }

    suspend fun findByEmailPassword(email: String, encodedPassword: String): User? {
        return databaseClient.select().from(User::class.java)
            .matching(where("email").`is`(email).and("password").`is`(encodedPassword)).fetch()
            .awaitOneOrNull()
    }

    suspend fun createUser(user: User) {
        databaseClient.insert().into(User::class.java).using(user).fetch().rowsUpdated().awaitSingle()
    }

    suspend fun updateUser(user: User) {
        databaseClient.update().table(User::class.java).using(user).fetch().rowsUpdated().awaitSingle()
    }

    suspend fun isFollowed(follower: User, followed: User): Boolean {
        return databaseClient.execute("SELECT * FROM follow_associations WHERE follower_id = :follower AND followed_id = :followed")
            .bind("follower", follower.id!!).bind("followed", followed.id!!).fetch().awaitFirstOrNull() != null
    }

    suspend fun getUserWithFollowingById(id: Int, currentUser: User?): User? {
        val user = findById(id)
        val following = if (user != null && currentUser != null) {
            isFollowed(currentUser, user)
        } else {
            false
        }
        return user?.copy(following = following)
    }

    suspend fun getUserWithFollowingByUsername(username: String, currentUser: User?): User? {
        val user = findByUsername(username)
        val following = if (user != null && currentUser != null) {
            isFollowed(currentUser, user)
        } else {
            false
        }
        return user?.copy(following = following)
    }

    suspend fun profileFollow(username: String, currentUser: User): User? {
        val user = getUserWithFollowingByUsername(username, currentUser)
        return user?.let {
            if (it.following == true) {
                it
            } else {
                databaseClient.insert().into("follow_associations").value("follower_id", currentUser.id!!)
                    .value("followed_id", it.id!!).fetch().rowsUpdated().awaitSingle()
                it.copy(following = true)
            }
        }
    }

    suspend fun profileUnfollow(username: String, currentUser: User): User? {
        val user = getUserWithFollowingByUsername(username, currentUser)
        return user?.let {
            if (it.following == false) {
                it
            } else {
                databaseClient.delete().from("follow_associations").matching(
                    where("follower_id").`is`(currentUser.id!!).and("followed_id").`is`(it.id!!)
                ).fetch().rowsUpdated().awaitSingle()
                it.copy(following = false)
            }
        }
    }

}
