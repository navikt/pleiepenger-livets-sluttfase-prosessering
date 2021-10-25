package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dokument.Søknadsformat
import no.nav.helse.prosessering.v1.søknad.*
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class SøknadsformatTest {

    @Test
    fun `Søknaden journalføres som JSON uten vedlegg`() {
        val søknadId = UUID.randomUUID().toString()
        val json = Søknadsformat.somJson(melding(søknadId))

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
                "fødselsdato": "2018-01-24",
                "aktørId": "123456"
              },
              "vedleggUrls": [],
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

    private fun melding(soknadId: String): Søknad = Søknad(
        søknadId = soknadId,
        mottatt = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola",
            fødselsdato = LocalDate.parse("2018-01-24")
        ),
        vedleggUrls = listOf(),
        k9Format = SøknadUtils.gyldigK9Format(soknadId),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )
}