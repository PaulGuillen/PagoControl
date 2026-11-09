package com.example.androidbase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidbase.features.auth.authGraph
import com.example.androidbase.features.home.homeGraph
import com.example.androidbase.navigation.AuthRoute
import com.example.androidbase.navigation.HomeRoute
import com.example.androidbase.platform.EventReporter
import com.example.androidbase.session.SessionState
import org.koin.compose.koinInject

@Composable
fun MainGraph(session: SessionState, darkTheme: Boolean, onSignOut: () -> Unit) {
    if (session == SessionState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val user = (session as? SessionState.SignedIn)?.user
    key(user?.id) {
        val navController = rememberNavController()
        val reporter: EventReporter = koinInject()
        val currentEntry = navController.currentBackStackEntryAsState().value
        LaunchedEffect(currentEntry) {
            if (currentEntry != null) reporter.screen(if (user == null) "auth" else "home")
        }
        NavHost(navController, startDestination = if (user == null) AuthRoute else HomeRoute) {
            if (user == null) authGraph()
            else homeGraph(user, darkTheme, onSignOut)
        }
    }
}
