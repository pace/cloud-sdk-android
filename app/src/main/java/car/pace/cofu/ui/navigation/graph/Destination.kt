package car.pace.cofu.ui.navigation.graph

sealed class Destination(val route: String) {

    data object Onboarding : Destination("onboarding")

    data object Home : Destination("home") {
        data object List : Destination("${Home.route}/list")
        data object Detail : Destination("${Home.route}/detail")
    }

    data object Wallet : Destination("wallet") {
        data object List : Destination("${Wallet.route}/list")
        data object Methods : Destination("${Wallet.route}/methods")
        data object Transactions : Destination("${Wallet.route}/transactions")
        data object FuelType : Destination("${Wallet.route}/fuelType")
    }

    data object More : Destination("more") {
        data object List : Destination("${More.route}/list")
        data object Terms : Destination("${More.route}/terms")
        data object Privacy : Destination("${More.route}/privacy")
        data object Contact : Destination("${More.route}/contact")
        data object Imprint : Destination("${More.route}/imprint")
        data object Libraries : Destination("${More.route}/libraries")
    }
}
