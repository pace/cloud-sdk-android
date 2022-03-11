package cloud.pace.sdk.fueling_app.data.model

sealed class PumpStatus

object PostPay {
    object Free : PumpStatus()
    object InUse : PumpStatus()
    data class ReadyToPay(val pumpResponse: PumpResponse) : PumpStatus()
    object OutOfOrder : PumpStatus()
}

object PreAuth {
    object Locked : PumpStatus()
    object Free : PumpStatus()
    object InUse : PumpStatus()
    data class Done(val transactionId: String) : PumpStatus()
    object InTransaction : PumpStatus()
    object OutOfOrder : PumpStatus()
    data class Canceled(val successful: Boolean) : PumpStatus()
}
