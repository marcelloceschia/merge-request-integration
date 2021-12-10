package net.ntworld.mergeRequestIntegration.provider.gitlab

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.FuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.GraphqlRequest
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class GitlabFuelClient private constructor(
    private val credentials: ApiCredentials
) : FuelClient(credentials) {
    override fun injectAuthentication(httpRequest: Request): Request {
        return httpRequest.header("PRIVATE-TOKEN", credentials.token)
    }

    val baseUrl: String = when (credentials.version) {
        "v4" -> "${credentials.url}/api/v4"
        else -> throw Exception("Not supported")
    }

    val baseProjectUrl: String = when (credentials.version) {
        "v4" -> "${credentials.url}/api/v4/projects/${credentials.projectId}"
        else -> throw Exception("Not supported")
    }

    fun callGraphQL(graphqlRequest: String): String {
        val httpRequest = makeRequestFactory().post("${credentials.url}/api/graphql")
        httpRequest.header("Authorization", "Bearer ${credentials.token}")
        httpRequest.header("Content-Type", "application/json")
        httpRequest.header("Accept", "application/json")
        httpRequest.body(graphqlRequest)
        val (_, response, result) = httpRequest.responseString()
        return when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                throw HttpException(response.statusCode, result.error.message ?: "Unknown")
            }
        }
    }

    fun callGraphQL(query: String, variables: Map<String, String?>): String {
        val graphqlRequest = GraphqlRequest(
            query = query,
            variables = variables
        )
        return this.callGraphQL(json.encodeToString(GraphqlRequest.serializer(), graphqlRequest))
    }

    companion object {
        operator fun <T, R : Response> invoke(
            request: T,
            execute: (GitlabFuelClient.(T) -> R),
            failed: ((Error) -> R)
        ): R where T : net.ntworld.foundation.Request<R>, T : GitlabRequest {
            return try {
                val client = GitlabFuelClient(request.credentials)
                execute.invoke(client, request)
            } catch (exception: HttpException) {
                failed.invoke(
                    GitlabFailedRequestError(
                        exception.message ?: "Failed request",
                        500
                    )
                )
            }
        }

        operator fun <T> invoke(
            credentials: ApiCredentials,
            execute: (GitlabFuelClient.() -> T),
            failed: ((Error) -> T)
        ) : T {
            return try {
                val client = GitlabFuelClient(credentials)
                execute.invoke(client)
            } catch (exception: HttpException) {
                failed.invoke(
                    GitlabFailedRequestError(
                        exception.message ?: "Failed request",
                        500
                    )
                )
            }
        }
    }

}
