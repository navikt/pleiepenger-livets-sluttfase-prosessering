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
                "fødselsdato": "2019-01-29",
                "aktørId": "123456"
              },
              "vedleggUrls": [],
              "pleietrengende": {
                "norskIdentitetsnummer": "02119970078"
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
                  "opptjeningAktivitet": {},
                  "bosteder": {
                    "perioder": {
                      "2021-01-01/2021-01-01": {
                        "land": "DNK"
                      }
                    },
                    "perioderSomSkalSlettes": {}
                  },
                  "utenlandsopphold": {
                    "perioder": {},
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