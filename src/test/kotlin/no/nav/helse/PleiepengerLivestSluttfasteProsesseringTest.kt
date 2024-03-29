package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.stop
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.Søknad
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.KafkaContainer
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class PleiepengerLivestSluttfasteProsesseringTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(PleiepengerLivestSluttfasteProsesseringTest::class.java)

        private val wireMockServer: WireMockServer = WireMockBuilder()
            .withAzureSupport()
            .navnOppslagConfig()
            .build()
            .stubK9MellomlagringHealth()
            .stubJoarkHealth()
            .stubJournalfor()
            .stubLagreDokument()
            .stubSlettDokument()

        private val kafkaContainer = KafkaWrapper.bootstrap()
        private val kafkaTestProducer = kafkaContainer.meldingsProducer()

        private val cleanupConsumer = kafkaContainer.cleanupConsumer()

        private var engine = newEngine(kafkaContainer).apply {
            start(wait = true)
        }

        private fun getConfig(kafkaEnvironment: KafkaContainer?): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)
            return HoconApplicationConfig(mergedConfig)
        }

        private fun newEngine(kafkaEnvironment: KafkaContainer) = TestApplicationEngine(createTestEnvironment {
            config = getConfig(kafkaEnvironment)
        })

        private fun stopEngine() = engine.stop(5, 60, TimeUnit.SECONDS)

        fun restartEngine() {
            logger.info("Restarting engine...")
            stopEngine()
            CollectorRegistry.defaultRegistry.clear()
            engine = newEngine(kafkaContainer)
            engine.start(wait = true)
            logger.info("Engine started.")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            cleanupConsumer.close()
            kafkaTestProducer.close()
            stopEngine()
            kafkaContainer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            val healthEndpoints = listOf("/isready", "/isalive", "/metrics", "/health")

            val responses = healthEndpoints
                .map { endpoint ->
                handleRequest(HttpMethod.Get, endpoint).response.status()
            }

            responses.forEach { statusCode ->
                Assertions.assertEquals(HttpStatusCode.OK, statusCode)
            }
        }
    }

    @Test
    fun `Gylding søknad blir prosessert av journalføringskonsumer`() {
        val søknad = SøknadUtils.gyldigSøknad()

        kafkaTestProducer.leggTilMottak(søknad)
        cleanupConsumer
            .hentCleanupMelding(søknad.søknadId)
            .assertGyldigMelding(søknad.søknadId)
    }

    @Test
    fun `Sende søknad hvor søker har D-nummer`() {
        val dNummerA = "55125314561"
        val søknad = SøknadUtils.gyldigSøknad(søkerFødselsnummer = dNummerA)

        kafkaTestProducer.leggTilMottak(søknad)
        cleanupConsumer
            .hentCleanupMelding(søknad.søknadId)
            .assertGyldigMelding(søknad.søknadId)
    }

    @Test
    fun `En feilprosessert søknad vil bli prosessert etter at tjenesten restartes`() {
        val søknad = SøknadUtils.gyldigSøknad()

        wireMockServer.stubJournalfor(500) // Simulerer feil ved journalføring

        kafkaTestProducer.leggTilMottak(søknad)
        ventPaaAtRetryMekanismeIStreamProsessering()
        readyGir200HealthGir503()

        wireMockServer.stubJournalfor(201) // Simulerer journalføring fungerer igjen
        restartEngine()
        cleanupConsumer
            .hentCleanupMelding(søknad.søknadId, maxWaitInSeconds = 60)
            .assertGyldigMelding(søknad.søknadId)
    }

    private fun ventPaaAtRetryMekanismeIStreamProsessering() = runBlocking { delay(Duration.ofSeconds(30)) }

    private fun readyGir200HealthGir503() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/health") {}.apply {
                    assertEquals(HttpStatusCode.ServiceUnavailable, response.status())
                }
            }
        }
    }

    private fun String.assertGyldigMelding(søknadId: String) {
        val rawJson = JSONObject(this)

        val metadata = assertNotNull(rawJson.getJSONObject("metadata"))
        assertNotNull(metadata.getString("correlationId"))

        val data = assertNotNull(rawJson.getJSONObject("data"))
        assertNotNull(data.getJSONObject("journalførtMelding").getString("journalpostId"))

        val søknad = assertNotNull(data.getJSONObject("melding")).getJSONObject("k9Format")

        assertEquals(søknadId, søknad.getString("søknadId"))

        val rekonstruertSøknad = JsonUtils.fromString(søknad.toString(), Søknad::class.java)

        val rekonstruertSøknadSomString = JsonUtils.toString(rekonstruertSøknad)
        JSONAssert.assertEquals(søknad.toString(), rekonstruertSøknadSomString, true)
    }

}
