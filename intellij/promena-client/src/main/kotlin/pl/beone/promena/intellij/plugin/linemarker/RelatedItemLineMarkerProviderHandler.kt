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
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors


private val dataDescriptorWithFileParser = DataDescriptorParser()
private val parametersParser = ParametersParser()

private val transformedDataDescriptorSaver = TransformedDataDescriptorSaver()

private val httpConnectorParser = HttpConnectorParser()
private val httpTransformer = HttpTransformer()

fun createOnClickHandler(
    project: Project,
    module: Module,
    getQualifiedClassName: () -> String,
    getMethodName: () -> String,
    getComments: () -> List<String>
): () -> Unit =
    {
        val qualifiedClassName = getQualifiedClassName()
        val methodName = getMethodName()
        val comments = getComments()

        CompilerManager.getInstance(project).make(module) { aborted, errors, _, _ ->
            if (successfulCompilation(aborted, errors)) {
                val startTimestamp = currentTimeMillis()

                val parameters = parametersParser.parse(comments)

                val httpAddress = httpConnectorParser.parseAddress(comments)

                val runToolWindowTabs = createRunToolWindowTabs(project, parameters.repeat).apply {
                    logStart(createTabName(qualifiedClassName, methodName))
                    logParameters(parameters)
                }

                try {
                    val clazz = loadClasses(JavaRelatedItemLineMarkerProvider::class.java.classLoader, module.getOutputFolderFile().path)
                        .createClass(qualifiedClassName)

                    val dataDescriptor = dataDescriptorWithFileParser.parse(comments, clazz)
                        .also(runToolWindowTabs::logData)
                        .also { runToolWindowTabs.newLine() }
                        .map(DataDescriptorWithFile::dataDescriptor)
                        .let(::dataDescriptor)

                    val transformation = clazz.invokePromenaMethod(methodName)

                    val executors = Executors.newFixedThreadPool(parameters.concurrency)
                    try {
                        runToolWindowTabs.map { runToolWindowTab ->
                            executors.submit {
                                transformUsingHttp(runToolWindowTab, transformation, dataDescriptor, httpAddress, startTimestamp)
                            }
                        }
                    } finally {
                        executors.shutdown()
                    }
                } catch (e: Throwable) {
                    runToolWindowTabs.logFailureThrowable(e)
                }
            }
        }
    }

private fun createRunToolWindowTabs(project: Project, number: Int): List<RunToolWindowTab> =
    (0 until number).map { RunToolWindowTab(project) }

private fun successfulCompilation(aborted: Boolean, errors: Int): Boolean =
    !aborted && errors == 0

private fun createTabName(qualifiedClassName: String, methodName: String): String =
    "${qualifiedClassName.split(".").last()}.$methodName"

private fun transformUsingHttp(
    runToolWindowTab: RunToolWindowTab,
    transformation: Transformation,
    dataDescriptor: DataDescriptor,
    httpAddress: String,
    startTimestamp: Long
) {
    httpTransformer.transform(httpAddress, transformationDescriptor(transformation, dataDescriptor))
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
