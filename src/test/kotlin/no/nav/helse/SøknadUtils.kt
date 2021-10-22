package no.nav.helse

import no.nav.helse.prosessering.v1.søknad.*
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder as K9AnnenForelder

object SøknadUtils {

    fun gyldigSøknad(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.now()
    ) = MeldingV1(
        språk = "nb",
        søknadId = søknadId,
        mottatt = mottatt,
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = søkerFødselsnummer,
            fødselsdato = LocalDate.now().minusDays(1000),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        vedleggUrls = listOf(),
        k9Format = gyldigK9Format(søknadId),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )

    fun gyldigK9Format(søknadId: String = UUID.randomUUID().toString()) = Søknad(
        SøknadId(søknadId),
        Versjon("1.0.0"),
        ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        K9Søker(NorskIdentitetsnummer.of("02119970078")),
        OmsorgspengerMidlertidigAlene(
            listOf(
                K9Barn(NorskIdentitetsnummer.of("29076523302"), null)
            ),
            K9AnnenForelder(
                NorskIdentitetsnummer.of("25058118020"),
                K9AnnenForelder.SituasjonType.FENGSEL,
                "Sitter i fengsel..",
                Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2030-01-01"))
            ),
            null
        )
    )
}
