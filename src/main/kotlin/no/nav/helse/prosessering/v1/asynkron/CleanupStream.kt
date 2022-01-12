package no.nav.helse.prosessering.v1.asynkron

import no.nav.helse.dokument.K9MellomlagringGateway
import no.nav.helse.dokument.K9MellomlagringService
import no.nav.helse.felles.CorrelationId
import no.nav.helse.felles.formaterStatuslogging
import no.nav.helse.felles.tilK9Beskjed
import no.nav.helse.kafka.*
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory

internal class CleanupStream(
    kafkaConfig: KafkaConfig,
    k9MellomlagringService: K9MellomlagringService
) {
    private val stream = ManagedKafkaStreams(
        name = NAME,
        properties = kafkaConfig.stream(NAME),
        topology = topology(k9MellomlagringService),
        unreadyAfterStreamStoppedIn = kafkaConfig.unreadyAfterStreamStoppedIn
    )

    internal val ready = ManagedStreamReady(stream)
    internal val healthy = ManagedStreamHealthy(stream)

    private companion object {
        private const val NAME = "CleanupV1"
        private val logger = LoggerFactory.getLogger("no.nav.$NAME.topology")

        private fun topology(k9MellomlagringService: K9MellomlagringService): Topology {
            val builder = StreamsBuilder()
            val fraCleanup = Topics.CLEANUP
            val tilK9DittnavVarsel = Topics.K9_DITTNAV_VARSEL

            builder
                .stream(fraCleanup.name, fraCleanup.consumed)
                .filter { _, entry -> 1 == entry.metadata.version }
                .mapValues { soknadId, entry ->
                    process(NAME, soknadId, entry) {
                        val cleanupMelding = entry.deserialiserTilCleanup()

                        logger.info(formaterStatuslogging(cleanupMelding.melding.søknadId, "kjører cleanup"))

                        k9MellomlagringService.slettDokumeter(
                            dokumentIdBolks = cleanupMelding.melding.dokumentId,
                            dokumentEier = K9MellomlagringGateway.DokumentEier(cleanupMelding.melding.søker.fødselsnummer),
                            correlationId = CorrelationId(entry.metadata.correlationId)
                        )

                        val k9beskjed = cleanupMelding.tilK9Beskjed()
                        logger.info(formaterStatuslogging(cleanupMelding.melding.søknadId, "sender K9Beskjed videre til k9-dittnav-varsel med eventId ${k9beskjed.eventId}"))
                        k9beskjed.serialiserTilData()
                    }
                }
                .to(tilK9DittnavVarsel.name, tilK9DittnavVarsel.produced)
            return builder.build()
        }
    }

    internal fun stop() = stream.stop(becauseOfError = false)
}