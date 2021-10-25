package no.nav.helse.prosessering.v1.søknad

import java.net.URI
import java.net.URL
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

data class PreprosessertSøknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentUrls: List<List<URI>>,
    val søker: Søker,
    val vedleggUrls: List<URL>,
    val pleietrengende: Pleietrengende,
    val medlemskap: Medlemskap,
    val fraværsperioder: List<Fraværsperiode>,
    val k9Format: K9Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    internal constructor(
        søknad: Søknad,
        dokumentUrls: List<List<URI>>,
    ) : this(
        språk = søknad.språk,
        søknadId = søknad.søknadId,
        mottatt = søknad.mottatt,
        dokumentUrls = dokumentUrls,
        søker = søknad.søker,
        vedleggUrls = søknad.vedleggUrls,
        pleietrengende = søknad.pleietrengende,
        medlemskap = søknad.medlemskap,
        fraværsperioder = søknad.fraværsperioder,
        k9Format = søknad.k9Format,
        harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = søknad.harBekreftetOpplysninger
    )
}