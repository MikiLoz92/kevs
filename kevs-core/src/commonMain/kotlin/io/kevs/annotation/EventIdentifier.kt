package io.kevs.annotation

/**
 * Same as [Event], but including an event identifier.
 */
@Event
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class EventIdentifier(val id: String)
