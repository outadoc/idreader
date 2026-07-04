package fr.outadoc.eidas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform