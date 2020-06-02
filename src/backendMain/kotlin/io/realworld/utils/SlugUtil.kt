package io.realworld.utils

import com.github.slugify.Slugify

object SlugUtil {

    private val slugify = Slugify()

    fun slugify(str: String): String {
        return slugify.slugify(str)
    }

}
