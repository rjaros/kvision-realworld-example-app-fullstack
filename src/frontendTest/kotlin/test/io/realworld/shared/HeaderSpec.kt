package test.io.realworld.shared

import io.kvision.core.getElementJQuery
import io.realworld.ConduitState
import io.realworld.model.User
import io.realworld.layout.shared.headerNav
import io.kvision.jquery.get
import io.kvision.panel.Root
import io.kvision.test.DomSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HeaderSpec : DomSpec {

    @Test
    fun headerForUnauthorizedUserTest() {
        run {
            val root = Root("test")
            root.headerNav(ConduitState(appLoading = false))
            val element = root.getElementJQuery()
            assertNotNull(element, "Generated content should not be empty")
            val links = element.find("a")
            assertEquals(4, links.length, "Content should generate four links")
            val loginLink = links.filter("a[href=\"#/login\"]")[0]
            assertNotNull(loginLink, "Login link should exist")
            val loginText = loginLink.innerText
            assertEquals("Sign in", loginText, "Login link should contain correct text")
            val registerLink = links.filter("a[href=\"#/register\"]")[0]
            assertNotNull(registerLink, "Register link should exist")
            val registerText = registerLink.innerText
            assertEquals("Sign up", registerText, "Register link should contain correct text")
        }
    }

    @Test
    fun headerForAuthorizedUserWithoutImageTest() {
        run {
            val root = Root("test")
            root.headerNav(ConduitState(appLoading = false, user = User(
                email = "test@gmail.com",
                username = "testuser"
            )
            ))
            val element = root.getElementJQuery()
            assertNotNull(element, "Generated content should not be empty")
            val links = element.find("a")
            assertEquals(5, links.length, "Content should generate five links")
            val editorLink = links.filter("a[href=\"#/editor\"]")[0]
            assertNotNull(editorLink, "Editor link should exist")
            val editorText = editorLink.innerHTML
            assertEquals(
                "<i class=\"ion-compose\"></i>&nbsp;New Post",
                editorText,
                "Editor link should contain correct html"
            )
            val settingsLink = links.filter("a[href=\"#/settings\"]")[0]
            assertNotNull(settingsLink, "Settings link should exist")
            val settingsText = settingsLink.innerHTML
            assertEquals(
                "<i class=\"ion-gear-a\"></i>&nbsp;Settings",
                settingsText,
                "Settings link should contain correct html"
            )
            val userLink = links.filter("a[href=\"#/@testuser\"]")[0]
            assertNotNull(userLink, "User link should exist")
            val userText = userLink.innerHTML
            assertEquals("testuser", userText, "User link should contain correct html")
        }
    }

    @Test
    fun headerForAuthorizedUserWithImageTest() {
        run {
            val root = Root("test")
            root.headerNav(
                ConduitState(appLoading = false,
                    user = User(
                        email = "test@gmail.com",
                        username = "testuser",
                        image = "https://google.com"
                    )
                )
            )
            val element = root.getElementJQuery()
            assertNotNull(element, "Generated content should not be empty")
            val links = element.find("a")
            val userLink = links.filter("a[href=\"#/@testuser\"]")[0]
            assertNotNull(userLink, "User link should exist")
            val userText = userLink.innerHTML
            assertEquals(
                "<img class=\"user-pic\" src=\"https://google.com\" alt=\"testuser\">testuser",
                userText,
                "User link should contain correct html"
            )
        }
    }
}
