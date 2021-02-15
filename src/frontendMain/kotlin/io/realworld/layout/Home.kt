package io.realworld.layout

import io.realworld.ConduitState
import io.realworld.layout.shared.pagination
import io.realworld.layout.shared.tags
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p

fun Container.homePage(state: ConduitState) {
    div(className = "home-page") {
        if (state.user == null) {
            div(className = "banner") {
                div(className = "container") {
                    h1("conduit", className = "logo-font")
                    p("A place to share your knowledge.")
                }
            }
        }
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-9") {
                    feedToggle(state)
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
                div(className = "col-md-3") {
                    tags(state)
                }
            }
        }
    }
}
