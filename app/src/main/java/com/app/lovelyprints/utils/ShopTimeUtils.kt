import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ShopTimeUtils {

    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun isShopOpen(openTime: String, closeTime: String): Boolean {
        return try {
            val now = LocalTime.now()

            val open = LocalTime.parse(openTime, formatter)
            val close = LocalTime.parse(closeTime, formatter)

            if (close.isAfter(open)) {
                // Normal case (09:00:00 - 17:00:00)
                now.isAfter(open) && now.isBefore(close)
            } else {
                // Overnight case (18:00:00 - 02:00:00)
                now.isAfter(open) || now.isBefore(close)
            }
        } catch (e: Exception) {
            false
        }
    }
}
