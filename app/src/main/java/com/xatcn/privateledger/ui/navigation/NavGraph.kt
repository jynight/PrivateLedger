package com.xatcn.privateledger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xatcn.privateledger.data.model.Transaction
import com.xatcn.privateledger.data.model.TransactionType
import com.xatcn.privateledger.ui.screen.auth.LoginScreen
import com.xatcn.privateledger.ui.screen.auth.RegisterScreen
import com.xatcn.privateledger.ui.screen.home.HomeScreen
import com.xatcn.privateledger.ui.screen.chat.ChatScreen
import com.xatcn.privateledger.ui.screen.stats.StatsScreen
import com.xatcn.privateledger.ui.screen.settings.SettingsScreen
import com.xatcn.privateledger.ui.screen.settings.ModelManagementScreen
import com.xatcn.privateledger.ui.screen.transaction.TransactionListScreen
import com.xatcn.privateledger.ui.screen.transaction.TransactionEditScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object ModelManagement : Screen("model_management")
    object TransactionList : Screen("transaction_list")
    object TransactionEdit : Screen("transaction_edit/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_edit/$transactionId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToModelManagement = { navController.navigate(Screen.ModelManagement.route) }
            )
        }
        
        composable(Screen.ModelManagement.route) {
            ModelManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TransactionList.route) {
            // TODO: 从数据库获取交易列表
            TransactionListScreen(
                transactions = emptyList(),
                onNavigateBack = { navController.popBackStack() },
                onEditTransaction = { transaction ->
                    navController.navigate(Screen.TransactionEdit.createRoute(transaction.id))
                },
                onReverseTransaction = { transaction ->
                    // TODO: 实现冲销逻辑
                },
                onDeleteTransaction = { transaction ->
                    // TODO: 实现删除逻辑
                }
            )
        }
        
        composable(
            route = Screen.TransactionEdit.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            // TODO: 从数据库获取交易详情
            TransactionEditScreen(
                transaction = Transaction(id = transactionId, amount = 0.0, category = "", type = TransactionType.EXPENSE),
                onNavigateBack = { navController.popBackStack() },
                onSave = { updatedTransaction ->
                    // TODO: 保存更新
                    navController.popBackStack()
                }
            )
        }
    }
}
