package no.nav.helse.spapi

import com.auth0.jwk.JwkProvider
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

internal open class Konsument(
    private val navn: String,
    internal val organisasjonsnummer: Organisasjonsnummer,
    private val id: String,
    private val scope: String,
    internal val behandlingsnummer: String,
    internal val behandlingsgrunnlag: Behandlingsgrunnlag
) {
    override fun toString() = navn

    internal fun setupAuthentication(authenticationConfig: AuthenticationConfig, maskinportenJwkProvider: JwkProvider, maskinportenIssuer: String, audience: String) {
        authenticationConfig.jwt(id) {
            verifier(maskinportenJwkProvider, maskinportenIssuer) {
                withAudience(audience)
                withClaim("scope", scope)
            }
            validate { credentials -> JWTPrincipal(credentials.payload) }
            challenge { _, _ -> call.respondChallenge() }
        }
    }

    internal fun setupApi(routing: Routing, block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit) {
        routing.authenticate(id) {
            post("/$id") {
                block(this)
            }
        }
    }
}

internal object FellesordningenForAfp: Konsument(
    navn = "Fellesordningen for AFP",
    organisasjonsnummer = Organisasjonsnummer("987414502"),
    id = "fellesordningen-for-afp",
    scope = "nav:sykepenger:fellesordningenforafp.read",
    behandlingsnummer = "B709",
    behandlingsgrunnlag = Behandlingsgrunnlag("GDPR Art. 6(1)e, 9(2)b. AFP-tilskottsloven §17 første ledd, §29 andre ledd, første punktum")
)

