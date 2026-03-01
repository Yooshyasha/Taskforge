package com.yooshyasha.backend.config

import feign.Client
import feign.
import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient as RawOkHttpClient

@Component
class VikunjaFeignConfig(
    private val config: VikunjaConfig
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header("Authorization", config.vikunjaAuthorization())
        }
    }

    @Bean
    fun feignClient(): Client {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val okHttpClient = RawOkHttpClient.Builder()
            .sslSocketFactory(
                sslContext.socketFactory,
                trustAllCerts[0] as X509TrustManager
            )
            .hostnameVerifier { _, _ -> true }

        return
    }
}