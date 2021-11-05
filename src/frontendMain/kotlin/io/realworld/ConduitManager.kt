package io.realworld

import io.kvision.navigo.Navigo
import io.kvision.redux.createReduxStore
import io.kvision.remote.ServiceException
import io.realworld.helpers.withProgress
import io.realworld.model.Article
import io.realworld.model.User
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.RequestInit

object ConduitManager : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    const val JWT_TOKEN = "jwtToken"

    private val routing = Navigo(null, true, "#")
    val conduitStore = createReduxStore(::conduitReducer, ConduitState())

    val userService = UserService {
        this.authRequest()
    }
    val articleService = ArticleService {
        this.authRequest()
    }

    private fun RequestInit.authRequest(): Unit {
        getJwtToken()?.let {
            this.headers["Authorization"] = "Token $it"
        }
    }

    fun initialize() {
        routing.initialize().resolve()
        if (getJwtToken() != null) {
            withProgress {
                try {
                    val user = userService.user()
                    conduitStore.dispatch(ConduitAction.Login(user))
                    saveJwtToken(user.token!!)
                    afterInitialize(FeedType.USER)
                } catch (e: Exception) {
                    console.log("Invalid JWT Token")
                    deleteJwtToken()
                    afterInitialize(FeedType.GLOBAL)
                }
            }
        } else {
            afterInitialize(FeedType.GLOBAL)
        }
    }

    private fun afterInitialize(feedType: FeedType) {
        conduitStore.dispatch(ConduitAction.AppLoaded)
        if (conduitStore.getState().view == View.HOME) {
            selectFeed(feedType)
            loadTags()
        }
    }

    fun redirect(view: View) {
        routing.navigate(view.url)
    }

    fun loginPage() {
        conduitStore.dispatch(ConduitAction.LoginPage)
    }

    fun login(email: String?, password: String?) {
        withProgress {
            try {
                val user = userService.login(email, password)
                conduitStore.dispatch(ConduitAction.Login(user))
                saveJwtToken(user.token!!)
                routing.navigate(View.HOME.url)
            } catch (e: ServiceException) {
                conduitStore.dispatch(ConduitAction.LoginError(parseErrors(e.message)))
            }
        }
    }

    fun settingsPage() {
        conduitStore.dispatch(ConduitAction.SettingsPage)
    }

    fun settings(image: String?, username: String?, bio: String?, email: String?, password: String?) {
        withProgress {
            try {
                val user = userService.settings(image, username, bio, email, password)
                conduitStore.dispatch(ConduitAction.Login(user))
                saveJwtToken(user.token!!)
                routing.navigate("${View.PROFILE.url}${user.username}")
            } catch (e: Exception) {
                conduitStore.dispatch(ConduitAction.SettingsError(parseErrors(e.message)))
            }
        }
    }

    fun registerPage() {
        conduitStore.dispatch(ConduitAction.RegisterPage)
    }

    fun register(username: String?, email: String?, password: String?) {
        withProgress {
            try {
                val user = userService.register(username, email, password)
                conduitStore.dispatch(ConduitAction.Login(user))
                saveJwtToken(user.token!!)
                routing.navigate(View.HOME.url)
            } catch (e: Exception) {
                conduitStore.dispatch(ConduitAction.RegisterError(username, email, parseErrors(e.message)))
            }
        }
    }

    fun logout() {
        deleteJwtToken()
        conduitStore.dispatch(ConduitAction.Logout)
        routing.navigate(View.HOME.url)
    }

    fun homePage() {
        conduitStore.dispatch(ConduitAction.HomePage)
        val state = conduitStore.getState()
        if (!state.appLoading) {
            if (state.user != null) {
                selectFeed(FeedType.USER)
            } else {
                selectFeed(FeedType.GLOBAL)
            }
            loadTags()
        }
    }

    fun selectFeed(feedType: FeedType, selectedTag: String? = null, profile: User? = null) {
        conduitStore.dispatch(ConduitAction.SelectFeed(feedType, selectedTag, profile))
        loadArticles()
    }

    fun selectPage(page: Int) {
        conduitStore.dispatch(ConduitAction.SelectPage(page))
        loadArticles()
    }

    fun showArticle(slug: String) {
        withProgress {
            try {
                val article = async { articleService.article(slug) }
                val articleComments = async { articleService.articleComments(slug) }
                conduitStore.dispatch(ConduitAction.ShowArticle(article.await()))
                conduitStore.dispatch(ConduitAction.ShowArticleCommets(articleComments.await()))
            } catch (e: Exception) {
                routing.navigate(View.HOME.url)
            }
        }
    }

    fun articleComment(slug: String, comment: String?) {
        withProgress {
            val newComment = articleService.articleComment(slug, comment)
            conduitStore.dispatch(ConduitAction.AddComment(newComment))
        }
    }

    fun articleCommentDelete(slug: String, id: Int) {
        withProgress {
            articleService.articleCommentDelete(slug, id)
            conduitStore.dispatch(ConduitAction.DeleteComment(id))
        }
    }

    fun toggleFavoriteArticle(article: Article) {
        if (conduitStore.getState().user != null) {
            withProgress {
                val articleUpdated = articleService.articleFavorite(article.slug!!, !article.favorited)
                conduitStore.dispatch(ConduitAction.ArticleUpdated(articleUpdated))
            }
        } else {
            routing.navigate(View.LOGIN.url)
        }
    }

    fun showProfile(username: String, favorites: Boolean) {
        val feedType = if (favorites) FeedType.PROFILE_FAVORITED else FeedType.PROFILE
        conduitStore.dispatch(ConduitAction.ProfilePage(feedType))
        withProgress {
            val user = userService.profile(username)
            selectFeed(feedType, null, user)
        }
    }

    fun toggleProfileFollow(user: User) {
        if (conduitStore.getState().user != null) {
            withProgress {
                val changedUser = userService.profileFollow(user.username!!, !user.following!!)
                conduitStore.dispatch(ConduitAction.ProfileFollowChanged(changedUser))
            }
        } else {
            routing.navigate(View.LOGIN.url)
        }
    }

    private fun loadArticles() {
        conduitStore.dispatch(ConduitAction.ArticlesLoading)
        withProgress {
            val state = conduitStore.getState()
            val limit = state.pageSize
            val offset = state.selectedPage * limit
            val articleDto = when (state.feedType) {
                FeedType.USER -> articleService.feed(offset, limit)
                FeedType.GLOBAL -> articleService.articles(null, null, null, offset, limit)
                FeedType.TAG -> articleService.articles(state.selectedTag, null, null, offset, limit)
                FeedType.PROFILE -> articleService.articles(null, state.profile?.username, null, offset, limit)
                FeedType.PROFILE_FAVORITED -> articleService.articles(
                    null,
                    null,
                    state.profile?.username,
                    offset,
                    limit
                )
            }
            conduitStore.dispatch(ConduitAction.ArticlesLoaded(articleDto.articles, articleDto.articlesCount))
        }
    }

    private fun loadTags() {
        conduitStore.dispatch(ConduitAction.TagsLoading)
        withProgress {
            val tags = articleService.tags()
            conduitStore.dispatch(ConduitAction.TagsLoaded(tags))
        }
    }

    fun editorPage(slug: String? = null) {
        if (slug == null) {
            conduitStore.dispatch(ConduitAction.EditorPage(null))
        } else {
            withProgress {
                val article = articleService.article(slug)
                conduitStore.dispatch(ConduitAction.EditorPage(article))
            }
        }
    }

    fun createArticle(title: String?, description: String?, body: String?, tags: String?) {
        withProgress {
            val tagList = tags?.split(" ")?.toList() ?: emptyList()
            try {
                val article = articleService.createArticle(title, description, body, tagList)
                routing.navigate(View.ARTICLE.url + "/" + article.slug)
            } catch (e: Exception) {
                conduitStore.dispatch(
                    ConduitAction.EditorError(
                        Article(
                            title = title,
                            description = description,
                            body = body,
                            tagList = tagList
                        ), parseErrors(e.message)
                    )
                )
            }
        }
    }

    fun updateArticle(slug: String, title: String?, description: String?, body: String?, tags: String?) {
        withProgress {
            val tagList = tags?.split(" ")?.toList() ?: emptyList()
            try {
                val article = articleService.updateArticle(slug, title, description, body, tagList)
                routing.navigate(View.ARTICLE.url + "/" + article.slug)
            } catch (e: Exception) {
                conduitStore.dispatch(
                    ConduitAction.EditorError(
                        conduitStore.getState().editedArticle!!.copy(
                            title = title,
                            description = description,
                            body = body,
                            tagList = tagList
                        ), parseErrors(e.message)
                    )
                )
            }
        }
    }

    fun deleteArticle(slug: String) {
        withProgress {
            articleService.deleteArticle(slug)
            routing.navigate(View.HOME.url)
        }
    }

    fun getJwtToken(): String? {
        return localStorage[JWT_TOKEN]
    }

    private fun saveJwtToken(token: String) {
        localStorage[JWT_TOKEN] = token
    }

    private fun deleteJwtToken() {
        localStorage.removeItem(JWT_TOKEN)
    }

    private fun parseErrors(message: String?): List<String> {
        return message?.split("\n")?.toList() ?: emptyList()
    }
}
