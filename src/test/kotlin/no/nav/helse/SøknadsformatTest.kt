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
              "opplastetIdVedleggId": ["987"],
              "pleietrengende": {
                "norskIdentitetsnummer": "02119970078",
                "fødselsdato": null,
                "årsakManglerIdentitetsnummer": null,
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
              "arbeidsgivere": [
                {
                  "navn": "Something Fishy AS",
                  "organisasjonsnummer": "123456789",
                  "erAnsatt": true,
                  "sluttetFørSøknadsperiode": false,
                    "arbeidsforhold": {
                      "jobberNormaltTimer": 7.5,
                      "arbeidIPeriode": {
                        "jobberIPerioden": "REDUSERT",
                        "enkeltdager": [
                          {
                            "dato": "2022-01-01",
                            "tid": "PT4H"
                          }
                        ]
                      }
                    }
                },
                {
                  "navn": "Slutta",
                  "organisasjonsnummer": "12121212",
                  "erAnsatt": false,
                  "sluttetFørSøknadsperiode": true,
                  "arbeidsforhold": null
                }
              ],
              "frilans" : {
                "startdato" : "2015-01-01",
                "jobberFortsattSomFrilans" : false,
                "sluttdato" : "2021-01-01",
                "harHattInntektSomFrilanser": true,
                "arbeidsforhold": {
                  "jobberNormaltTimer": 7.5,
                  "arbeidIPeriode": {
                    "jobberIPerioden": "HELT_FRAVÆR",
                    "enkeltdager": null
                  }
                }
              },
              "selvstendigNæringsdrivende": {
                "virksomhet": {
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
                "arbeidsforhold": {
                  "jobberNormaltTimer": 7.5,
                  "arbeidIPeriode": {
                    "jobberIPerioden": "SOM_VANLIG",
                    "enkeltdager": null
                  }
                }
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
                    "norskIdentitetsnummer": "02119970078",
                    "fødselsdato": null
                  },
                  "søknadsperiode": [],
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
                  "uttak": {
                    "perioder": {}
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
              "harVærtEllerErVernepliktig" : false,
              "opptjeningIUtlandet": [
                {
                  "navn": "Kiwi AS",
                  "opptjeningType": "ARBEIDSTAKER",
                  "land": {
                    "landkode": "IKKE GYLDIG",
                    "landnavn": "Belgia"
                  },
                  "fraOgMed": "2022-01-01",
                  "tilOgMed": "2022-01-10"
                }
              ],
             "utenlandskNæring": [
               {
                 "næringstype": "DAGMAMMA",
                 "navnPåVirksomheten": "Dagmamma AS",
                 "land": {
                   "landkode": "NDL",
                   "landnavn": "Nederland"
                 },
                 "organisasjonsnummer": "123ABC",
                 "fraOgMed": "2022-01-01",
                 "tilOgMed": "2022-01-10"
               }
             ],
              "harForståttRettigheterOgPlikter": true,
              "harBekreftetOpplysninger": true
            }
            """.trimIndent()
        JSONAssert.assertEquals(forventetSøknad, String(json), true)
    }
}
