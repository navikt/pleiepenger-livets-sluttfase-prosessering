package no.nav.helse.prosessering.v1.søknad

import java.time.Duration
import java.time.LocalDate

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null
)

data class Arbeidsforhold(
    val jobberNormaltTimer: Double,
    val arbeidIPeriode: ArbeidIPeriode? = null
)

data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val enkeltdager: List<Enkeltdag>? = null
)

enum class JobberIPeriodeSvar(val pdfTekst: String) {
    JA("Ja"),
    NEI("Nei");

    fun tilBoolean(): Boolean{
        return when(this){
            JA -> true
            NEI -> false
        }
    }
}

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration
)