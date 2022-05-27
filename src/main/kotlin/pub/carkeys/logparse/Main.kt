package pub.carkeys.logparse

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        LogParse(args).process()
    } catch (e: UsageException) {
        println(e.localizedMessage)
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println("Error: ${e.localizedMessage}")
        exitProcess(3)
    }
}
