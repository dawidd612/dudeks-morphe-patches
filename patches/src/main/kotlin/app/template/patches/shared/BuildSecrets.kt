package app.template.patches.shared

/**
 * Public builds do not embed a shared third-party Maps API credential.
 * Patches using this value keep their existing no-key fallback behavior.
 */
internal object BuildSecrets {
    const val SHARED_MAPS_API_KEY = ""
}
