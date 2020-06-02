package io.realworld.helpers

import pl.treksoft.kvision.require

@Suppress("UnsafeCastFromDynamic")
val marked: (String, dynamic) -> String = require("marked")
