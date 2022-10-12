package no.nav.helse.prosessering.v1

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.context.MapValueResolver
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.util.XRLog
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.prosessering.v1.PdfV1Generator.Companion.DATE_FORMATTER
import no.nav.helse.prosessering.v1.søknad.*
import no.nav.helse.utils.somNorskDag
import no.nav.helse.utils.somNorskMåned
import no.nav.helse.utils.somTekst
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import java.util.logging.Level

internal class PdfV1Generator {
    companion object {
        private const val ROOT = "handlebars"
        private const val SOKNAD = "soknad"

        private val REGULAR_FONT = "$ROOT/fonts/SourceSansPro-Regular.ttf".fromResources().readBytes()
        private val BOLD_FONT = "$ROOT/fonts/SourceSansPro-Bold.ttf".fromResources().readBytes()
        private val ITALIC_FONT = "$ROOT/fonts/SourceSansPro-Italic.ttf".fromResources().readBytes()

        private val sRGBColorSpace = "$ROOT/sRGB.icc".fromResources().readBytes()

        private val handlebars = Handlebars(ClassPathTemplateLoader("/$ROOT")).apply {
            registerHelper("eq", Helper<String> { context, options ->
                if (context == options.param(0)) options.fn() else options.inverse()
            })
            registerHelper("eqTall", Helper<Int> { context, options ->
                if (context == options.param(0)) options.fn() else options.inverse()
            })
            registerHelper("fritekst", Helper<String> { context, _ ->
                if (context == null) "" else {
                    val text = Handlebars.Utils.escapeExpression(context)
                        .toString()
                        .replace(Regex("\\r\\n|[\\n\\r]"), "<br/>")
                    Handlebars.SafeString(text)
                }
            })
            registerHelper("jaNeiSvar", Helper<Boolean> { context, _ ->
                if (context == true) "Ja" else "Nei"
            })

            infiniteLoops(true)
        }

        private val soknadTemplate = handlebars.compile(SOKNAD)

        private val ZONE_ID = ZoneId.of("Europe/Oslo")
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZONE_ID)
        val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZONE_ID)
    }

    internal fun generateOppsummeringPdf(søknad: Søknad): ByteArray {
        XRLog.listRegisteredLoggers().forEach { logger -> XRLog.setLevel(logger, Level.WARNING) }
        soknadTemplate.apply(
            Context
                .newBuilder(
                    mapOf(
                        "tittel" to "Søknad om pleiepenger i livets sluttfase",
                        "søknadId" to søknad.søknadId,
                        "søknadMottattDag" to søknad.mottatt.withZoneSameInstant(ZONE_ID).norskDag(),
                        "søknadMottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
                        "periode" to mapOf(
                            "fraOgMed" to DATE_FORMATTER.format(søknad.fraOgMed),
                            "tilOgMed" to DATE_FORMATTER.format(søknad.tilOgMed)
                        ),
                        "søker" to mapOf(
                            "navn" to søknad.søker.formatertNavn().capitalizeName(),
                            "fødselsnummer" to søknad.søker.fødselsnummer
                        ),
                        "pleietrengende" to søknad.pleietrengende.somMap(),
                        "medlemskap" to søknad.medlemskap.somMap(),
                        "utenlandsoppholdIPerioden" to mapOf(
                            "skalOppholdeSegIUtlandetIPerioden" to søknad.utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden,
                            "opphold" to søknad.utenlandsoppholdIPerioden.opphold.somMapUtenlandsopphold()
                        ),
                        "ferieuttakIPerioden" to mapOf(
                            "skalTaUtFerieIPerioden" to søknad.ferieuttakIPerioden?.skalTaUtFerieIPerioden,
                            "ferieuttak" to søknad.ferieuttakIPerioden?.ferieuttak?.somMapFerieuttak()
                        ),
                        "harLastetOppId" to søknad.opplastetIdVedleggId.isNotEmpty(),
                        "harLastetOppLegeerklæring" to søknad.vedleggId.isNotEmpty(),
                        "arbeidsgivere" to søknad.arbeidsgivere.somMapAnsatt(),
                        "frilans" to søknad.frilans?.somMap(),
                        "selvstendigNæringsdrivende" to søknad.selvstendigNæringsdrivende?.somMap(),
                        "opptjeningIUtlandet" to søknad.opptjeningIUtlandet.somMap(),
                        "utenlandskNæring" to søknad.utenlandskNæring.somMapUtenlandskNæring(),
                        "harVærtEllerErVernepliktig" to søknad.harVærtEllerErVernepliktig,
                        "samtykke" to mapOf(
                            "harForståttRettigheterOgPlikter" to søknad.harForståttRettigheterOgPlikter,
                            "harBekreftetOpplysninger" to søknad.harBekreftetOpplysninger
                        ),
                        "hjelp" to mapOf(
                            "språk" to søknad.språk?.språkTilTekst(),
                            "ingen_arbeidsgivere" to søknad.arbeidsgivere.isEmpty(),
                            "harFlereAktiveVirksomheterErSatt" to søknad.harFlereAktiveVirksomehterSatt(),
                            "ingen_arbeidsforhold" to !søknad.harMinstEtArbeidsforhold(),
                            "harVærtEllerErVernepliktigErSatt" to (søknad.harVærtEllerErVernepliktig != null)
                            )
                    )
                )
                .resolver(MapValueResolver.INSTANCE)
                .build()
        ).let { html ->
            val outputStream = ByteArrayOutputStream()
            PdfRendererBuilder()
                .useFastMode()
                .usePdfUaAccessbility(true)
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_B)
                .useColorProfile(sRGBColorSpace)
                .withHtmlContent(html, "")
                .medFonter()
                .toStream(outputStream)
                .buildPdfRenderer()
                .createPDF()
            return outputStream.use {
                it.toByteArray()
            }
        }
    }

    private fun PdfRendererBuilder.medFonter() =
        useFont(
            { ByteArrayInputStream(REGULAR_FONT) },
            "Source Sans Pro",
            400,
            BaseRendererBuilder.FontStyle.NORMAL,
            false
        )
            .useFont(
                { ByteArrayInputStream(BOLD_FONT) },
                "Source Sans Pro",
                700,
                BaseRendererBuilder.FontStyle.NORMAL,
                false
            )
            .useFont(
                { ByteArrayInputStream(ITALIC_FONT) },
                "Source Sans Pro",
                400,
                BaseRendererBuilder.FontStyle.ITALIC,
                false
            )
}

private fun Søker.formatertNavn() = if (mellomnavn != null) "$fornavn $mellomnavn $etternavn" else "$fornavn $etternavn"

private fun Boolean?.erSatt() = this != null

private fun String.språkTilTekst() = when (this.lowercase()) {
    "nb" -> "bokmål"
    "nn" -> "nynorsk"
    else -> this
}

private fun ZonedDateTime.norskDag() = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Mandag"
    DayOfWeek.TUESDAY -> "Tirsdag"
    DayOfWeek.WEDNESDAY -> "Onsdag"
    DayOfWeek.THURSDAY -> "Torsdag"
    DayOfWeek.FRIDAY -> "Fredag"
    DayOfWeek.SATURDAY -> "Lørdag"
    else -> "Søndag"
}

private fun Pleietrengende.somMap() = mapOf<String, Any?>(
    "manglerNorskIdentitetsnummer" to (norskIdentitetsnummer == null),
    "norskIdentitetsnummer" to norskIdentitetsnummer,
    "fødselsdato" to if(fødselsdato != null) DATE_FORMATTER.format(fødselsdato) else null,
    "årsakManglerIdentitetsnummer" to årsakManglerIdentitetsnummer?.pdfTekst,
    "navn" to navn
)

private fun Medlemskap.somMap() = mapOf<String, Any?>(
    "data" to this,
    "harBoddIUtlandetSiste12Mnd" to this.harBoddIUtlandetSiste12Mnd,
    "utenlandsoppholdSiste12Mnd" to this.utenlandsoppholdSiste12Mnd.somMapOpphold(),
    "skalBoIUtlandetNeste12Mnd" to this.skalBoIUtlandetNeste12Mnd,
    "utenlandsoppholdNeste12Mnd" to this.utenlandsoppholdNeste12Mnd.somMapOpphold()
)

private fun List<Opphold>.somMapOpphold(): List<Map<String, Any?>> {
    return map {
        mapOf(
            "landnavn" to it.landnavn,
            "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
            "tilOgMed" to DATE_FORMATTER.format(it.tilOgMed)
        )
    }
}

private fun List<Utenlandsopphold>.somMapUtenlandsopphold(): List<Map<String, Any?>> {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Oslo"))
    return map {
        mapOf<String, Any?>(
            "landnavn" to it.landnavn,
            "landkode" to it.landkode,
            "fraOgMed" to dateFormatter.format(it.fraOgMed),
            "tilOgMed" to dateFormatter.format(it.tilOgMed),
        )
    }
}

private fun Søknad.harMinstEtArbeidsforhold() : Boolean{
    frilans?.let {
        if(it.arbeidsforhold != null) return true
    }

    selvstendigNæringsdrivende?.let {
        if(it.arbeidsforhold != null) return true
    }

    if(arbeidsgivere.any(){it.arbeidsforhold != null}) return true

    return false
}

private fun Arbeidsforhold.somMap(): Map<String, Any?> = mapOf(
    "jobberNormaltTimer" to jobberNormaltTimer,
    "arbeidIPeriode" to arbeidIPeriode.somMap()
)

private fun ArbeidIPeriode.somMap(): Map<String, Any?> = mapOf(
    "jobberIPerioden" to jobberIPerioden.tilBoolean(),
    "enkeltdagerPerMnd" to enkeltdager?.somMapPerMnd()
)

private fun List<Enkeltdag>.somMapEnkeltdag(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "dato" to DATE_FORMATTER.format(it.dato),
            "dag" to it.dato.dayOfWeek.somNorskDag(),
            "tid" to it.tid.somTekst(avkort = false)
        )
    }
}

private fun List<Enkeltdag>.somMapPerUke(): List<Map<String, Any>> {
    val perUke = this.groupBy {
        val uketall = it.dato.get(WeekFields.of(Locale.getDefault()).weekOfYear())
        if (uketall == 0) 53 else uketall
    }
    return perUke.map {
        mapOf(
            "uke" to it.key,
            "dager" to it.value.somMapEnkeltdag()
        )
    }
}

fun List<Enkeltdag>.somMapPerMnd(): List<Map<String, Any>> {
    val perMåned: Map<Month, List<Enkeltdag>> = this.groupBy { it.dato.month }

    return perMåned.map {
        mapOf(
            "år" to it.value.first().dato.year,
            "måned" to it.key.somNorskMåned().capitalizeName(),
            "enkeltdagerPerUke" to it.value.somMapPerUke()
        )
    }
}

private fun Duration?.harGyldigVerdi() = this != null && this != Duration.ZERO

private fun List<Arbeidsgiver>.somMapAnsatt() = map {
    mapOf<String, Any?>(
        "navn" to it.navn,
        "organisasjonsnummer" to it.organisasjonsnummer,
        "erAnsatt" to it.erAnsatt,
        "arbeidsforhold" to it.arbeidsforhold?.somMap(),
        "sluttetFørSøknadsperiodeErSatt" to (it.sluttetFørSøknadsperiode != null),
        "sluttetFørSøknadsperiode" to it.sluttetFørSøknadsperiode
    )
}

private fun Frilans.somMap() = mapOf<String, Any?>(
    "startdato" to DATE_FORMATTER.format(startdato),
    "sluttdato" to if (sluttdato != null) DATE_FORMATTER.format(sluttdato) else null,
    "jobberFortsattSomFrilans" to jobberFortsattSomFrilans,
    "arbeidsforhold" to arbeidsforhold?.somMap(),
    "harHattInntektSomFrilanser" to harHattInntektSomFrilanser
)

private fun SelvstendigNæringsdrivende.somMap() = mapOf<String, Any?>(
    "virksomhet" to virksomhet.somMap(),
    "arbeidsforhold" to arbeidsforhold?.somMap()
)


private fun Søknad.harFlereAktiveVirksomehterSatt() =
    (this.selvstendigNæringsdrivende?.virksomhet?.harFlereAktiveVirksomheter != null)

private fun Virksomhet.somMap(): Map<String, Any?> = mapOf(
    "næringstype" to næringstype.beskrivelse,
    "næringsinntekt" to næringsinntekt,
    "yrkesaktivSisteTreFerdigliknedeÅrene" to yrkesaktivSisteTreFerdigliknedeÅrene?.somMap(),
    "varigEndring" to varigEndring?.somMap(),
    "harFlereAktiveVirksomheter" to harFlereAktiveVirksomheter,
    "navnPåVirksomheten" to navnPåVirksomheten,
    "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
    "tilOgMed" to if (tilOgMed != null) DATE_FORMATTER.format(tilOgMed) else null,
    "fiskerErPåBladB" to fiskerErPåBladB,
    "registrertINorge" to registrertINorge,
    "organisasjonsnummer" to organisasjonsnummer,
    "registrertIUtlandet" to registrertIUtlandet?.somMap(),
    "regnskapsfører" to regnskapsfører?.somMap()
)

private fun List<OpptjeningIUtlandet>.somMap(): List<Map<String, Any?>>? {
    if(isEmpty()) return null
    return map {
        mapOf<String, Any?>(
            "navn" to it.navn,
            "land" to it.land.somMap(),
            "opptjeningType" to it.opptjeningType.pdfTekst,
            "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
            "tilOgMed" to DATE_FORMATTER.format(it.tilOgMed)
        )
    }
}

private fun List<UtenlandskNæring>.somMapUtenlandskNæring(): List<Map<String, Any?>>? {
    if(isEmpty()) return null
    return map {
        mapOf(
            "næringstype" to it.næringstype.beskrivelse,
            "navnPåVirksomheten" to it.navnPåVirksomheten,
            "land" to it.land.somMap(),
            "organisasjonsnummer" to it.organisasjonsnummer,
            "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
            "tilOgMed" to if(it.tilOgMed != null) DATE_FORMATTER.format(it.tilOgMed) else null
        )
    }
}

private fun Regnskapsfører.somMap() = mapOf<String, Any?>(
    "navn" to navn,
    "telefon" to telefon
)

private fun YrkesaktivSisteTreFerdigliknedeArene.somMap() = mapOf<String, Any?>(
    "oppstartsdato" to DATE_FORMATTER.format(oppstartsdato)
)

private fun Land.somMap() = mapOf<String, Any?>(
    "landnavn" to landnavn,
    "landkode" to landkode
)

private fun VarigEndring.somMap() = mapOf<String, Any?>(
    "dato" to DATE_FORMATTER.format(dato),
    "inntektEtterEndring" to inntektEtterEndring,
    "forklaring" to forklaring
)

private fun List<Ferieuttak>.somMapFerieuttak(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
            "tilOgMed" to DATE_FORMATTER.format(it.tilOgMed)
        )
    }
}