package io.realworld.layout.articles

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.View
import io.realworld.model.Article
import io.kvision.core.Container
import io.kvision.html.ButtonSize
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.types.toStringF
import io.kvision.utils.px

fun Container.articleMeta(article: Article, state: ConduitState) {
    div(className = "article-meta") {
        val image =
            article.author?.image?.ifBlank { null } ?: "https://static.productionready.io/images/smiley-cyrus.jpg"
        link("", "#/@${article.author?.username}", image = image)
        div(className = "info") {
            link(article.author?.username ?: "", "#/@${article.author?.username}", className = "author")
            val createdAtFormatted = article.createdAt?.toStringF("MMMM D, YYYY")
            span(createdAtFormatted, className = "date")
        }
        if (article.author?.username != state.user?.username) {
            if (article.author?.following == true) {
                button(
                    "Unfollow ${article.author.username}",
                    "ion-plus-round",
                    ButtonStyle.SECONDARY,
                    separator = "&nbsp; "
                ) {
                    size = ButtonSize.SMALL
                    onClick {
                        ConduitManager.toggleProfileFollow(article.author)
                    }
                }
            } else {
                button(
                    "Follow ${article.author?.username}",
                    "ion-plus-round",
                    ButtonStyle.OUTLINESECONDARY,
                    separator = "&nbsp; "
                ) {
                    size = ButtonSize.SMALL
                    onClick {
                        ConduitManager.toggleProfileFollow(article.author!!)
                    }
                }
            }
            if (article.favorited) {
                button("Unfavorite Post ", "ion-heart", separator = "&nbsp; ") {
                    marginLeft = 5.px
                    size = ButtonSize.SMALL
                    span("(${article.favoritesCount})", className = "counter")
                    onClick {
                        ConduitManager.toggleFavoriteArticle(article)
                    }
                }
            } else {
                button("Favorite Post ", "ion-heart", ButtonStyle.OUTLINEPRIMARY, separator = "&nbsp; ") {
                    marginLeft = 5.px
                    size = ButtonSize.SMALL
                    span("(${article.favoritesCount})", className = "counter")
                    onClick {
                        ConduitManager.toggleFavoriteArticle(article)
                    }
                }
            }
        } else {
            link(
                "Edit Article",
                "#${View.EDITOR.url}/${article.slug}",
                "ion-edit",
                className = "btn btn-outline-secondary btn-sm"
            )
            button("Delete Article", "ion-trash-a", ButtonStyle.OUTLINEDANGER) {
                marginLeft = 5.px
                size = ButtonSize.SMALL
            }.onClick {
                ConduitManager.deleteArticle(article.slug!!)
            }
        }
    }
}
