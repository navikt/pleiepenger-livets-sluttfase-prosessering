package no.nav.helse

import no.nav.helse.prosessering.v1.søknad.*
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
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
import no.nav.k9.søknad.Søknad as k9FormatSøknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold

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
            fødselsdato = LocalDate.parse("2000-01-01"),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        fraOgMed = LocalDate.parse("2022-01-01"),
        tilOgMed = LocalDate.parse("2022-02-01"),
        vedleggId = listOf("123", "456"),
        opplastetIdVedleggId = listOf("987"),
        pleietrengende = Pleietrengende(norskIdentitetsnummer = "02119970078", navn = "Bjarne"),
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
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-10"),
                    landnavn = "Cuba",
                    landkode = "CU"
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2021-02-01"),
                    tilOgMed = LocalDate.parse("2021-02-10"),
                    landnavn = "Cuba",
                    landkode = "CU"
                )
            )
        ),
        arbeidsgivere = listOf(
            Arbeidsgiver(
                navn = "Something Fishy AS",
                organisasjonsnummer = "123456789",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(
                    jobberNormaltTimer = 7.5,
                    arbeidIPeriode = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                        enkeltdager = listOf(Enkeltdag(LocalDate.parse("2022-01-01"), Duration.ofHours(4)))
                    )
                )
            ),
            Arbeidsgiver(
                navn = "Slutta",
                organisasjonsnummer = "12121212",
                sluttetFørSøknadsperiode = true,
                erAnsatt = false
            )
        ),
        frilans = Frilans(
            startdato = LocalDate.parse("2015-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true,
            sluttdato = LocalDate.parse("2021-01-01"),
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 7.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.HELT_FRAVÆR,
                    enkeltdager = null
                )
            )
        ),
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            virksomhet = Virksomhet(
                fraOgMed = LocalDate.parse("2015-01-01"),
                næringstype = Næringstype.FISKE,
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.parse("2020-03-04")),
                fiskerErPåBladB = false,
                navnPåVirksomheten = "Bjarnes Bakeri",
                registrertINorge = false,
                registrertIUtlandet = Land("ABW","Aruba"),
                næringsinntekt = 9656876,
                erNyoppstartet = false,
                harFlereAktiveVirksomheter = false,
                varigEndring = VarigEndring(
                    dato = LocalDate.parse("2019-09-09"),
                    inntektEtterEndring = 854875,
                    forklaring = "Opplevde en varig endring fordi....."
                ),
                regnskapsfører = Regnskapsfører(
                    navn = "Regn",
                    telefon = "987654321"
                ),
                tilOgMed = null,
                organisasjonsnummer = null
            ),
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 7.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.SOM_VANLIG,
                    enkeltdager = null
                )
            )
        ),
        harVærtEllerErVernepliktig = false,
        opptjeningIUtlandet = listOf(
            OpptjeningIUtlandet(
                navn = "Kiwi AS",
                opptjeningType = OpptjeningType.ARBEIDSTAKER,
                land = Land(
                    landkode = "IKKE GYLDIG",
                    landnavn = "Belgia",
                ),
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10")
            )
        ),
        utenlandskNæring = listOf(
            UtenlandskNæring(
                næringstype = Næringstype.DAGMAMMA,
                navnPåVirksomheten = "Dagmamma AS",
                land = Land(landkode = "NDL", landnavn = "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10")
            )
        ),
        k9Format = gyldigK9Format(søknadId),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        ferieuttakIPerioden = FerieuttakIPerioden(
            skalTaUtFerieIPerioden = true,
            ferieuttak = listOf(
                Ferieuttak(fraOgMed = LocalDate.parse("2022-01-05"),
                tilOgMed = LocalDate.parse("2022-01-06"))
            )
        )
    )

    fun gyldigK9Format(søknadId: String = UUID.randomUUID().toString()) = k9FormatSøknad(
        SøknadId(søknadId),
        Versjon("1.0.0"),
        ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        K9Søker(NorskIdentitetsnummer.of("02119970078")),
        PleipengerLivetsSluttfase()
            .medPleietrengende(Pleietrengende().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("02119970078")))
            .medUtenlandsopphold(
                K9Utenlandsopphold()
                    .medPerioder(
                        mapOf(
                            Periode(
                                LocalDate.parse("2021-03-01"),
                                LocalDate.parse("2021-03-03")
                            ) to UtenlandsoppholdPeriodeInfo().medLand(Landkode.CANADA)
                        )
                    )
            )
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
            .medOpptjeningAktivitet(
                OpptjeningAktivitet()
                    .medFrilanser(
                        Frilanser()
                            .medStartdato(LocalDate.parse("2015-01-01"))
                            .medSluttdato(LocalDate.parse("2021-01-01"))
                    )
            )
    ).medKildesystem(Kildesystem.SØKNADSDIALOG)
}
