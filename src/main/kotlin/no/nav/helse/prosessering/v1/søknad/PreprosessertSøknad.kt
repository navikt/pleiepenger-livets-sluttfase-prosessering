package no.nav.helse.prosessering.v1.søknad

import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

data class PreprosessertSøknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val søker: Søker,
    val dokumentId: List<List<String>>,
    val pleietrengende: Pleietrengende,
    val medlemskap: Medlemskap,
    val fraværsperioder: List<Fraværsperiode>,
    val utenlandsopphold: List<Utenlandsopphold>?,
    val frilans: Frilans?,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    val k9Format: K9Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    internal constructor(
        søknad: Søknad,
        dokumentId: List<List<String>>,
    ) : this(
        språk = søknad.språk,
        søknadId = søknad.søknadId,
        mottatt = søknad.mottatt,
        søker = søknad.søker,
        dokumentId = dokumentId,
        pleietrengende = søknad.pleietrengende,
        medlemskap = søknad.medlemskap,
        fraværsperioder = søknad.fraværsperioder,
        utenlandsopphold = søknad.utenlandsopphold,
        frilans = søknad.frilans,
        selvstendigNæringsdrivende = søknad.selvstendigNæringsdrivende,
        k9Format = søknad.k9Format,
        harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = søknad.harBekreftetOpplysninger
    )
}