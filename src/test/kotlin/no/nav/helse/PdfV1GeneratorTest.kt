package no.nav.helse

import no.nav.helse.prosessering.v1.PdfV1Generator
import no.nav.helse.prosessering.v1.søknad.*
import java.io.File
import java.time.LocalDate
import kotlin.test.Test

class PdfV1GeneratorTest {

    private companion object {
        private val generator = PdfV1Generator()
        private val fødselsdato = LocalDate.now()
    }

    private fun genererOppsummeringsPdfer(writeBytes: Boolean) {

        var id = "1-full-søknad"
        var pdf = generator.generateOppsummeringPdf(
            søknad = SøknadUtils.gyldigSøknad(søknadId = id)
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "2-søknad-pleietrengende-uten-norskidentifikator"
        pdf = generator.generateOppsummeringPdf(
            søknad = SøknadUtils.gyldigSøknad(søknadId = id).copy(
                opptjeningIUtlandet = listOf(),
                utenlandskNæring = listOf(),
                pleietrengende = Pleietrengende(
                    navn = "Bjarne",
                    fødselsdato = LocalDate.now().minusYears(45),
                    årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
                )
            )
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "3-søknad-med-utenlandsk-næring"
        pdf = generator.generateOppsummeringPdf(
            søknad = SøknadUtils.gyldigSøknad(søknadId = id).copy(
                opptjeningIUtlandet = listOf(),
                utenlandskNæring = listOf(
                    UtenlandskNæring(
                        næringstype = Næringstype.DAGMAMMA,
                        navnPåVirksomheten = "Dagmamma AS",
                        land = Land(landkode = "NDL", landnavn = "Nederland"),
                        organisasjonsnummer = "123ABC",
                        fraOgMed = LocalDate.parse("2022-01-01"),
                        tilOgMed = LocalDate.parse("2022-01-10")
                    ),
                    UtenlandskNæring(
                        næringstype = Næringstype.FISKE,
                        navnPåVirksomheten = "Fiskeriet AS",
                        land = Land(landkode = "NDL", landnavn = "Nederland"),
                        organisasjonsnummer = null,
                        fraOgMed = LocalDate.parse("2022-01-01")
                    )
                )
            )
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "4-sluttet-som-frilans-før-søknadsperioden"
        pdf = generator.generateOppsummeringPdf(
            søknad = SøknadUtils.gyldigSøknad(søknadId = id).copy(
                opptjeningIUtlandet = listOf(),
                utenlandskNæring = listOf(),
                fraOgMed = LocalDate.parse("2022-01-05"),
                tilOgMed = LocalDate.parse("2022-01-10"),
                frilans = Frilans(
                    startdato = LocalDate.parse("2000-01-01"),
                    jobberFortsattSomFrilans = false,
                    sluttdato = LocalDate.parse("2022-01-04"),
                    arbeidsforhold = null,
                    harHattInntektSomFrilanser = false
                )
            )
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)
    }

    private fun pdfPath(soknadId: String) = "${System.getProperty("user.dir")}/generated-pdf-$soknadId.pdf"

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    //@Ignore
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }
}
