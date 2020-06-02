package io.realworld.model

import kotlinx.serialization.Serializable
import pl.treksoft.kvision.remote.Id
import pl.treksoft.kvision.remote.PersistenceConstructor
import pl.treksoft.kvision.remote.Table
import pl.treksoft.kvision.remote.Transient

@Serializable
@Table("users")
data class User(
    @Id
    val id: Int? = null,
    val email: String? = null,
    @Transient
    val token: String? = null,
    val username: String? = null,
    val password: String? = null,
    val bio: String? = null,
    val image: String? = null,
    @Transient
    val following: Boolean? = null
) {
    @PersistenceConstructor
    constructor(id: Int?, email: String?, username: String?, password: String?, bio: String?, image: String?) : this(
        id, email, null, username, password, bio, image, null
    )
}
