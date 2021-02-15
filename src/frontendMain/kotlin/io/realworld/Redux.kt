package io.realworld

import io.realworld.model.Article
import io.realworld.model.Comment
import io.realworld.model.User
import io.kvision.redux.RAction

enum class FeedType {
    USER,
    GLOBAL,
    TAG,
    PROFILE,
    PROFILE_FAVORITED
}

data class ConduitState(
    val appLoading: Boolean = true,
    val view: View = View.HOME,
    val user: User? = null,
    val articlesLoading: Boolean = false,
    val articles: List<Article>? = null,
    val articlesCount: Int = 0,
    val article: Article? = null,
    val articleComments: List<Comment>? = null,
    val selectedPage: Int = 0,
    val feedType: FeedType = FeedType.GLOBAL,
    val selectedTag: String? = null,
    val profile: User? = null,
    val tagsLoading: Boolean = false,
    val tags: List<String>? = null,
    val editorErrors: List<String>? = null,
    val editedArticle: Article? = null,
    val loginErrors: List<String>? = null,
    val settingsErrors: List<String>? = null,
    val registerErrors: List<String>? = null,
    val registerUserName: String? = null,
    val registerEmail: String? = null
) {
    val pageSize = when (feedType) {
        FeedType.USER, FeedType.GLOBAL, FeedType.TAG -> 10
        FeedType.PROFILE, FeedType.PROFILE_FAVORITED -> 5
    }

    private fun linkClassName(view: View) = if (this.view == view) "nav-link active" else "nav-link"

    val homeLinkClassName = linkClassName(View.HOME)
    val loginLinkClassName = linkClassName(View.LOGIN)
    val registerLinkClassName = linkClassName(View.REGISTER)
    val editorLinkClassName = linkClassName(View.EDITOR)
    val settingsLinkClassName = linkClassName(View.SETTINGS)
    val profileLinkClassName =
        if (view == View.PROFILE && profile?.username == user?.username) "nav-link active" else "nav-link"
}

sealed class ConduitAction : RAction {
    object AppLoaded : ConduitAction()
    object HomePage : ConduitAction()
    data class SelectFeed(
        val feedType: FeedType,
        val tag: String?,
        val profile: User?
    ) : ConduitAction()

    object ArticlesLoading : ConduitAction()
    data class ArticlesLoaded(val articles: List<Article>, val articlesCount: Int) : ConduitAction()
    data class SelectPage(val selectedPage: Int) : ConduitAction()

    data class ShowArticle(val article: Article) : ConduitAction()
    data class ShowArticleCommets(val articleComments: List<Comment>) : ConduitAction()
    data class ArticleUpdated(val article: Article) : ConduitAction()

    object TagsLoading : ConduitAction()
    data class TagsLoaded(val tags: List<String>) : ConduitAction()

    data class AddComment(val comment: Comment) : ConduitAction()
    data class DeleteComment(val id: Int) : ConduitAction()

    data class ProfilePage(val feedType: FeedType) : ConduitAction()
    data class ProfileFollowChanged(val user: User) : ConduitAction()

    object LoginPage : ConduitAction()
    data class Login(val user: User) : ConduitAction()
    data class LoginError(val errors: List<String>) : ConduitAction()

    object SettingsPage : ConduitAction()
    data class SettingsError(val errors: List<String>) : ConduitAction()

    object RegisterPage : ConduitAction()
    data class RegisterError(val username: String?, val email: String?, val errors: List<String>) : ConduitAction()

    object Logout : ConduitAction()

    data class EditorPage(val article: Article?) : ConduitAction()
    data class EditorError(
        val article: Article,
        val errors: List<String>
    ) : ConduitAction()
}

fun conduitReducer(state: ConduitState, action: ConduitAction): ConduitState = when (action) {
    is ConduitAction.AppLoaded -> {
        state.copy(appLoading = false)
    }
    is ConduitAction.HomePage -> {
        state.copy(view = View.HOME, articles = null)
    }
    is ConduitAction.SelectFeed -> {
        state.copy(
            feedType = action.feedType,
            selectedTag = action.tag,
            profile = action.profile,
            selectedPage = 0
        )
    }
    is ConduitAction.ArticlesLoading -> {
        state.copy(articlesLoading = true)
    }
    is ConduitAction.ArticlesLoaded -> {
        state.copy(articlesLoading = false, articles = action.articles, articlesCount = action.articlesCount)
    }
    is ConduitAction.SelectPage -> {
        state.copy(selectedPage = action.selectedPage)
    }
    is ConduitAction.ShowArticle -> {
        state.copy(view = View.ARTICLE, article = action.article)
    }
    is ConduitAction.ShowArticleCommets -> {
        state.copy(view = View.ARTICLE, articleComments = action.articleComments)
    }
    is ConduitAction.ArticleUpdated -> {
        if (state.view == View.ARTICLE) {
            state.copy(article = action.article)
        } else {
            state.copy(articles = state.articles?.map {
                if (it.slug == action.article.slug) {
                    action.article
                } else {
                    it
                }
            })
        }
    }
    is ConduitAction.TagsLoading -> {
        state.copy(tagsLoading = true)
    }
    is ConduitAction.TagsLoaded -> {
        state.copy(tagsLoading = false, tags = action.tags)
    }
    is ConduitAction.AddComment -> {
        state.copy(articleComments = listOf(action.comment) + (state.articleComments ?: listOf()))
    }
    is ConduitAction.DeleteComment -> {
        state.copy(articleComments = state.articleComments?.filterNot { it.id == action.id })
    }
    is ConduitAction.ProfilePage -> {
        state.copy(view = View.PROFILE, feedType = action.feedType, articles = null)
    }
    is ConduitAction.ProfileFollowChanged -> {
        if (state.view == View.PROFILE) {
            state.copy(profile = action.user)
        } else {
            state.copy(article = state.article?.copy(author = action.user))
        }
    }
    is ConduitAction.LoginPage -> {
        state.copy(view = View.LOGIN, loginErrors = null)
    }
    is ConduitAction.Login -> {
        state.copy(user = action.user)
    }
    is ConduitAction.LoginError -> {
        state.copy(user = null, loginErrors = action.errors)
    }
    is ConduitAction.SettingsPage -> {
        state.copy(view = View.SETTINGS, settingsErrors = null)
    }
    is ConduitAction.SettingsError -> {
        state.copy(settingsErrors = action.errors)
    }
    is ConduitAction.RegisterPage -> {
        state.copy(view = View.REGISTER, registerErrors = null, registerUserName = null, registerEmail = null)
    }
    is ConduitAction.RegisterError -> {
        state.copy(registerErrors = action.errors, registerUserName = action.username, registerEmail = action.email)
    }
    is ConduitAction.Logout -> {
        ConduitState(appLoading = false)
    }
    is ConduitAction.EditorPage -> {
        state.copy(
            view = View.EDITOR,
            editorErrors = null,
            editedArticle = action.article
        )
    }
    is ConduitAction.EditorError -> {
        state.copy(
            editorErrors = action.errors,
            editedArticle = action.article
        )
    }
}
