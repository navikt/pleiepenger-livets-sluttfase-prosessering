package no.nav.helse.felles

import no.nav.helse.kafka.Cleanup
import java.util.*

data class K9Beskjed(
    val metadata: Metadata,
    val grupperingsId: String,
    val tekst: String,
    val link: String?,
    val dagerSynlig: Long,
    val søkerFødselsnummer: String,
    val eventId: String,
    val ytelse: String
)
const val DAGER_SYNLIG : Long= 7
const val TEKST = "Søknad om pleiepenger i livets sluttfase "
const val YTELSE = "PLEIEPENGER_LIVETS_SLUTTFASE"

fun Cleanup.tilK9Beskjed() = K9Beskjed(
    metadata = this.metadata,
    grupperingsId = this.melding.søknadId,
    tekst = TEKST,
    søkerFødselsnummer = this.melding.søker.fødselsnummer,
    dagerSynlig = DAGER_SYNLIG,
    link = null,
    eventId = UUID.randomUUID().toString(),
    ytelse = YTELSE
)