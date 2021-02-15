package io.realworld.layout.shared

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.kvision.core.Container
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.nav
import io.kvision.html.ul

fun Container.pagination(state: ConduitState) {
    val limit = state.pageSize
    if (state.articlesCount > limit) {
        nav {
            ul(className = "pagination") {
                val numberOfPages = ((state.articlesCount - 1) / limit) + 1
                for (page in 0 until numberOfPages) {
                    val className = if (page == state.selectedPage) "page-item active" else "page-item"
                    li(className = className) {
                        link("${page + 1}", "", className = "page-link").onClick { e ->
                            e.preventDefault()
                            ConduitManager.selectPage(page)
                        }
                    }
                }
            }
        }
    }
}
