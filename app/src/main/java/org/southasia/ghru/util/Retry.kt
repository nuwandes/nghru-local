package org.southasia.ghru.util

/**
 * Makes the Call retry on failure
 */
@kotlin.annotation.MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Retry(val value: Int = 3)