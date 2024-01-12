import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class KottieCommand : CliktCommand(
    help = "Convert Lottie JSON to dotLottie format"
) {
    val files: List<String> by argument().multiple()
    val recursive by option("-r", "--recursive", help = "convert directories recursively").flag()

    private val client by lazy { getHttpClient() }

    override fun run() {
        val fileSystem = FileSystem.SYSTEM
        val cookie = readCookie()
        if (cookie.isNullOrEmpty()) {
            echo(Printer.red("✘ Cookie not found."))
            echo(Printer.red("✘ Please config your cookie first: https://github.com/iamwent/kottie"))
            return
        }

        echo("Find lottie files...\n")
        val lottieFiles = findLottieFiles()
        if (lottieFiles.isEmpty()) {
            echo(Printer.red("✘ No lottie file found."))
            return
        }

        val message = if (lottieFiles.size == 1) "✔ Found 1 JSON file" else "✔ Found ${lottieFiles.size} JSON files"
        echo(Printer.green(message))
        echo(Printer.bold("\nProcessing...\n"))

        val converter = KottieConverter(fileSystem, client, cookie)
        runBlocking {
            lottieFiles.map { source ->
                async {
                    val fileName = "${source.name.dropLast(".json".length)}.lottie"
                    val dest = source.parent?.resolve(fileName) ?: fileName.toPath()
                    val result = converter.convert(source, dest)
                    if (result.isSuccess) {
                        result.getOrNull()?.let { printConversionResult(it) }
                    } else {
                        result.exceptionOrNull()?.message?.let { println(it) }
                    }
                }
            }.awaitAll()
        }
    }

    private fun printConversionResult(optimize: Optimize) {
        val savedSize = (optimize.savedSize / 1024.0).toInt()
        val message = "${optimize.dest} done! saved ${savedSize}kb(${optimize.savedPercent}%)"
        echo(Printer.green(message))
    }

    private fun findLottieFiles(): List<Path> {
        return files.toSet()
            .map { it.toPath() }
            .filter { FileSystem.SYSTEM.exists(it) }
            .flatMap {
                return@flatMap if (recursive) {
                    FileSystem.SYSTEM.listRecursively(it, false).toList()
                } else {
                    FileSystem.SYSTEM.list(it)
                }
            }.filter {
                return@filter FileSystem.SYSTEM.metadata(it).isRegularFile
                        && it.name.endsWith(".json", ignoreCase = true)
            }
    }

    private fun readCookie(): String? {
        val system = getSystem()
        val result = system.execute("realpath", COOKIE_PATH)
        val cookie = result.output
            ?.takeIf { system.fileExists(it) }
            ?.let { system.readFile(it) }
        return cookie
    }

    companion object {
        private const val COOKIE_PATH = "~/.kottie/cookie"
    }
}
