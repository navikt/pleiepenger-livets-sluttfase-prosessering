package no.nav.helse.prosessering.v1.søknad

import java.time.LocalDate

class UtenlandskNæring(
    val næringstype: Næringstype,
    val navnPåVirksomheten: String,
    val land: Land,
    val organisasjonsnummer: String? = null,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null
)