package com.habittracker
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.habittracker.ui.screens.HabitTrackerScreen
import com.habittracker.ui.screens.HomeScreen
import com.habittracker.ui.screens.LoginScreen
import com.habittracker.ui.screens.SetupScreen
import com.habittracker.ui.theme.HabitTrackerTheme
import com.habittracker.util.Constants
import com.habittracker.util.SessionManager
import com.habittracker.util.UserPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        UserPreferences.init(applicationContext)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }
            var isLoggedIn by remember { mutableStateOf(false) }
            var isSetupComplete by remember { mutableStateOf(UserPreferences.isSetupComplete) }
            var isAuthLoading by remember { mutableStateOf(true) }
            val gso = remember { GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(Scope(Constants.SCOPE_SPREADSHEETS)).build() }
            val googleSignInClient = remember { GoogleSignIn.getClient(this, gso) }
            val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) { SessionManager.currentUserAccount = account; isLoggedIn = true }
                } catch (e: ApiException) { Log.e("Auth", "Sign in failed code=${e.statusCode}") }
            }
            LaunchedEffect(Unit) {
                val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                if (account != null && GoogleSignIn.hasPermissions(account, Scope(Constants.SCOPE_SPREADSHEETS))) { SessionManager.currentUserAccount = account; isLoggedIn = true }
                isAuthLoading = false
            }
            HabitTrackerTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (isLoggedIn) { 
                        if (isSetupComplete) {
                            HabitTrackerApp(isDarkTheme = isDarkTheme, onToggleTheme = { isDarkTheme = !isDarkTheme }) 
                        } else {
                            SetupScreen(onSetupComplete = { isSetupComplete = true })
                        }
                    } else { 
                        LoginScreen(isLoading = isAuthLoading, onSignInClick = { launcher.launch(googleSignInClient.signInIntent) }) 
                    }
                }
            }
        }
    }
}

@Composable
fun HabitTrackerApp(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController, 
        startDestination = "home",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut() }
    ) {
        composable("home") { HomeScreen(onNavigateToHabits = { navController.navigate("habits") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
        composable("habits") {
            HabitTrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = { navController.navigate("stats") }
            )
        }
        composable("stats") {
            com.habittracker.ui.screens.StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
