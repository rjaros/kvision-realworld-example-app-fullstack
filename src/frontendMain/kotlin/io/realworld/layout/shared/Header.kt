package io.realworld.layout.shared

import io.realworld.ConduitState
import io.realworld.View
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.image
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.nav
import io.kvision.html.ul

fun Container.headerNav(state: ConduitState) {
    nav(className = "navbar navbar-light") {
        div(className = "container") {
            link("conduit", "#${View.HOME.url}", className = "navbar-brand")
            if (!state.appLoading) {
                ul(className = "nav navbar-nav pull-xs-right") {
                    li(className = "nav-item") {
                        link("Home", "#${View.HOME.url}", className = state.homeLinkClassName)
                    }
                    if (state.user == null) {
                        li(className = "nav-item") {
                            link("Sign in", "#${View.LOGIN.url}", className = state.loginLinkClassName)
                        }
                        li(className = "nav-item") {
                            link("Sign up", "#${View.REGISTER.url}", className = state.registerLinkClassName)
                        }
                    } else {
                        li(className = "nav-item") {
                            link(
                                "New Post",
                                "#${View.EDITOR.url}",
                                "ion-compose",
                                separator = "&nbsp;",
                                className = state.editorLinkClassName
                            )
                        }
                        li(className = "nav-item") {
                            link(
                                "Settings",
                                "#${View.SETTINGS.url}",
                                "ion-gear-a",
                                separator = "&nbsp;",
                                className = state.settingsLinkClassName
                            )
                        }
                        if (state.user.username != null) {
                            li(className = "nav-item") {
                                link(
                                    state.user.username,
                                    "#/@${state.user.username}",
                                    labelFirst = false,
                                    className = state.profileLinkClassName
                                ) {
                                    if (!state.user.image.isNullOrBlank()) {
                                        image(state.user.image, state.user.username, className = "user-pic")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
