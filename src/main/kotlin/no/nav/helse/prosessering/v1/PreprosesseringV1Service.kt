package no.nav.helse.prosessering.v1

import no.nav.helse.dokument.K9MellomlagringGateway
import no.nav.helse.dokument.K9MellomlagringService
import no.nav.helse.dokument.Søknadsformat
import no.nav.helse.felles.CorrelationId
import no.nav.helse.felles.Metadata
import no.nav.helse.prosessering.v1.søknad.PreprosessertSøknad
import no.nav.helse.prosessering.v1.søknad.Søknad
import org.slf4j.LoggerFactory

internal class PreprosesseringV1Service(
    private val pdfV1Generator: PdfV1Generator,
    private val k9MellomlagringService: K9MellomlagringService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PreprosesseringV1Service::class.java)
    }

    internal suspend fun preprosesser(
        søknad: Søknad,
        metadata: Metadata
    ): PreprosessertSøknad {
        val correlationId = CorrelationId(metadata.correlationId)
        val dokumentEier = K9MellomlagringGateway.DokumentEier(søknad.søker.fødselsnummer)

        logger.info("Genererer Oppsummerings-PDF av søknaden.")
        val oppsummeringPdf = pdfV1Generator.generateOppsummeringPdf(søknad)

        logger.info("Mellomlagrer Oppsummerings-PDF.")
        val oppsummeringPdfUrl = k9MellomlagringService.lagreDokument(
            dokument = K9MellomlagringGateway.Dokument(
                eier = dokumentEier,
                content = oppsummeringPdf,
                contentType = "application/pdf",
                title = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase"
            ),
            correlationId = correlationId
        )

        logger.info("Mellomlagrer Oppsummerings-JSON")
        val søknadJsonUrl = k9MellomlagringService.lagreDokument(
            dokument = K9MellomlagringGateway.Dokument(
                eier = dokumentEier,
                content = Søknadsformat.somJson(søknad.k9Format),
                contentType = "application/json",
                title = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - JSON"
            ),
            correlationId = correlationId
        )

        val komplettDokumentUrls = mutableListOf(
            listOf(
                oppsummeringPdfUrl,
                søknadJsonUrl
            )
        )

        if (søknad.vedleggUrls.isNotEmpty()) {
            logger.info("Legger til ${søknad.vedleggUrls.size} vedlegg URL's fra meldingen som dokument.")
            søknad.vedleggUrls.forEach { komplettDokumentUrls.add(listOf(it.toURI())) }
        }

        logger.info("Totalt ${komplettDokumentUrls.size} dokumentbolker med totalt ${komplettDokumentUrls.flatten().size} dokumenter")

        val preprosessertMeldingV1 = PreprosessertSøknad(
            søknad = søknad,
            dokumentUrls = komplettDokumentUrls.toList()
        )

        preprosessertMeldingV1.reportMetrics()
        return preprosessertMeldingV1
    }
}