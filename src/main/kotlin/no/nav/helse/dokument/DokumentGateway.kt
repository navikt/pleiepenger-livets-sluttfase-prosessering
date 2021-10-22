package no.nav.helse.dokument

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpPost
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.felles.CorrelationId
import no.nav.helse.HttpError
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Duration

class DokumentGateway(
    private val accessTokenClient: AccessTokenClient,
    private val lagreDokumentScopes: Set<String>,
    private val sletteDokumentScopes: Set<String>,
    baseUrl : URI
) : HealthCheck {

    private companion object {
        private const val LAGRE_DOKUMENT_OPERATION = "lagre-dokument"
        private const val SLETTE_DOKUMENT_OPERATION = "slette-dokument"
        private val logger: Logger = LoggerFactory.getLogger(DokumentGateway::class.java)
    }

    private val completeUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("v1", "dokument")
    )

    private val objectMapper = configuredObjectMapper()
    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    override suspend fun check(): Result {
        val checkGetLagreDokumentAccessToken = checkGetAccessToken(LAGRE_DOKUMENT_OPERATION, lagreDokumentScopes)
        val checkGetSletteDokumentAccessToken = checkGetAccessToken(SLETTE_DOKUMENT_OPERATION, sletteDokumentScopes)
        val combined = checkGetLagreDokumentAccessToken.result().toMutableMap()
        combined.putAll(checkGetSletteDokumentAccessToken.result())
        combined["name"] = "DokumentGateway"
        return if (checkGetLagreDokumentAccessToken is UnHealthy || checkGetSletteDokumentAccessToken is UnHealthy) UnHealthy(combined)
        else Healthy(combined)
    }

    private fun checkGetAccessToken(
        operation: String,
        scopes: Set<String>
    ) : Result {
        return try {
            accessTokenClient.getAccessToken(scopes)
            Healthy(mapOf(operation to "Henting av access token OK"))
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for henting av dokument", cause)
            UnHealthy(mapOf(operation to "Henting av access token feilet"))
        }
    }


    internal suspend fun lagreDokmenter(
        dokumenter: Set<Dokument>,
        correlationId: CorrelationId
    ) : List<URI> {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(lagreDokumentScopes).asAuthoriationHeader()

        return coroutineScope {
            val deferred = mutableListOf<Deferred<URI>>()
            dokumenter.forEach { dokument: Dokument ->
                deferred.add(async {
                    requestLagreDokument(
                        dokument = dokument,
                        correlationId = correlationId,
                        authorizationHeader = authorizationHeader
                    )
                })
            }
            deferred.awaitAll()
        }
    }

    internal suspend fun slettDokmenter(
        urls: List<URI>,
        dokumentEier: DokumentEier,
        correlationId: CorrelationId
    ) {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(sletteDokumentScopes).asAuthoriationHeader()
        coroutineScope {
            val deferred = mutableListOf<Deferred<Unit>>()
            urls.forEach {
                deferred.add(async {
                    requestSlettDokument(
                        url = it.tilServiceDiscoveryUrl(baseUrl = completeUrl),
                        correlationId = correlationId,
                        dokumentEier = dokumentEier,
                        authorizationHeader = authorizationHeader
                    )
                })
            }
            deferred.awaitAll()
        }
    }

    private suspend fun requestSlettDokument(
        url: URI,
        dokumentEier: DokumentEier,
        correlationId: CorrelationId,
        authorizationHeader: String
    ) {
        val body = objectMapper.writeValueAsBytes(dokumentEier)
        val contentStream = { ByteArrayInputStream(body) }

        val httpRequest = url.toString()
            .httpDelete()
            .body(contentStream)
            .header(
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.XCorrelationId to correlationId.value,
                HttpHeaders.ContentType to "application/json"
            )

        val (request, _, result) = Operation.monitored(
            app = "pleiepenger-livets-sluttfase-prosessering",
            operation = SLETTE_DOKUMENT_OPERATION,
            resultResolver = { 204 == it.second.statusCode }
        ) {
            httpRequest.awaitStringResponseResult()
        }


        result.fold(
            {},
            { error ->
                logger.warn("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                logger.warn("Feil ved sletting av dokument. $error")
            }
        )
    }

    private suspend fun requestLagreDokument(
        dokument: Dokument,
        correlationId: CorrelationId,
        authorizationHeader: String
    ) : URI {

        val body = objectMapper.writeValueAsBytes(dokument)
        val contentStream = { ByteArrayInputStream(body) }

        return Retry.retry(
            operation = LAGRE_DOKUMENT_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0
        ) {
            val (request, response, result) = Operation.monitored(
                app = "pleiepenger-livets-sluttfase-prosessering",
                operation = LAGRE_DOKUMENT_OPERATION,
                resultResolver = { 201 == it.second.statusCode }
            ) {
                completeUrl.toString()
                    .httpPost()
                    .body(contentStream)
                    .header(
                        HttpHeaders.Authorization to authorizationHeader,
                        HttpHeaders.XCorrelationId to correlationId.value,
                        HttpHeaders.ContentType to "application/json"
                    )
                    .awaitStringResponseResult()
            }
            result.fold(
                { URI(response.header(HttpHeaders.Location).first()) },
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw HttpError(response.statusCode, "Feil ved lagring av dokument.")
                }
            )
        }
    }

    private fun configuredObjectMapper() : ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }

    data class Dokument(
        val eier: DokumentEier,
        val content: ByteArray,
        @JsonProperty("content_type") val contentType: String,
        val title: String
    )

    data class DokumentEier(
        @JsonProperty("eiers_fødselsnummer") val eiersFødselsnummer: String
    )

}

fun URI.tilServiceDiscoveryUrl(baseUrl: URI): URI {
    /*
    K9-mellomlagring returnerer direktelenke til dokumentet. Prosessering bruker service-discovery, så må gjøres om.
    Feks i dev blir denne url returnert: https://k9-dokument.nais.preprod.local/v1/dokument/xxx.xxx,
    mens vi ønsker http://k9-dokument/v1/dokument/xxx.xxx
     */
    val idFraUrl = this.path.substringAfterLast("/")
    val nyUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf(idFraUrl)
    )
    return nyUrl
}