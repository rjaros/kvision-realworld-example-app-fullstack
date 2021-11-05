package io.realworld

import io.realworld.layout.articles.article
import io.realworld.layout.homePage
import io.realworld.layout.profilePage
import io.realworld.layout.shared.footer
import io.realworld.layout.shared.headerNav
import io.realworld.layout.users.editorPage
import io.realworld.layout.users.loginPage
import io.realworld.layout.users.registerPage
import io.realworld.layout.users.settingsPage
import io.kvision.Application
import io.kvision.html.header
import io.kvision.html.main
import io.kvision.module
import io.kvision.pace.Pace
import io.kvision.pace.PaceOptions
import io.kvision.panel.ContainerType
import io.kvision.panel.root
import io.kvision.require
import io.kvision.routing.Routing
import io.kvision.startApplication
import io.kvision.state.bind

class App : Application() {

    override fun start() {
        Routing.init()
        Pace.init(require("pace-progressbar/themes/green/pace-theme-bounce.css"))
        Pace.setOptions(PaceOptions(manual = true))
        ConduitManager.initialize()
        root("kvapp", containerType = ContainerType.NONE, addRow = false) {
            header().bind(ConduitManager.conduitStore) { state ->
                headerNav(state)
            }
            main().bind(ConduitManager.conduitStore) { state ->
                if (!state.appLoading) {
                    when (state.view) {
                        View.HOME -> {
                            homePage(state)
                        }
                        View.ARTICLE -> {
                            article(state)
                        }
                        View.PROFILE -> {
                            profilePage(state)
                        }
                        View.LOGIN -> {
                            loginPage(state)
                        }
                        View.REGISTER -> {
                            registerPage(state)
                        }
                        View.EDITOR -> {
                            editorPage(state)
                        }
                        View.SETTINGS -> {
                            settingsPage(state)
                        }
                    }
                }
            }
            footer()
        }
    }
}

fun main() {
    startApplication(::App, module.hot)
}
