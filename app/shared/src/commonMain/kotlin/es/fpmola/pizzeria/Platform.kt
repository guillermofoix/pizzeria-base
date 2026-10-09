package es.fpmola.pizzeria

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform