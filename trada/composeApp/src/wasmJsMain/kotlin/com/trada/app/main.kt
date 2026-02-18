package com.trada.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import com.trada.app.auth.TokenStorage

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val tokenStorage = TokenStorage()

    ComposeViewport(document.body!!) {
        App(tokenStorage = tokenStorage)
    }
}