package io.realworld.helpers

import io.kvision.require

@Suppress("UnsafeCastFromDynamic")
val marked: (String, dynamic) -> String = require("marked")
