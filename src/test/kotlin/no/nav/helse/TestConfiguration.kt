package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import org.testcontainers.containers.KafkaContainer

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        kafkaEnvironment: KafkaContainer? = null,
        port : Int = 8080,
        joarkBaseUrl : String? = wireMockServer?.getJoarkBaseUrl(),
        k9MellomlagringServiceDiscovery : String? = wireMockServer?.getK9MellomlagringServiceDiscovery()
    ) : Map<String, String>{
        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.gateways.k9_joark_url","$joarkBaseUrl"),
            Pair("nav.gateways.k9_mellomlagring_service_discovery","$k9MellomlagringServiceDiscovery")
        )

        // Clients
        if (wireMockServer != null) {
            map["nav.auth.clients.1.alias"] = "azure-v2"
            map["nav.auth.clients.1.client_id"] = "pleiepenger-livets-sluttfase-prosessering"
            map["nav.auth.clients.1.private_key_jwk"] = ClientCredentials.ClientA.privateKeyJwk
            map["nav.auth.clients.1.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.lagre-dokument"] = "k9-mellomlagring/.default"
            map["nav.auth.scopes.slette-dokument"] = "k9-mellomlagring/.default"
            map["nav.auth.scopes.journalfore"] = "omsorgspenger-joark/.default"
        }

        kafkaEnvironment?.let {
            map["nav.kafka.bootstrap_servers"] = it.bootstrapServers
            map["nav.kafka.auto_offset_reset"] = "earliest"
        }

        return map.toMap()
    }
}
