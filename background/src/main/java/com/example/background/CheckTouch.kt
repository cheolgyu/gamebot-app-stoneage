package com.example.background

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager


class CheckTouch(val context: Context) {
    val am: AccessibilityManager by lazy {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    fun chk(): Boolean {
        if (!checkAccessibilityPermissions()) {
            Log.d("333333333", "222222222")
            setAccessibilityPermissions()
        } else {
            Log.d("333333333", "------------")
            return true
        }
        return false
    }

    fun isAccessServiceEnabled(context: Context): Boolean {
        val prefString =
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        return prefString.contains(context.getPackageName() + "/com.example.background.service.TouchService")
    }

    fun checkAccessibilityPermissions(): Boolean {
        if (am != null && am.isEnabled() && isAccessServiceEnabled(context)) {
            return true
        }

        return false
    }

    fun setAccessibilityPermissions() {
        val gsDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        gsDialog.setTitle("접근성 권한 설정")
        gsDialog.setMessage("접근성 권한을 필요로 합니다. " +
                "\n 확인을 눌러 설정에 가셔서 "+context.getString(R.string.app_name)+" 을 켜주세요.")
        gsDialog.setPositiveButton("확인",
            DialogInterface.OnClickListener { dialog, which ->
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@OnClickListener
            }).create().show()
    }
}