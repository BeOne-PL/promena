package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.intellij.plugin.classloader.createClass
import pl.beone.promena.intellij.plugin.classloader.invokePromenaMethod
import pl.beone.promena.intellij.plugin.classloader.loadClasses
import pl.beone.promena.intellij.plugin.common.getOutputFolderFile
import pl.beone.promena.intellij.plugin.common.invokeLater
import pl.beone.promena.intellij.plugin.parser.HttpConnectorParser
import pl.beone.promena.intellij.plugin.parser.datadescriptor.DataDescriptorParser
import pl.beone.promena.intellij.plugin.parser.datadescriptor.DataDescriptorWithFile
import pl.beone.promena.intellij.plugin.parser.parameter.ParametersParser
import pl.beone.promena.intellij.plugin.saver.TransformedDataDescriptorSaver
import pl.beone.promena.intellij.plugin.toolwindow.*
import pl.beone.promena.intellij.plugin.transformer.HttpTransformer
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.lang.System.currentTimeMillis

private val dataDescriptorWithFileParser = DataDescriptorParser()
private val parametersParser = ParametersParser()

private val transformedDataDescriptorSaver = TransformedDataDescriptorSaver()

private val httpConnectorParser = HttpConnectorParser()
private val httpTransformer = HttpTransformer()

fun createOnClickHandler(
    project: Project,
    module: Module,
    qualifiedClassName: String,
    methodName: String,
    getComments: () -> List<String>
): () -> Unit =
    {
        val comments = getComments()

        CompilerManager.getInstance(project).make(module) { aborted, errors, _, _ ->
            val startTimestamp = currentTimeMillis()

            val runToolWindowTab = RunToolWindowTab(project).also {
                it.logStart(createTabName(qualifiedClassName, methodName))
            }

            try {
                if (aborted || errors > 0) {
                    runToolWindowTab.logFailureCompilationError()
                } else {
                    val clazz = loadClasses(JavaRelatedItemLineMarkerProvider::class.java.classLoader, module.getOutputFolderFile().path)
                        .createClass(qualifiedClassName)

                    val parameters = parametersParser.parse(comments)
                        .also { runToolWindowTab.logParameters(it) }

                    val dataDescriptor = dataDescriptorWithFileParser.parse(comments, clazz)
                        .also { runToolWindowTab.logData(it) }
                        .also { runToolWindowTab.println() }
                        .map(DataDescriptorWithFile::dataDescriptor)
                        .let(::dataDescriptor)

                    val transformation = clazz.invokePromenaMethod(methodName)
                    httpTransformer.transform(httpConnectorParser.parseAddress(comments), transformationDescriptor(transformation, dataDescriptor))
                        .subscribe(
                            { (_, transformedDataDescriptor) ->
                                handleSuccessfulTransformation(
                                    runToolWindowTab,
                                    transformation,
                                    transformedDataDescriptor,
                                    currentTimeMillis() - startTimestamp
                                )
                            },
                            { exception -> handleFailedTransformation(runToolWindowTab, exception) }
                        )
                }
            } catch (e: Throwable) {
                runToolWindowTab.logFailureThrowable(e)
            }
        }
    }

private fun createTabName(qualifiedClassName: String, methodName: String): String =
    "${qualifiedClassName.split(".").last()}.$methodName"

private fun handleSuccessfulTransformation(
    runToolWindowTab: RunToolWindowTab,
    transformation: Transformation,
    transformedDataDescriptor: TransformedDataDescriptor,
    executionTimeMillis: Long
) {
    val targetMediaType = transformation.transformers.last().targetMediaType
    invokeLater {
        transformedDataDescriptorSaver.save(transformedDataDescriptor, targetMediaType)
            .also { runToolWindowTab.logSuccess(transformedDataDescriptor, targetMediaType, it, executionTimeMillis) }
    }
}

private fun handleFailedTransformation(runToolWindowTab: RunToolWindowTab, exception: Throwable) {
    invokeLater {
        runToolWindowTab.logFailureThrowable(exception)
    }
}
