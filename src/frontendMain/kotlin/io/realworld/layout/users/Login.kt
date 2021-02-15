package io.realworld.layout.users

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.View
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.form.text.TextInput
import io.kvision.form.text.TextInputType
import io.kvision.form.text.textInput
import io.kvision.html.ButtonType
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.fieldset
import io.kvision.html.h1
import io.kvision.html.link
import io.kvision.html.p
import io.kvision.html.ul

fun Container.loginPage(state: ConduitState) {
    div(className = "auth-page") {
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-6 offset-md-3 col-xs-12") {
                    h1("Sign in", className = "text-xs-center")
                    p(className = "text-xs-center") {
                        link("Need an account?", "#${View.REGISTER.url}")
                    }
                    if (!state.loginErrors.isNullOrEmpty()) {
                        ul(state.loginErrors, className = "error-messages")
                    }
                    lateinit var emailInput: TextInput
                    lateinit var passwordInput: TextInput
                    form {
                        fieldset(className = "form-group") {
                            emailInput =
                                textInput(type = TextInputType.EMAIL, className = "form-control form-control-lg") {
                                    placeholder = "Email"
                                }
                        }
                        fieldset(className = "form-group") {
                            passwordInput =
                                textInput(TextInputType.PASSWORD, className = "form-control form-control-lg") {
                                    placeholder = "Password"
                                }
                        }
                        button(
                            "Sign in",
                            type = ButtonType.SUBMIT,
                            className = "btn-lg pull-xs-right"
                        )
                    }.onEvent {
                        submit = { ev ->
                            ev.preventDefault()
                            ConduitManager.login(emailInput.value, passwordInput.value)
                        }
                    }
                }
            }
        }
    }
}
