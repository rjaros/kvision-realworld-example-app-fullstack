package io.realworld.layout

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.FeedType
import io.realworld.View
import io.realworld.layout.shared.pagination
import io.kvision.core.Container
import io.kvision.html.*

fun Container.profilePage(state: ConduitState) {
    val profile = state.profile
    if (profile != null) {
        div(className = "profile-page") {
            div(className = "user-info") {
                div(className = "container") {
                    div(className = "row") {
                        div(className = "col-xs-12 col-md-10 offset-md-1") {
                            val imageSrc = profile.image?.ifBlank { null }
                                ?: "https://static.productionready.io/images/smiley-cyrus.jpg"
                            image(imageSrc, className = "user-img")
                            h4(profile.username)
                            p(profile.bio)
                            if (state.user?.username != profile.username) {
                                if (profile.following == true) {
                                    button(
                                        "Unfollow ${profile.username}",
                                        "ion-plus-round",
                                        ButtonStyle.SECONDARY,
                                        separator = "&nbsp; ",
                                        className = "action-btn"
                                    ) {
                                        size = ButtonSize.SMALL
                                        onClick {
                                            ConduitManager.toggleProfileFollow(profile)
                                        }
                                    }
                                } else {
                                    button(
                                        "Follow ${profile.username}",
                                        "ion-plus-round",
                                        ButtonStyle.OUTLINESECONDARY,
                                        separator = "&nbsp; ",
                                        className = "action-btn"
                                    ) {
                                        size = ButtonSize.SMALL
                                        onClick {
                                            ConduitManager.toggleProfileFollow(profile)
                                        }
                                    }
                                }
                            } else {
                                link(
                                    "Edit Profile Settings",
                                    "#/settings",
                                    "ion-gear-a",
                                    separator = "&nbsp; ",
                                    className = "btn btn-sm btn-outline-secondary action-btn"
                                )
                            }
                        }
                    }
                }
            }
            div(className = "container") {
                div(className = "row") {
                    div(className = "col-xs-12 col-md-10 offset-md-1") {
                        div(className = "articles-toggle") {
                            ul(className = "nav nav-pills outline-active") {
                                li(className = "nav-item") {
                                    val className =
                                        if (state.feedType == FeedType.PROFILE) "nav-link active" else "nav-link"
                                    link(
                                        "My Articles",
                                        "#${View.PROFILE.url}${profile.username}",
                                        className = className
                                    )
                                }
                                li(className = "nav-item") {
                                    val className =
                                        if (state.feedType == FeedType.PROFILE_FAVORITED) "nav-link active" else "nav-link"
                                    link(
                                        "Favorited Articles",
                                        "#${View.PROFILE.url}${profile.username}/favorites",
                                        className = className
                                    )
                                }
                            }
                        }
                        if (state.articlesLoading) {
                            div("Loading articles...", className = "article-preview")
                        } else if (!state.articles.isNullOrEmpty()) {
                            state.articles.forEach {
                                articlePreview(it)
                            }
                            pagination(state)
                        } else {
                            div("No articles are here... yet.", className = "article-preview")
                        }
                    }
                }
            }
        }
    }
}
