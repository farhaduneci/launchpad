package com.farhaduneci.launchpad.util

import android.app.Activity
import android.app.ActivityOptions
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.farhaduneci.launchpad.R
import com.farhaduneci.launchpad.admin.LaunchpadAdminReceiver
import com.farhaduneci.launchpad.data.ACTION_LOCK_SCREEN

fun launchMuDirect(context: Context, pkg: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return
    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_NO_ANIMATION or
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    )
    val options = try {
        ActivityOptions.makeCustomAnimation(context, R.anim.stay_still, R.anim.stay_still)
    } catch (_: Exception) {
        ActivityOptions.makeCustomAnimation(context, 0, 0)
    }
    context.startActivity(intent, options.toBundle())
    if (context is Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                context.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.stay_still,
                    R.anim.stay_still
                )
            } catch (_: Exception) {
                @Suppress("DEPRECATION")
                context.overridePendingTransition(0, 0)
            }
        } else {
            @Suppress("DEPRECATION")
            context.overridePendingTransition(0, 0)
        }
    }
}

fun lockScreen(context: Context) {
    val dpm = context.getSystemService(DevicePolicyManager::class.java)
    val admin = ComponentName(context, LaunchpadAdminReceiver::class.java)
    if (dpm.isAdminActive(admin)) {
        dpm.lockNow()
    } else {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Required to lock the screen from Launchpad slots."
            )
        }
        context.startActivity(intent)
    }
}

/** Dispatch a slot value: runs a named action or launches an app package. */
fun handleSlotAction(context: Context, value: String) = when (value) {
    ACTION_LOCK_SCREEN -> lockScreen(context)
    else -> launchMuDirect(context, value)
}

fun expandNotifications(context: Context) {
    try {
        val sbs = context.getSystemService("statusbar")
        val sbm = Class.forName("android.app.StatusBarManager")
        sbm.getMethod("expandNotificationsPanel").invoke(sbs)
    } catch (_: Exception) { }
}
