package fr.gouv.monprojetsup.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    lateinit var authServerUrl: String

    companion object {
        private const val OAUTH_SCHEME_NAME: String = "keycloak"
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(Components().addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme()))
            .addSecurityItem(SecurityRequirement().addList(OAUTH_SCHEME_NAME))
            .info(
                Info().title("MonProjetSup API")
                    .description("API du frontend MonProjetSup")
                    .version("1.0"),
            )
    }

    private fun createOAuthScheme(): SecurityScheme {
        val flows = createOAuthFlows()
        return SecurityScheme().type(SecurityScheme.Type.OAUTH2).flows(flows)
    }

    private fun createOAuthFlows(): OAuthFlows {
        val flow = createAuthorizationCodeFlow()
        return OAuthFlows().authorizationCode(flow)
    }

    private fun createAuthorizationCodeFlow(): OAuthFlow {
        return OAuthFlow()
            .authorizationUrl("$authServerUrl/protocol/openid-connect/auth")
            .tokenUrl("$authServerUrl/protocol/openid-connect/token")
    }
}
