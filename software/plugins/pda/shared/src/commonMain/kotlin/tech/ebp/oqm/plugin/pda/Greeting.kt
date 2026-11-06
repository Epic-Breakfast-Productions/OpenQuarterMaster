package tech.ebp.oqm.plugin.pda

class Greeting {
	private val platform = getPlatform()

	fun greet(): String {
		return sayHello(platform.name)
	}
}
