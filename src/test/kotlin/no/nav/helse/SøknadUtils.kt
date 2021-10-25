package no.nav.helse

import no.nav.helse.prosessering.v1.søknad.*
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.pls.v1.Pleietrengende
import no.nav.k9.søknad.ytelse.pls.v1.PleipengerLivetsSluttfase
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

object SøknadUtils {

    fun gyldigSøknad(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
    ) = Søknad(
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
        pleietrengende = Pleietrengende("02119970078"),
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    tilOgMed = LocalDate.parse("2020-01-10"),
                    landnavn = "Brasil",
                    landkode = "BR"
                )
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-10"),
                    landnavn = "Cuba",
                    landkode = "CU"
                )
            )
        ),
        fraværsperioder = listOf(
            Fraværsperiode(
                fraOgMed = LocalDate.parse("2021-02-01"),
                tilOgMed = LocalDate.parse("2021-02-10"),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7).plusMinutes(30)
            ),
            Fraværsperiode(
                fraOgMed = LocalDate.parse("2021-02-13"),
                tilOgMed = LocalDate.parse("2021-02-15")
            )
        ),
        k9Format = gyldigK9Format(søknadId),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )

    fun gyldigK9Format(søknadId: String = UUID.randomUUID().toString()) = Søknad(
        SøknadId(søknadId),
        Versjon("1.0.0"),
        ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        K9Søker(NorskIdentitetsnummer.of("02119970078")),
        PleipengerLivetsSluttfase()
            .medPleietrengende(Pleietrengende(NorskIdentitetsnummer.of("02119970078")))
            .medBosteder(
                Bosteder()
                    .medPerioder(
                        mapOf(
                            Periode(
                                LocalDate.parse("2021-01-01"),
                                LocalDate.parse("2021-01-01")
                            ) to Bosteder.BostedPeriodeInfo().medLand(
                                Landkode.DANMARK
                            )
                        )
                    )
            )
    )
}
