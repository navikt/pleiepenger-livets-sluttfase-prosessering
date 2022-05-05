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
    val harFraværIPeriode: Boolean,
    val arbeidIPeriode: ArbeidIPeriode? = null
)

data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val jobberProsent: Double? = null,
    val erLiktHverUke: Boolean? = null,
    val enkeltdager: List<Enkeltdag>? = null,
    val fasteDager: PlanUkedager? = null
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

data class PlanUkedager(
    val mandag: Duration? = null,
    val tirsdag: Duration? = null,
    val onsdag: Duration? = null,
    val torsdag: Duration? = null,
    val fredag: Duration? = null
)
