package tech.ebp.oqm.plugin.pda

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform
