package io.realworld.layout

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.FeedType
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.ul

fun Container.feedToggle(state: ConduitState) {
    div(className = "feed-toggle") {
        ul(className = "nav nav-pills outline-active") {
            if (state.user != null) {
                li(className = "nav-item") {
                    val className = if (state.feedType == FeedType.USER) "nav-link active" else "nav-link"
                    link("Your Feed", "", className = className).onClick { e ->
                        e.preventDefault()
                        ConduitManager.selectFeed(FeedType.USER)
                    }
                }
            }
            li(className = "nav-item") {
                val className = if (state.feedType == FeedType.GLOBAL) "nav-link active" else "nav-link"
                link("Global Feed", "", className = className).onClick { e ->
                    e.preventDefault()
                    ConduitManager.selectFeed(FeedType.GLOBAL)
                }
            }
            if (state.selectedTag != null) {
                li(className = "nav-item") {
                    link("${state.selectedTag}", "", "ion-pound", className = "nav-link active").onClick { e ->
                        e.preventDefault()
                    }
                }
            }
        }
    }
}
