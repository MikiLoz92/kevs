package io.kevs.annotation

/**
 * Indicates that this function is an internal API of Kevs and shouldn't be used by consumers of the library other than
 * modules of Kevs itself, or other gutsy adventurers.
 */
@RequiresOptIn(
        level = RequiresOptIn.Level.ERROR,
        message = "This is an internal Kevs API, you shouldn't rely on it."
)
annotation class InternalKevsApi
