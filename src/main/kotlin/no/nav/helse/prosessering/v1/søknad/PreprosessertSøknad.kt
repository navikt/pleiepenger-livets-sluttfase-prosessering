package no.nav.helse.prosessering.v1.søknad

import java.time.LocalDate
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

data class PreprosessertSøknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val søker: Søker,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val dokumentId: List<List<String>>,
    val pleietrengende: Pleietrengende,
    val arbeidsgivere: List<Arbeidsgiver>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val frilans: Frilans?,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    val harVærtEllerErVernepliktig: Boolean? = null,
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
        fraOgMed = søknad.fraOgMed,
        tilOgMed = søknad.tilOgMed,
        dokumentId = dokumentId,
        pleietrengende = søknad.pleietrengende,
        arbeidsgivere = søknad.arbeidsgivere,
        medlemskap = søknad.medlemskap,
        utenlandsoppholdIPerioden = søknad.utenlandsoppholdIPerioden,
        opptjeningIUtlandet = søknad.opptjeningIUtlandet,
        frilans = søknad.frilans,
        selvstendigNæringsdrivende = søknad.selvstendigNæringsdrivende,
        harVærtEllerErVernepliktig = søknad.harVærtEllerErVernepliktig,
        k9Format = søknad.k9Format,
        harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = søknad.harBekreftetOpplysninger
    )
}
