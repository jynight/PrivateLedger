package com.xatcn.privateledger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xatcn.privateledger.PrivateLedgerApp
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
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val app = context.applicationContext as PrivateLedgerApp
    val transactionRepo = app.transactionRepository

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
            // 从数据库获取交易列表
            val transactions by transactionRepo.getAllTransactions().collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            TransactionListScreen(
                transactions = transactions.filter { !it.isReversed },
                onNavigateBack = { navController.popBackStack() },
                onEditTransaction = { transaction ->
                    navController.navigate(Screen.TransactionEdit.createRoute(transaction.id))
                },
                onReverseTransaction = { transaction ->
                    scope.launch {
                        transactionRepo.reverseTransaction(transaction.id)
                    }
                },
                onDeleteTransaction = { transaction ->
                    scope.launch {
                        transactionRepo.deleteTransaction(transaction)
                    }
                }
            )
        }

        composable(
            route = Screen.TransactionEdit.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val transactions by transactionRepo.getAllTransactions().collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            // 从列表中找到对应交易
            val transaction = transactions.find { it.id == transactionId }
                ?: Transaction(id = transactionId, amount = 0.0, category = "", type = TransactionType.EXPENSE)

            TransactionEditScreen(
                transaction = transaction,
                onNavigateBack = { navController.popBackStack() },
                onSave = { updatedTransaction ->
                    scope.launch {
                        transactionRepo.updateTransaction(updatedTransaction)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}
