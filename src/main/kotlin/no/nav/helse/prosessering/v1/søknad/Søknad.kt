package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class Søknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val vedleggId: List<String> = listOf(),
    val opplastetIdVedleggId: List<String> = listOf(),
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val pleietrengende: Pleietrengende,
    val arbeidsgivere: List<Arbeidsgiver>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val frilans: Frilans?,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)

data class Søker(
    val fødselsnummer: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate?,
    val aktørId: String
) {
    override fun toString(): String {
        return "Soker(fornavn='$fornavn', mellomnavn=$mellomnavn, etternavn='$etternavn', fødselsdato=$fødselsdato, aktørId='*****')"
    }
}

fun String.capitalizeName(): String = split(" ").joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar {
            it.titlecase()
        }
}
