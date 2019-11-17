package me.avo.ktor.session

import io.ktor.http.CookieEncoding
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.cookiesSession
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

internal class AppKtTest {

    @TestFactory
    fun `session should be valid after login`(): List<DynamicTest> =
        CookieEncoding.values().map {
            DynamicTest.dynamicTest(it.name) {
                testSessionsWithEncoding(it)
            }
        }

    private fun testSessionsWithEncoding(encoding: CookieEncoding) {
        withTestApplication({ module(encoding) }) {
            cookiesSession {
                val loginRequest = handleRequest { uri = "/login" }
                expectThat(loginRequest).succeeded()

                val cookie = loginRequest.response.cookies["id"]!!
                println(cookie)

                val request = handleRequest { uri = "/" }
                expectThat(request).succeeded()
            }
        }
    }

    private fun Assertion.Builder<TestApplicationCall>.succeeded() {
        get { requestHandled }.isTrue()
        get { response.status() }.isEqualTo(HttpStatusCode.OK)
    }
}
