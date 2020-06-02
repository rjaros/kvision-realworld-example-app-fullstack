package io.realworld.layout.articles

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.model.Article
import io.realworld.model.Comment
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.image
import pl.treksoft.kvision.html.link
import pl.treksoft.kvision.html.p
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.html.tag
import pl.treksoft.kvision.types.toStringF

fun Container.articleComment(state: ConduitState, comment: Comment, article: Article) {
    div(className = "card") {
        div(className = "card-block") {
            p(comment.body, className = "card-text")
        }
        div(className = "card-footer", rich = true) {
            val imageSrc =
                comment.author?.image?.ifBlank { null } ?: "https://static.productionready.io/images/smiley-cyrus.jpg"
            link("", "#/@${comment.author?.username}", className = "comment-author") {
                image(imageSrc, className = "comment-author-img")
            }
            +" &nbsp; "
            link(comment.author?.username ?: "", "#/@${comment.author?.username}", className = "comment-author")
            val createdAtFormatted = comment.createdAt?.toStringF("MMMM D, YYYY")
            span(createdAtFormatted, className = "date-posted")
            if (state.user != null && state.user.username == comment.author?.username) {
                span(className = "mod-options") {
                    tag(TAG.I, className = "ion-trash-a").onClick {
                        ConduitManager.articleCommentDelete(article.slug!!, comment.id!!)
                    }
                }
            }
        }
    }
}
