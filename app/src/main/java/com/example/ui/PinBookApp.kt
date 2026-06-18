package com.example.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.LocationRepository
import com.example.ui.screens.AddEditScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocationDetailScreen
import com.example.ui.screens.PasteLinkScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class AddEditRoute(val incomingUri: String? = null, val locationId: Int = -1)

@Serializable
object PasteLinkRoute

@Serializable
data class DetailRoute(val locationId: Int)

@Composable
fun PinBookApp(repository: LocationRepository, sharedUrl: String?, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: PinBookViewModel = viewModel(factory = PinBookViewModelFactory(repository))

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                viewModel = viewModel,
                onAddLocation = { navController.navigate(PasteLinkRoute) },
                onLocationClick = { id -> navController.navigate(DetailRoute(locationId = id)) }
            )
            
            LaunchedEffect(sharedUrl) {
                if (sharedUrl != null) {
                    navController.navigate(AddEditRoute(incomingUri = sharedUrl))
                }
            }
        }
        composable<PasteLinkRoute> {
            PasteLinkScreen(
                onNavigateBack = { navController.popBackStack() },
                onParseLink = { uri -> 
                    navController.navigate(AddEditRoute(incomingUri = uri))
                }
            )
        }
        composable<AddEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AddEditRoute>()
            AddEditScreen(
                viewModel = viewModel,
                incomingUri = route.incomingUri,
                locationId = if (route.locationId == -1) null else route.locationId,
                onNavigateBack = { 
                    navController.popBackStack(HomeRoute, inclusive = false)
                }
            )
        }
        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            LocationDetailScreen(
                viewModel = viewModel,
                locationId = route.locationId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(AddEditRoute(locationId = id)) }
            )
        }
    }
}
