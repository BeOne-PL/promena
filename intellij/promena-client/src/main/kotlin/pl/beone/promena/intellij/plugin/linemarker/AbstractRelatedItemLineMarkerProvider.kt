package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.internal.serialization.ThreadUnsafeKryoSerializationService
import pl.beone.promena.intellij.plugin.classloader.createClassLoaderBasedOnFoldersWithCompiledFiles
import pl.beone.promena.intellij.plugin.common.getExistingOutputFolders
import pl.beone.promena.intellij.plugin.common.invokeLater
import pl.beone.promena.intellij.plugin.common.invokePromenaMethod
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

abstract class AbstractRelatedItemLineMarkerProvider {

    companion object {
        private val dataDescriptorWithFileParser = DataDescriptorParser()
        private val parametersParser = ParametersParser()

        private val transformedDataDescriptorSaver = TransformedDataDescriptorSaver()

        private val httpConnectorParser = HttpConnectorParser()
    }

    protected fun createOnClickHandler(
        project: Project,
        getQualifiedClassName: () -> String,
        getMethodName: () -> String,
        getComments: () -> List<String>
    ): () -> Unit =
        {
            val qualifiedClassName = getQualifiedClassName()
            val methodName = getMethodName()
            val comments = getComments()

            CompilerManager.getInstance(project).make(project, project.allModules().toTypedArray()) { aborted, errors, _, _ ->
                if (successfulCompilation(aborted, errors)) {
                    val startTimestamp = currentTimeMillis()

                    val parameters = parametersParser.parse(comments)

                    val httpAddress = httpConnectorParser.parseAddress(comments)

                    val runToolWindowTabs = createRunToolWindowTabs(project, parameters.repeat).apply {
                        logStart(createTabName(qualifiedClassName, methodName))
                        logParameters(parameters)
                    }

                    try {
                        val classLoader = createClassLoaderBasedOnFoldersWithCompiledFiles(this.javaClass.classLoader, project.getExistingOutputFolders())

                        val promenaClass = classLoader
                            .loadClass(qualifiedClassName)

                        val kryoSerializationService = ThreadUnsafeKryoSerializationService(classLoader)

                        val dataDescriptor = dataDescriptorWithFileParser.parse(comments, promenaClass)
                            .also(runToolWindowTabs::logData)
                            .also { runToolWindowTabs.newLine() }
                            .map(DataDescriptorWithFile::dataDescriptor)
                            .let(::dataDescriptor)

                        val transformation = promenaClass.invokePromenaMethod(methodName)

                        val executors = Executors.newFixedThreadPool(parameters.concurrency)
                        try {
                            runToolWindowTabs.map { runToolWindowTab ->
                                executors.submit {
                                    transformUsingHttp(
                                        kryoSerializationService,
                                        runToolWindowTab,
                                        transformation,
                                        dataDescriptor,
                                        httpAddress,
                                        startTimestamp
                                    )
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
        serializationService: SerializationService,
        runToolWindowTab: RunToolWindowTab,
        transformation: Transformation,
        dataDescriptor: DataDescriptor,
        httpAddress: String,
        startTimestamp: Long
    ) {
        HttpTransformer(serializationService).transform(
            httpAddress,
            transformationDescriptor(transformation, dataDescriptor, memoryCommunicationParameters())
        )
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
}