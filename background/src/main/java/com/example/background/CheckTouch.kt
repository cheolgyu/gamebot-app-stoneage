package com.example.background

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager


class CheckTouch(val context: Context) {

    fun checkAccessibilityPermissions(): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        val list =
            accessibilityManager!!.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT)
        for (i in list.indices) {
            val info = list[i]

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.resolveInfo.serviceInfo.packageName == context.packageName) {
                return true
            }
        }
        return false
    }

    // 접근성 설정화면으로 넘겨주는 부분
    fun setAccessibilityPermissions() {
        val gsDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        gsDialog.setTitle("접근성 권한 설정")
        gsDialog.setMessage("접근성 권한을 필요로 합니다")
        gsDialog.setPositiveButton("확인",
            DialogInterface.OnClickListener { dialog, which -> // 설정화면으로 보내는 부분
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@OnClickListener
            }).create().show()
    }
}