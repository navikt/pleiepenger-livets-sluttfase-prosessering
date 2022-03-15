package no.nav.helse

import no.nav.helse.SøknadUtils.gyldigSøknad
import no.nav.helse.dokument.Søknadsformat
import org.skyscreamer.jsonassert.JSONAssert
import java.util.*
import kotlin.test.Test

class SøknadsformatTest {

    @Test
    fun `Søknaden journalføres som JSON uten vedlegg`() {
        val søknadId = UUID.randomUUID().toString()
        val json = Søknadsformat.somJson(gyldigSøknad(søknadId = søknadId))

        println(String(json))

        val forventetSøknad =
            //language=json
            """
            {
              "søknadId": "$søknadId",
              "mottatt": "2018-01-02T03:04:05.000000006Z",
              "språk": "nb",
              "søker": {
                "fødselsnummer": "02119970078",
                "fornavn": "Ola",
                "mellomnavn": "Mellomnavn",
                "etternavn": "Nordmann",
                "fødselsdato": "2000-01-01",
                "aktørId": "123456"
              },
              "fraOgMed": "2022-01-01",
              "tilOgMed": "2022-02-01",
              "vedleggId": ["123", "456"],
              "pleietrengende": {
                "norskIdentitetsnummer": "02119970078",
                "navn" : "Bjarne"
              },
              "medlemskap": {
                "harBoddIUtlandetSiste12Mnd": true,
                "utenlandsoppholdSiste12Mnd": [
                  {
                    "fraOgMed": "2020-01-01",
                    "tilOgMed": "2020-01-10",
                    "landkode": "BR",
                    "landnavn": "Brasil"
                  }
                ],
                "skalBoIUtlandetNeste12Mnd": true,
                "utenlandsoppholdNeste12Mnd": [
                  {
                    "fraOgMed": "2021-01-01",
                    "tilOgMed": "2021-01-10",
                    "landkode": "CU",
                    "landnavn": "Cuba"
                  }
                ]
              },
              "utenlandsoppholdIPerioden": {
                "skalOppholdeSegIUtlandetIPerioden": true,
                "opphold": [
                    {
                      "fraOgMed": "2021-01-01",
                      "tilOgMed": "2021-01-10",
                      "landkode": "CU",
                      "landnavn": "Cuba"
                    },
                    {
                      "fraOgMed": "2021-02-01",
                      "tilOgMed": "2021-02-10",
                      "landkode": "CU",
                      "landnavn": "Cuba"
                    }
                  ]
              },
              "frilans" : {
                "startdato" : "2015-01-01",
                "jobberFortsattSomFrilans" : false,
                "sluttdato" : "2021-01-01"
              },
              "selvstendigNæringsdrivende": {
                "fraOgMed": "2015-01-01",
                "tilOgMed": null,
                "næringstype": "FISKE",
                "fiskerErPåBladB": false,
                "navnPåVirksomheten": "Bjarnes Bakeri",
                "registrertINorge": false,
                "registrertIUtlandet": {
                  "landkode": "ABW",
                  "landnavn": "Aruba"
                },
                "næringsinntekt": 9656876,
                "organisasjonsnummer": null,
                "yrkesaktivSisteTreFerdigliknedeÅrene": {
                  "oppstartsdato": "2020-03-04"
                },
                "varigEndring": {
                  "dato": "2019-09-09",
                  "inntektEtterEndring": 854875,
                  "forklaring": "Opplevde en varig endring fordi....."
                },
                "regnskapsfører": {
                  "navn": "Regn",
                  "telefon": "987654321"
                },
                "erNyoppstartet": false,
                "harFlereAktiveVirksomheter": false
              },
              "k9Format": {
                "søknadId": "$søknadId",
                "versjon": "1.0.0",
                "mottattDato": "2018-01-02T03:04:05.000Z",
                "søker": {
                  "norskIdentitetsnummer": "02119970078"
                },
                "språk": "nb",
                "ytelse": {
                  "type": "PLEIEPENGER_LIVETS_SLUTTFASE",
                  "pleietrengende": {
                    "norskIdentitetsnummer": "02119970078"
                  },
                  "arbeidstid": {
                    "arbeidstakerList": [],
                    "frilanserArbeidstidInfo": null,
                    "selvstendigNæringsdrivendeArbeidstidInfo": null
                  },
                  "opptjeningAktivitet": {
                    "frilanser" : {
                      "startdato" : "2015-01-01",
                      "sluttdato" : "2021-01-01"
                    }
                  },
                  "bosteder": {
                    "perioder": {
                      "2021-01-01/2021-01-01": {
                        "land": "DNK"
                      }
                    },
                    "perioderSomSkalSlettes": {}
                  },
                  "trekkKravPerioder" : [],
                  "utenlandsopphold": {
                    "perioder": {
                      "2021-03-01/2021-03-03": {
                        "land": "CAN",
                        "årsak": null
                      }
                    },
                    "perioderSomSkalSlettes": {}
                  }
                },
                "journalposter": [],
                "begrunnelseForInnsending": {
                  "tekst": null
                }
              },
              "harForståttRettigheterOgPlikter": true,
              "harBekreftetOpplysninger": true
            }
            """.trimIndent()

        JSONAssert.assertEquals(forventetSøknad, String(json), true)
    }
}
