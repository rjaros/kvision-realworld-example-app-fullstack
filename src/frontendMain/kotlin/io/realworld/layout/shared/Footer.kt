package io.realworld.layout.shared

import io.realworld.View
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.footer
import pl.treksoft.kvision.html.link
import pl.treksoft.kvision.html.span

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
