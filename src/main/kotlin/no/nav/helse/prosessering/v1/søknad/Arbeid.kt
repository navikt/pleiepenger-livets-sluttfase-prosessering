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
    val arbeidIPeriode: ArbeidIPeriode
)

data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val enkeltdager: List<Enkeltdag>? = null
)

enum class JobberIPeriodeSvar(val pdfTekst: String) {
    SOM_VANLIG("Jeg jobber som normalt, og har ikke fravær"),
    REDUSERT("Jeg kombinerer delvis jobb med pleiepenger"),
    HELT_FRAVÆR("Jeg jobber ikke");

    fun tilBoolean(): Boolean{
        return when(this){
            SOM_VANLIG, REDUSERT -> true
            HELT_FRAVÆR -> false
        }
    }
}

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration
)