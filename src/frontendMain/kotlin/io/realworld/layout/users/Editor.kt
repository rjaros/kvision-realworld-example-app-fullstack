package io.realworld.layout.users

import io.realworld.ConduitManager
import io.realworld.ConduitState
import io.realworld.View
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.form.form
import pl.treksoft.kvision.form.text.TextAreaInput
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.textAreaInput
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.ButtonType
import pl.treksoft.kvision.html.button
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.fieldset
import pl.treksoft.kvision.html.ul

fun Container.editorPage(state: ConduitState) {
    val isNewArticle = state.editedArticle?.slug == null
    val isMyArticle =
        state.editedArticle?.author?.username == null || state.editedArticle.author.username == state.user?.username
    div(className = "editor-page") {
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-10 offset-md-1 col-xs-12") {
                    if (isNewArticle || isMyArticle) {
                        if (!state.editorErrors.isNullOrEmpty()) {
                            ul(state.editorErrors, className = "error-messages")
                        }
                        lateinit var titleInput: TextInput
                        lateinit var descriptionInput: TextInput
                        lateinit var bodyInput: TextAreaInput
                        lateinit var tagsInput: TextInput
                        form {
                            fieldset {
                                fieldset(className = "form-group") {
                                    titleInput = textInput(
                                        value = state.editedArticle?.title,
                                        className = "form-control form-control-lg"
                                    ) {
                                        placeholder = "Article Title"
                                    }
                                }
                                fieldset(className = "form-group") {
                                    descriptionInput = textInput(
                                        value = state.editedArticle?.description,
                                        className = "form-control"
                                    ) {
                                        placeholder = "What's this article about?"
                                    }
                                }
                                fieldset(className = "form-group") {
                                    bodyInput = textAreaInput(
                                        value = state.editedArticle?.body,
                                        rows = 8,
                                        className = "form-control"
                                    ) {
                                        placeholder = "Write your article (in markdown)"
                                    }
                                }
                                fieldset(className = "form-group") {
                                    tagsInput =
                                        textInput(
                                            value = state.editedArticle?.tagList?.joinToString(" "),
                                            className = "form-control"
                                        ) {
                                            placeholder = "Enter tags"
                                        }
                                }
                                button(
                                    "Publish Article", type = ButtonType.SUBMIT,
                                    className = "btn-lg pull-xs-right"
                                )
                            }
                        }.onEvent {
                            submit = { ev ->
                                ev.preventDefault()
                                if (isNewArticle) {
                                    ConduitManager.createArticle(
                                        titleInput.value,
                                        descriptionInput.value,
                                        bodyInput.value,
                                        tagsInput.value
                                    )
                                } else {
                                    ConduitManager.updateArticle(
                                        state.editedArticle?.slug!!,
                                        titleInput.value,
                                        descriptionInput.value,
                                        bodyInput.value,
                                        tagsInput.value
                                    )
                                }
                            }
                        }
                    } else {
                        ConduitManager.redirect(View.HOME)
                    }
                }
            }
        }
    }
}
