package com.buwin.magiccapsule.capsule

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.Parcelable
import android.util.Log

/**
 * Low-level bridge synthesizing Honor MagicOS native CapsuleData Parcelables.
 *
 * Honor MagicOS 8/9/10/11 SystemUI (GlobalDynamicCoordinator) enforces that any
 * notification aspiring to show on the Magic Capsule island MUST provide an instance of
 * [com.hihonor.android.app.CapsuleData] attached to [Notification.hnExtras] under key
 * "smart_island_capsule_data". Without this valid Parcelable containing StatusBarData,
 * GlobalDynamicCoordinator logs "capsuleData == null" and aborts capsule presentation.
 */
object HonorCapsuleHelper {
    private const val TAG = "HonorCapsuleHelper"
    private const val KEY_CAPSULE_DATA = "smart_island_capsule_data"
    private const val KEY_BUSINESS_STEP = "is_capsule_business_key_step"

    fun attachHonorCapsuleData(
        context: Context,
        notification: Notification,
        capsuleText: String,
        title: String,
        details: String,
        iconRes: Int
    ) {
        try {
            val capsuleClass = Class.forName("com.hihonor.android.app.CapsuleData")
            val extraDataClass = Class.forName("com.hihonor.android.app.CapsuleData\$ExtraData")
            val statusBarDataClass = Class.forName("com.hihonor.android.app.CapsuleData\$StatusBarData")

            val capsuleData = capsuleClass.getDeclaredConstructor().newInstance()
            val extraData = extraDataClass.getDeclaredConstructor().newInstance()
            val statusBarData = statusBarDataClass.getDeclaredConstructor().newInstance()

            // 1. Configure StatusBarData
            try {
                statusBarDataClass.getMethod("setControlCapsuleState", Boolean::class.javaPrimitiveType)
                    .invoke(statusBarData, true)
            } catch (_: Exception) {}

            try {
                statusBarDataClass.getMethod("setClickOpenActivity", Boolean::class.javaPrimitiveType)
                    .invoke(statusBarData, true)
            } catch (_: Exception) {}

            try {
                statusBarDataClass.getMethod("setSubText", CharSequence::class.java)
                    .invoke(statusBarData, details.ifBlank { title })
            } catch (_: Exception) {}

            try {
                statusBarDataClass.getMethod("setBusinessName", String::class.java)
                    .invoke(statusBarData, title.ifBlank { capsuleText })
            } catch (_: Exception) {}

            try {
                statusBarDataClass.getMethod("setCapsuleType", Int::class.javaPrimitiveType)
                    .invoke(statusBarData, 1)
            } catch (_: Exception) {}

            try {
                statusBarDataClass.getMethod("setDynamicCardShowFlag", Int::class.javaPrimitiveType)
                    .invoke(statusBarData, 1)
            } catch (_: Exception) {}

            try {
                statusBarDataClass.getMethod("setTextChangeNeedAnim", Boolean::class.javaPrimitiveType)
                    .invoke(statusBarData, true)
            } catch (_: Exception) {}

            // 2. Attach StatusBarData to ExtraData
            try {
                extraDataClass.getMethod("setStatusBarData", statusBarDataClass)
                    .invoke(extraData, statusBarData)
            } catch (_: Exception) {}

            // 3. Configure CapsuleData
            try {
                capsuleClass.getMethod("setStatusBarTemplate", String::class.java)
                    .invoke(capsuleData, "image_text")
            } catch (_: Exception) {}

            try {
                capsuleClass.getMethod("setMainText", CharSequence::class.java)
                    .invoke(capsuleData, capsuleText)
            } catch (_: Exception) {}

            try {
                capsuleClass.getMethod("setMainContent", Array<String>::class.java)
                    .invoke(capsuleData, arrayOf(title, details))
            } catch (_: Exception) {}

            try {
                capsuleClass.getMethod("setSubContent", Array<String>::class.java)
                    .invoke(capsuleData, arrayOf(details.ifBlank { title }))
            } catch (_: Exception) {}

            try {
                capsuleClass.getMethod("setExtraData", extraDataClass)
                    .invoke(capsuleData, extraData)
            } catch (_: Exception) {}

            try {
                val icon = Icon.createWithResource(context, iconRes)
                capsuleClass.getMethod("setFirstIcon", Icon::class.java)
                    .invoke(capsuleData, icon)
            } catch (_: Exception) {}

            // 4. Attach Parcelable to Notification
            val parcelable = capsuleData as? Parcelable
            if (parcelable != null) {
                // Method 4a: Official Honor Notification method
                try {
                    val method = Notification::class.java.getMethod(
                        "setHnExtrasParcelable",
                        String::class.java,
                        Parcelable::class.java
                    )
                    method.invoke(notification, KEY_CAPSULE_DATA, parcelable)
                } catch (_: Exception) {}

                // Method 4b: Direct hnExtras bundle reflection
                try {
                    val field = Notification::class.java.getDeclaredField("hnExtras")
                    field.isAccessible = true
                    var hnBundle = field.get(notification) as? Bundle
                    if (hnBundle == null) {
                        hnBundle = Bundle()
                        field.set(notification, hnBundle)
                    }
                    hnBundle.putParcelable(KEY_CAPSULE_DATA, parcelable)
                    hnBundle.putString(KEY_BUSINESS_STEP, "true")
                    hnBundle.putBoolean("com.hihonor.android.capsule.enable", true)
                    hnBundle.putString("com.hihonor.android.capsule.title", capsuleText)
                    hnBundle.putString("capsule_title", capsuleText)
                    hnBundle.putString("capsule_content", details)
                    hnBundle.putInt("live_notification_mode", 1)
                } catch (_: Exception) {}

                // Method 4c: Standard extras bundle
                notification.extras?.let { extras ->
                    extras.putParcelable(KEY_CAPSULE_DATA, parcelable)
                    extras.putString(KEY_BUSINESS_STEP, "true")
                    extras.putBoolean("com.hihonor.android.capsule.enable", true)
                    extras.putString("com.hihonor.android.capsule.title", capsuleText)
                }
            }

            // 5. Set key step and protocol keys via Honor Notification setters
            try {
                val setStr = Notification::class.java.getMethod(
                    "setHnExtrasValue",
                    String::class.java,
                    String::class.java
                )
                setStr.invoke(notification, KEY_BUSINESS_STEP, "true")
                setStr.invoke(notification, "com.hihonor.android.capsule.title", capsuleText)
                setStr.invoke(notification, "capsule_title", capsuleText)
                setStr.invoke(notification, "capsule_content", details)
            } catch (_: Exception) {}

            try {
                val setBool = Notification::class.java.getMethod(
                    "setHnExtrasBoolean",
                    String::class.java,
                    Boolean::class.javaPrimitiveType
                )
                setBool.invoke(notification, "com.hihonor.android.capsule.enable", true)
                setBool.invoke(notification, "hw_capsule_enable", true)
                setBool.invoke(notification, "hn_capsule_enable", true)
            } catch (_: Exception) {}

            try {
                val setInt = Notification::class.java.getMethod(
                    "setHnExtrasInt",
                    String::class.java,
                    Int::class.javaPrimitiveType
                )
                setInt.invoke(notification, "hw_capsule_display_mode", 1)
                setInt.invoke(notification, "hn_capsule_display_mode", 1)
                setInt.invoke(notification, "live_notification_mode", 1)
                setInt.invoke(notification, "capsule_type", 1)
            } catch (_: Exception) {}

        } catch (e: Exception) {
            Log.w(TAG, "Honor CapsuleData creation failed: ${e.message}")
        }
    }
}
