package io.realworld.layout.shared

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.FeedType
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.html.p

fun Container.tags(state: ConduitState) {
    div(className = "sidebar") {
        p("Popular Tags")
        if (state.tagsLoading) {
            div("Loading tags...", className = "post-preview")
        } else if (!state.tags.isNullOrEmpty()) {
            div(className = "tag-list") {
                state.tags.forEach { tag ->
                    link(tag, "", className = "tag-pill tag-default").onClick {
                        it.preventDefault()
                        ConduitManager.selectFeed(FeedType.TAG, selectedTag = tag)
                    }
                }
            }
        } else {
            div("No tags are here... yet.", className = "post-preview")
        }
    }
}
