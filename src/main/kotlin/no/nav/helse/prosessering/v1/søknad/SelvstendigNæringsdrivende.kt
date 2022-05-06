package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class SelvstendigNæringsdrivende(
    val virksomhet: Virksomhet,
    val arbeidsforhold: Arbeidsforhold? = null
)

data class Virksomhet(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate? = null,
    val næringstype: Næringstype,
    val fiskerErPåBladB: Boolean = false,
    val navnPåVirksomheten: String,
    val registrertINorge: Boolean,
    val registrertIUtlandet: Land? = null,
    val næringsinntekt: Int? = null,
    val organisasjonsnummer: String? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val erNyoppstartet: Boolean,
    val harFlereAktiveVirksomheter: Boolean
)

data class Land(val landkode: String, val landnavn: String)

data class YrkesaktivSisteTreFerdigliknedeArene(
    val oppstartsdato: LocalDate
)

enum class Næringstype(val beskrivelse: String) {
    FISKE("Fiske"),
    JORDBRUK_SKOGBRUK("Jordbruk/skogbruk"),
    DAGMAMMA("Dagmamma eller familiebarnehage i eget hjem"),
    ANNEN("Annen");
}

data class VarigEndring(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dato: LocalDate,
    val inntektEtterEndring: Int,
    val forklaring: String
)

data class Regnskapsfører(
    val navn: String,
    val telefon: String
)