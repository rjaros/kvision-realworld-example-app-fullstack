package io.realworld.layout.users

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.View
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.form.form
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.ButtonType
import pl.treksoft.kvision.html.button
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.fieldset
import pl.treksoft.kvision.html.h1
import pl.treksoft.kvision.html.link
import pl.treksoft.kvision.html.p
import pl.treksoft.kvision.html.ul

fun Container.registerPage(state: ConduitState) {
    div(className = "auth-page") {
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-6 offset-md-3 col-xs-12") {
                    h1("Sign up", className = "text-xs-center")
                    p(className = "text-xs-center") {
                        link("Have an account?", "#${View.LOGIN.url}")
                    }
                    if (!state.registerErrors.isNullOrEmpty()) {
                        ul(state.registerErrors, className = "error-messages")
                    }
                    lateinit var usernameInput: TextInput
                    lateinit var emailInput: TextInput
                    lateinit var passwordInput: TextInput
                    form {
                        fieldset(className = "form-group") {
                            usernameInput =
                                textInput(value = state.registerUserName, className = "form-control form-control-lg") {
                                    placeholder = "Your Name"
                                }
                        }
                        fieldset(className = "form-group") {
                            emailInput =
                                textInput(
                                    TextInputType.EMAIL,
                                    state.registerEmail,
                                    className = "form-control form-control-lg"
                                ) {
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
                            "Sign up",
                            type = ButtonType.SUBMIT,
                            className = "btn-lg pull-xs-right"
                        )
                    }.onEvent {
                        submit = { ev ->
                            ev.preventDefault()
                            ConduitManager.register(usernameInput.value, emailInput.value, passwordInput.value)
                        }
                    }
                }
            }
        }
    }
}
