package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Duration
import java.time.LocalDate

data class Fraværsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val aktivitetFravær: List<AktivitetFravær>,
    val organisasjonsnummer: List<String>? = null,
    val antallTimerBorte: Duration? = null,
    val antallTimerPlanlagt: Duration? = null
)

enum class AktivitetFravær(val pdfTekst: String){
    FRILANS("Frilans"),
    SELVSTENDIG_NÆRINGSDRIVENDE("Selvstendig næringsdrivende"),
    ARBEIDSTAKER("Arbeidstaker")
}
