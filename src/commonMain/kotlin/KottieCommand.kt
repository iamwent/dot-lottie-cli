import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
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
        echo("Find lottie files...\n")
        val lottieFiles = findLottieFiles()
        if (lottieFiles.isEmpty()) {
            echo(Printer.red("✘ No lottie file found."))
            return
        }

        val message = if (lottieFiles.size == 1) "✔ Found 1 JSON file" else "✔ Found ${lottieFiles.size} JSON files"
        echo(Printer.green(message))
        echo(Printer.bold("\nProcessing...\n"))

        val converter = KottieConverter(FileSystem.SYSTEM, client)
        runBlocking {
            lottieFiles.forEach { source ->
                val fileName = "${source.name.dropLast(".json".length)}.lottie"
                val dest = source.parent?.resolve(fileName) ?: fileName.toPath()
                converter.convert(source, dest)
                printConversionResult(source, dest)
            }
        }
    }

    private fun printConversionResult(source: Path, dest: Path) {
        val sourceSize = FileSystem.SYSTEM.metadata(source).size ?: return
        val destSize = FileSystem.SYSTEM.metadata(dest).size ?: return
        val savedSize = (sourceSize - destSize).div(1024)
        val savedPercent = (sourceSize - destSize).toDouble().div(sourceSize).times(100).toInt()
        val message = "${dest.name} done! saved ${savedSize}kb(${savedPercent}%)"
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
}
