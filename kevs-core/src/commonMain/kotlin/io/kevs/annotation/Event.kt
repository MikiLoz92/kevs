package io.kevs.annotation

/**
 * Marks some class as an Event, in order for Kevs to add it to its Events registrar.
 */
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Event
