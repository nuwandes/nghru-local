package org.southasia.ghru.di

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Created by kural on 10/27/17.
 */

class StringConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        return if (String::class.java == type) {
            Converter<ResponseBody, String> { value -> value.string() }
        } else null
    }

    override fun requestBodyConverter(
        type: Type?,
        parameterAnnotations: Array<Annotation>?,
        methodAnnotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<*, RequestBody>? {
        return if (String::class.java == type) {
            Converter<String, RequestBody> { value -> RequestBody.create(MEDIA_TYPE, value) }
        } else null

    }

    companion object {
        private val MEDIA_TYPE = MediaType.parse("text/plain")

        fun create(): StringConverterFactory {
            return StringConverterFactory()
        }
    }
}