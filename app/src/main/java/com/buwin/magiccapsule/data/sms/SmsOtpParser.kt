package com.buwin.magiccapsule.data.sms

import java.util.regex.Pattern

enum class OtpType(val label: String) {
    VERIFICATION_CODE("Mã xác minh"),
    DELIVERY_CODE("Mã nhận hàng"),
    GENERAL("Mã giao dịch")
}

data class OtpResult(
    val code: String,
    val type: OtpType,
    val sender: String,
    val previewText: String
)

/**
 * Intelligent parser extracting OTP verification and parcel delivery codes from messages.
 */
object SmsOtpParser {

    // Regex for standard 4-8 digit OTPs preceded by keywords (Vietnamese & English)
    private val OTP_PATTERNS = listOf(
        Pattern.compile("(?i)(?:mã|code|otp|xác nhận|xác minh|mật khẩu|passcode)[^0-9a-zA-Z]{1,10}([0-9]{4,8})\\b"),
        Pattern.compile("(?i)\\b([0-9]{4,8})\\b[^0-9a-zA-Z]{1,10}(?:là mã|is your code|để xác minh)"),
        Pattern.compile("(?i)(?:OTP|Code):?\\s*([0-9]{4,8})\\b")
    )

    // Regex for delivery / parcel pickup codes (e.g., SPX, Shopee, Lazada, GHN, Viettel Post)
    private val DELIVERY_PATTERNS = listOf(
        Pattern.compile("(?i)(?:lấy hàng|nhận hàng|bưu tá|giao hàng|kiện hàng)[^0-9a-zA-Z]{1,15}([0-9A-Z]{4,10})\\b"),
        Pattern.compile("(?i)(?:mã nhận|mã lấy)[^0-9a-zA-Z]{1,10}([0-9A-Z]{4,10})\\b")
    )

    fun parse(sender: String, content: String): OtpResult? {
        if (content.isBlank()) return null

        // 1. Try delivery patterns first
        for (pattern in DELIVERY_PATTERNS) {
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                val code = matcher.group(1) ?: continue
                return OtpResult(
                    code = code,
                    type = OtpType.DELIVERY_CODE,
                    sender = sender,
                    previewText = content.take(60)
                )
            }
        }

        // 2. Try OTP patterns
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                val code = matcher.group(1) ?: continue
                return OtpResult(
                    code = code,
                    type = OtpType.VERIFICATION_CODE,
                    sender = sender,
                    previewText = content.take(60)
                )
            }
        }

        return null
    }
}
