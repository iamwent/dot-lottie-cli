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
        echo("Find lottie files...")
        val lottieFiles = findLottieFiles()
        if (lottieFiles.isEmpty()) {
            echo("No lottie file found!")
            return
        }

        val converter = KottieConverter(FileSystem.SYSTEM, client)
        runBlocking {
            lottieFiles.forEach { source ->
                val fileName = "${source.name.dropLast(".json".length)}.lottie"
                val dest = source.parent?.resolve(fileName) ?: fileName.toPath()
                converter.convert(source, dest)
            }
        }
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
