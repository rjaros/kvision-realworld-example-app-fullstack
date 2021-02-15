package io.realworld.layout.shared

import io.realworld.View
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.footer
import io.kvision.html.link
import io.kvision.html.span

fun Container.footer() {
    footer {
        div(className = "container") {
            link("conduit", "#${View.HOME.url}", className = "logo-font")
            span(
                "An interactive learning project from <a href=\"https://thinkster.io\">Thinkster</a>. " +
                        "Code &amp; design licensed under MIT.", rich = true, className = "attribution"
            )
        }
    }
}
