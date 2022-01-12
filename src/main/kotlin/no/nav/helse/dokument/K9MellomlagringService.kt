package no.nav.helse.dokument

import no.nav.helse.felles.CorrelationId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("nav.DokumentService")

class K9MellomlagringService(
    private val k9MellomlagringGateway: K9MellomlagringGateway
) {
    internal suspend fun lagreDokument(
        dokument: K9MellomlagringGateway.Dokument,
        correlationId: CorrelationId
    ) : URI {
        return k9MellomlagringGateway.lagreDokmenter(
            dokumenter = setOf(dokument),
            correlationId = correlationId
        ).first()
    }

    internal suspend fun slettDokumeter(
        dokumentIdBolks: List<List<String>>,
        dokumentEier: K9MellomlagringGateway.DokumentEier,
        correlationId : CorrelationId
    ) {
        k9MellomlagringGateway.slettDokmenter(
            dokumentId = dokumentIdBolks.flatten(),
            dokumentEier = dokumentEier,
            correlationId = correlationId
        )

    }

}