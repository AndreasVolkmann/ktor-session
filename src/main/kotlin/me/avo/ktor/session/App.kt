package me.avo.ktor.session

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.http.CookieEncoding
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.error
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, 5000) { module(CookieEncoding.BASE64_ENCODING) }
        .start(true)
}

data class MySession(val name: String, val id: Int)

fun Application.module(encoding: CookieEncoding) {
    install(CallLogging) { level = Level.INFO }
    install(StatusPages) {
        exception<Throwable> {
            call.application.environment.log.error(it)
            call.respond(it.message ?: "Error")
        }
    }
    install(Sessions) {
        cookie<MySession>("id", SessionStorageMemory()) {
            cookie.encoding = encoding
        }
    }
    install(Routing) {
        get {
            val session = call.sessions.get<MySession>()
            if (session != null) {
                call.respond(session.id.toString())
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Error")
            }
        }

        get("login") {
            val session = MySession("test", 1)
            call.sessions.set(session)
            call.respond("Logged in")
        }
    }
}
