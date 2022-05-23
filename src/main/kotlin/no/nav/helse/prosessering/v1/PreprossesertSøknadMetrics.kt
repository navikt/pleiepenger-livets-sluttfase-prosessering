package no.nav.helse.prosessering.v1

import io.prometheus.client.Counter
import no.nav.helse.prosessering.v1.søknad.PreprosessertSøknad

private val generelCounter = Counter.build()
    .name("generel_counter")
    .help("Generel counter")
    .labelNames("spm", "svar")
    .register()

internal fun PreprosessertSøknad.reportMetrics(){
    utenlandsoppholdIPeriodenMetrikk()
    pleietrengendeMetrikk()
    opptjeningIUtlandet()
    verneplikt()
}

internal fun PreprosessertSøknad.pleietrengendeMetrikk(){
    if(pleietrengende.norskIdentitetsnummer == null){
        generelCounter.labels("pleietrengendeUtenFnr", "ja").inc()
        generelCounter.labels("pleietrengendeUtenFnrGrunn", "${pleietrengende.årsakManglerIdentitetsnummer}").inc()
    } else generelCounter.labels("pleietrengendeUtenFnr", "nei").inc()
}

internal fun PreprosessertSøknad.utenlandsoppholdIPeriodenMetrikk(){
    if(utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true){
        generelCounter.labels("utenlandsoppholdIPerioden", "ja").inc()
    } else generelCounter.labels("utenlandsoppholdIPerioden", "nei").inc()
}

internal fun PreprosessertSøknad.opptjeningIUtlandet(){
    if(this.opptjeningIUtlandet.isNotEmpty()){
        generelCounter.labels("opptjeningIUtlandet", "ja").inc()
    } else generelCounter.labels("opptjeningIUtlandet", "nei").inc()
}

internal fun PreprosessertSøknad.verneplikt(){
    if(this.harVærtEllerErVernepliktig == true){
        generelCounter.labels("verneplikt", "ja").inc()
    } else generelCounter.labels("verneplikt", "nei").inc()
}