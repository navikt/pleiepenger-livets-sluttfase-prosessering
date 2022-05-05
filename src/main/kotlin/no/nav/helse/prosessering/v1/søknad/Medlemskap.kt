package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean,
    val utenlandsoppholdSiste12Mnd: List<Bosted> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean,
    val utenlandsoppholdNeste12Mnd: List<Bosted> = listOf()
)

typealias Utenlandsopphold = Opphold
typealias Bosted = Opphold

data class Opphold(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
)

data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean? = null,
    val opphold: List<Utenlandsopphold> = listOf()
)
