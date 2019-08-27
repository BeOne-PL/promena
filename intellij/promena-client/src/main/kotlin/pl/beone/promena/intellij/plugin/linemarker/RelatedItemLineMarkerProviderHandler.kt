package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import pl.beone.promena.intellij.plugin.classloader.createClass
import pl.beone.promena.intellij.plugin.classloader.getPromenaMethod
import pl.beone.promena.intellij.plugin.classloader.loadClasses
import pl.beone.promena.intellij.plugin.common.getOutputFolderFile
import pl.beone.promena.intellij.plugin.connector.httpTransform
import pl.beone.promena.intellij.plugin.parser.parseDataDescriptor
import pl.beone.promena.intellij.plugin.parser.parseParameters
import pl.beone.promena.intellij.plugin.toolwindow.*
import pl.beone.promena.transformer.contract.transformation.Transformation

internal fun createOnClickHandler(
    project: Project,
    module: Module,
    qualifiedClassName: String,
    methodName: String,
    getComments: () -> List<String>
): () -> Unit =
    {
        val comments = getComments()

        CompilerManager.getInstance(project).make(module) { aborted, errors, _, _ ->
            val runToolWindowTab = RunToolWindowTab(project).also {
                it.logStart(createTabName(qualifiedClassName, methodName))
            }

            try {
                if (aborted || errors > 0) {
                    runToolWindowTab.logFailureCompilationError()
                } else {
                    val clazz = loadClasses(JavaRelatedItemLineMarkerProvider::class.java.classLoader, module.getOutputFolderFile().path)
                        .createClass(qualifiedClassName)

                    clazz
                        .also { runToolWindowTab.logParameters(parseParameters(comments)) }
                        .let { parseDataDescriptor(comments, clazz) }
                        .also { dataDescriptor -> runToolWindowTab.logData(dataDescriptor) }
                        .let { dataDescriptor -> clazz.getPromenaMethod(methodName).invoke(null, dataDescriptor) as Transformation }
                        .let { transformation -> httpTransform("localhost:8080", transformation) }

                    runToolWindowTab.logSuccess()
                }
            } catch (e: Throwable) {
                runToolWindowTab.logFailureException(e)
            }
        }
    }

private fun createTabName(qualifiedClassName: String, methodName: String): String =
    "${qualifiedClassName.split(".").last()}#$methodName"