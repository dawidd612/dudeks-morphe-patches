package app.template.patches.calendar.widget

import app.morphe.patcher.patch.PatchException
import org.w3c.dom.Element

/** Re-signed fresh installs must not join Google's certificate-bound shared UID. */
internal object ScheduleInstallManifest {
    private const val ANDROID = "http://schemas.android.com/apk/res/android"
    private const val SHARED_UID = "com.google.android.calendar.uid.shared"

    fun prepare(manifest: Element) {
        if (manifest.tagName != "manifest" ||
            manifest.getAttribute("package") != "com.google.android.calendar") {
            throw PatchException("Stabilize schedule widget: unexpected manifest package")
        }
        val sharedUid = manifest.getAttributeNS(ANDROID, "sharedUserId")
        if (sharedUid.isNotEmpty() && sharedUid != SHARED_UID) {
            throw PatchException("Stabilize schedule widget: unexpected shared user ID")
        }
        // This changes the new APK only; it cannot migrate an installed Google
        // package or its data to the Morphe signing key / a different Linux UID.
        listOf("sharedUserId", "sharedUserLabel", "sharedUserMaxSdkVersion").forEach {
            manifest.removeAttributeNS(ANDROID, it)
        }
    }
}
