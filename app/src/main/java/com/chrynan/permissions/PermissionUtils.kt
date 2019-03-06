@file:Suppress("unused")

package com.chrynan.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

fun Context.hasPermission(permission: RuntimePermission): Boolean =
    checkSelfPermission(permission.permissionString) == PackageManager.PERMISSION_GRANTED

fun Context.doesNotHavePermission(permission: RuntimePermission): Boolean =
    checkSelfPermission(permission.permissionString) != PackageManager.PERMISSION_GRANTED

enum class RuntimePermission(val permissionString: String) {

    READ_CALENDAR(Manifest.permission.READ_CALENDAR),
    WRITE_CALENDAR(Manifest.permission.WRITE_CALENDAR),
    CAMERA(Manifest.permission.CAMERA),
    READ_CONTACTS(Manifest.permission.READ_CONTACTS),
    WRITE_CONTACTS(Manifest.permission.WRITE_CALENDAR),
    GET_ACCOUNTS(Manifest.permission.GET_ACCOUNTS),
    ACCESS_FINE_LOCATION(Manifest.permission.ACCESS_FINE_LOCATION),
    ACCESS_COARSE_LOCATION(Manifest.permission.ACCESS_COARSE_LOCATION),
    RECORD_AUDIO(Manifest.permission.RECORD_AUDIO),
    READ_PHONE_STATE(Manifest.permission.READ_PHONE_STATE),
    @RequiresApi(Build.VERSION_CODES.O)
    READ_PHONE_NUMBERS(Manifest.permission.READ_PHONE_NUMBERS),
    CALL_PHONE(Manifest.permission.CALL_PHONE),
    @RequiresApi(Build.VERSION_CODES.O)
    ANSWER_PHONE_CALLS(Manifest.permission.ANSWER_PHONE_CALLS),
    READ_CALL_LOG(Manifest.permission.READ_CALL_LOG),
    WRITE_CALL_LOG(Manifest.permission.WRITE_CALL_LOG),
    ADD_VOICEMAIL(Manifest.permission.ADD_VOICEMAIL),
    USE_SIP(Manifest.permission.USE_SIP),
    PROCESS_OUTGOING_CALLS(Manifest.permission.PROCESS_OUTGOING_CALLS),
    BODY_SENSORS(Manifest.permission.BODY_SENSORS),
    SEND_SMS(Manifest.permission.SEND_SMS),
    RECEIVE_SMS(Manifest.permission.RECEIVE_SMS),
    READ_SMS(Manifest.permission.READ_SMS),
    RECEIVE_WAP_PUSH(Manifest.permission.RECEIVE_WAP_PUSH),
    RECEIVE_MMS(Manifest.permission.RECEIVE_MMS),
    READ_EXTERNAL_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
    WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    companion object {

        fun fromPermissionString(permissionString: String): RuntimePermission? =
            values().firstOrNull { it.permissionString == permissionString }
    }
}

object PermissionInt {

    const val GRANTED = PackageManager.PERMISSION_GRANTED
    const val DENIED = PackageManager.PERMISSION_DENIED
    const val RATIONALE_SUGGESTED = 1
    const val PROHIBITED = 2
}

enum class PermissionState(val permissionInt: Int) {

    PERMISSION_GRANTED(PermissionInt.GRANTED),
    PERMISSION_DENIED(PermissionInt.DENIED),
    RATIONALE_SUGGESTED(PermissionInt.RATIONALE_SUGGESTED),
    PROHIBITED(PermissionInt.PROHIBITED);

    companion object {

        fun fromPermissionInt(permissionInt: Int): PermissionState? =
            values().firstOrNull { it.permissionInt == permissionInt }
    }
}

data class PermissionResult(
    val permission: RuntimePermission,
    val state: PermissionState
)

typealias RequestCode = Int

typealias PermissionRequestCallback = (requestCode: RequestCode, results: List<PermissionResult>) -> Unit

fun Activity.permissionState(permission: RuntimePermission, hasRequestedBefore: Boolean = false): PermissionState {
    val hasPermission = hasPermission(permission)
    val shouldShowRationale = shouldShowRequestPermissionRationale(permission.permissionString)

    return when {
        hasPermission -> PermissionState.PERMISSION_GRANTED
        !hasPermission && !hasRequestedBefore -> PermissionState.PERMISSION_DENIED
        !hasPermission && shouldShowRationale -> PermissionState.RATIONALE_SUGGESTED
        !hasPermission && !shouldShowRationale -> PermissionState.PROHIBITED
        else -> PermissionState.PERMISSION_DENIED
    }
}

fun Activity.isGranted(permission: RuntimePermission, hasRequestedBefore: Boolean = false): Boolean =
    permissionState(
        permission = permission,
        hasRequestedBefore = hasRequestedBefore
    ) == PermissionState.PERMISSION_GRANTED

fun Activity.isDenied(permission: RuntimePermission, hasRequestedBefore: Boolean = false): Boolean =
    permissionState(
        permission = permission,
        hasRequestedBefore = hasRequestedBefore
    ) == PermissionState.PERMISSION_DENIED

fun Activity.isRationaleSuggested(permission: RuntimePermission, hasRequestedBefore: Boolean = false): Boolean =
    permissionState(
        permission = permission,
        hasRequestedBefore = hasRequestedBefore
    ) == PermissionState.RATIONALE_SUGGESTED

fun Activity.isProhibited(permission: RuntimePermission, hasRequestedBefore: Boolean = false): Boolean =
    permissionState(permission = permission, hasRequestedBefore = hasRequestedBefore) == PermissionState.PROHIBITED

object Permissions {

    fun request(activity: Activity, permissions: List<RuntimePermission>, requestCode: RequestCode) {
        activity.requestPermissions(permissions.map { it.permissionString }.toTypedArray(), requestCode)
    }

    fun handle(
        requestCode: RequestCode,
        permissions: Array<out String>,
        grantResults: IntArray,
        callback: PermissionRequestCallback
    ) {
        if (permissions.size == grantResults.size) {
            val results = permissions.mapIndexed { index, s ->
                PermissionResult(
                    permission = RuntimePermission.fromPermissionString(s)!!,
                    state = PermissionState.fromPermissionInt(grantResults[index])!!
                )
            }
            callback(requestCode, results)
        }
    }
}