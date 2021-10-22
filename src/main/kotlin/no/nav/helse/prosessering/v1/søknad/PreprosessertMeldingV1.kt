package no.nav.helse.prosessering.v1.søknad

import no.nav.k9.søknad.Søknad
import java.net.URI
import java.net.URL
import java.time.ZonedDateTime

data class PreprosessertMeldingV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentUrls: List<List<URI>>,
    val søker: Søker,
    val vedleggUrls: List<URL>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    internal constructor(
        melding: MeldingV1,
        dokumentUrls: List<List<URI>>,
    ) : this(
        språk = melding.språk,
        søknadId = melding.søknadId,
        mottatt = melding.mottatt,
        dokumentUrls = dokumentUrls,
        søker = melding.søker,
        vedleggUrls = melding.vedleggUrls,
        k9Format = melding.k9Format,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger
    )
}