package org.southasia.ghru.vo

import org.southasia.ghru.vo.Status.*

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class Resource<out T>(val status: Status, val data: T?, val message: Message?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(SUCCESS, data, null)
        }

        fun <T> error(msg: Message, data: T?): Resource<T> {
            return Resource(ERROR, data, msg)
        }
        fun <T> noInternet(): Resource<T> {
            return Resource(NO_INTERNET, null, null)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(LOADING, data, null)
        }

    }
}
