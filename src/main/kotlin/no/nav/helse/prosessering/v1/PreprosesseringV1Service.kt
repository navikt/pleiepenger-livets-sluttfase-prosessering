package no.nav.helse.prosessering.v1

import no.nav.helse.dokument.DokumentGateway
import no.nav.helse.dokument.DokumentService
import no.nav.helse.felles.CorrelationId
import no.nav.helse.felles.Metadata
import no.nav.helse.prosessering.v1.søknad.MeldingV1
import no.nav.helse.prosessering.v1.søknad.PreprosessertMeldingV1
import org.slf4j.LoggerFactory

internal class PreprosesseringV1Service(
    private val pdfV1Generator: PdfV1Generator,
    private val dokumentService: DokumentService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PreprosesseringV1Service::class.java)
    }

    internal suspend fun preprosesser(
        melding: MeldingV1,
        metadata: Metadata
    ): PreprosessertMeldingV1 {
        val correlationId = CorrelationId(metadata.correlationId)
        val dokumentEier = DokumentGateway.DokumentEier(melding.søker.fødselsnummer)

        logger.trace("Genererer Oppsummerings-PDF av søknaden.")
        val søknadOppsummeringPdf = pdfV1Generator.generateSoknadOppsummeringPdf(melding)
        logger.trace("Generering av Oppsummerings-PDF OK.")

        logger.trace("Mellomlagrer Oppsummerings-PDF.")

        val soknadOppsummeringPdfUrl = dokumentService.lagreSoknadsOppsummeringPdf(
            pdf = søknadOppsummeringPdf,
            correlationId = correlationId,
            dokumentEier = dokumentEier,
            dokumentbeskrivelse = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase"
        )

        logger.trace("Mellomlagring av Oppsummerings-PDF OK")

        logger.trace("Mellomlagrer Oppsummerings-JSON")

        val søknadJsonUrl = dokumentService.lagreSoknadsMelding(
            k9Format = melding.k9Format,
            dokumentEier = dokumentEier,
            correlationId = correlationId
        )

        logger.trace("Mellomlagrer Oppsummerings-JSON OK.")
        val komplettDokumentUrls = mutableListOf(
            listOf(
                soknadOppsummeringPdfUrl,
                søknadJsonUrl
            )
        )

        if (melding.vedleggUrls.isNotEmpty()) {
            logger.info("Legger til ${melding.vedleggUrls.size} vedlegg URL's fra meldingen som dokument.")
            melding.vedleggUrls.forEach { komplettDokumentUrls.add(listOf(it.toURI())) }
        }

        logger.trace("Totalt ${komplettDokumentUrls.size} dokumentbolker.")

        val preprosessertMeldingV1 = PreprosessertMeldingV1(
            melding = melding,
            dokumentUrls = komplettDokumentUrls.toList()
        )

        preprosessertMeldingV1.reportMetrics()
        return preprosessertMeldingV1
    }
}