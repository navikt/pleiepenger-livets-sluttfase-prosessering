package no.nav.helse

import no.nav.helse.dokument.Søknadsformat
import no.nav.helse.prosessering.v1.søknad.*
import org.junit.jupiter.api.Disabled
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class SøknadsformatTest {

    @Disabled
    @Test
    fun `Søknaden journalføres som JSON uten vedlegg`() {
        val søknadId = UUID.randomUUID().toString()
        val json = Søknadsformat.somJson(melding(søknadId))

        val forventetSøknad =
            //language=json
            """
            {
              "søknadId": $søknadId,
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
              "id": "123456789",
              "annenForelder": {
                "navn": "Berit",
                "fnr": "02119970078",
                "situasjon": "FENGSEL",
                "situasjonBeskrivelse": "Sitter i fengsel..",
                "periodeOver6Måneder": false,
                "periodeFraOgMed": "2020-01-01",
                "periodeTilOgMed": "2020-10-01"
              },
              "barn": [
                {
                  "navn": "Ole",
                  "identitetsnummer": "1234",
                  "aktørId": null
                }
              ],
              "k9Format": {
                "søknadId": $søknadId,
                "versjon": "1.0.0",
                "mottattDato": "2018-01-02T03:04:05.000Z",
                "søker": {
                  "norskIdentitetsnummer": "02119970078"
                },
                "språk": "nb",
                "ytelse": {
                  "type": "OMP_UTV_MA",
                  "barn": [
                    {
                      "norskIdentitetsnummer": "29076523302",
                      "fødselsdato": null
                    }
                  ],
                  "annenForelder": {
                    "norskIdentitetsnummer": "25058118020",
                    "situasjon": "FENGSEL",
                    "situasjonBeskrivelse": "Sitter i fengsel..",
                    "periode": "2020-01-01/2030-01-01"
                  },
                  "begrunnelse": null
                },
                "journalposter": []
              },
              "harForståttRettigheterOgPlikter": true,
              "harBekreftetOpplysninger": true
            }
            """.trimIndent()

        JSONAssert.assertEquals(forventetSøknad, String(json), true)
    }

    private fun melding(soknadId: String): MeldingV1 = MeldingV1(
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