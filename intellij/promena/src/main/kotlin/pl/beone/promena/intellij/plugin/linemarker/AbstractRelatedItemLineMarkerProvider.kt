package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.internal.serialization.ClassLoaderKryoSerializationService
import pl.beone.promena.intellij.plugin.applicationmodel.ClassDescriptor
import pl.beone.promena.intellij.plugin.applicationmodel.HttpConfigurationParameters
import pl.beone.promena.intellij.plugin.configuration.PromenaRunConfiguration
import pl.beone.promena.intellij.plugin.dialog.ConfigurationAlreadyExistsDialog
import pl.beone.promena.intellij.plugin.dialog.ConfigurationAlreadyExistsDialog.Result.CREATE_NEW
import pl.beone.promena.intellij.plugin.dialog.ConfigurationAlreadyExistsDialog.Result.USE_SELECTED
import pl.beone.promena.intellij.plugin.extension.*
import pl.beone.promena.intellij.plugin.parser.DataDescriptorParser
import pl.beone.promena.intellij.plugin.parser.DataDescriptorWithFile
import pl.beone.promena.intellij.plugin.saver.TransformedDataDescriptorSaver
import pl.beone.promena.intellij.plugin.toolwindow.*
import pl.beone.promena.intellij.plugin.transformer.HttpPromenaTransformationExecutor
import pl.beone.promena.intellij.plugin.util.createClassLoaderBasedOnFoldersWithCompiledFiles
import pl.beone.promena.intellij.plugin.util.invokeLater
import pl.beone.promena.lib.connector.http.applicationmodel.exception.HttpException
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.lang.System.currentTimeMillis
import java.net.HttpURLConnection.HTTP_NOT_FOUND

internal abstract class AbstractRelatedItemLineMarkerProvider {

    companion object {
        private val kryoLockObject = Object()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    protected fun createOnClickHandler(
        project: Project,
        getComments: () -> List<String>,
        getClassDescriptor: () -> ClassDescriptor
    ): () -> Unit =
        {
            val runManager = RunManager.getInstance(project)
            val runnerAndConfigurationSettings =
                (runManager.getSelectedPromenaRunnerAndConfigurationSettings() ?: showDialogToUseSelectedOrCreateNew(project, runManager))

            try {
                runnerAndConfigurationSettings.checkSettings()

                val classDescriptor = getClassDescriptor()
                val httpConfigurationParameters =
                    createHttpConfigurationParameters(runnerAndConfigurationSettings.configuration as PromenaRunConfiguration)

                CompilerManager.getInstance(project).make(project, project.allModules().toTypedArray()) { aborted, errors, _, _ ->
                    if (successfulCompilation(aborted, errors)) {
                        project.getActiveModule().getCompilerOutputFolder().refresh(true, true) {
                            transform(project, getComments(), classDescriptor, httpConfigurationParameters)
                        }
                    }
                }
            } catch (e: RuntimeConfigurationException) {
                project.editConfiguration(runnerAndConfigurationSettings)
            }
        }

    private fun showDialogToUseSelectedOrCreateNew(project: Project, runManager: RunManager): RunnerAndConfigurationSettings {
        val configurations = runManager.getPromenaRunnerAndConfigurationSettings()
        return if (configurations.isNotEmpty()) {
            val dialog = ConfigurationAlreadyExistsDialog(project, configurations)
                .also(ConfigurationAlreadyExistsDialog::show)

            when (dialog.result) {
                CREATE_NEW -> runManager.createPromenaRunnerAndConfigurationSettings()
                    .also { project.editConfiguration(it) }
                USE_SELECTED -> dialog.selectedConfiguration
            }
        } else {
            runManager.createPromenaRunnerAndConfigurationSettings()
                .also { project.editConfiguration(it) }
        }.also { runManager.selectedConfiguration = it }
    }

    private fun createHttpConfigurationParameters(runConfiguration: PromenaRunConfiguration): HttpConfigurationParameters =
        HttpConfigurationParameters(runConfiguration.host, runConfiguration.port)

    private fun successfulCompilation(aborted: Boolean, errors: Int): Boolean =
        !aborted && errors == 0

    private fun transform(
        project: Project,
        comments: List<String>,
        classDescriptor: ClassDescriptor,
        httpConfigurationParameters: HttpConfigurationParameters
    ) {
        val startTimestamp = currentTimeMillis()

        val runToolWindowTab = RunToolWindowTab(project)
            .also { it.logStart(createTabName(classDescriptor.className, classDescriptor.functionName), httpConfigurationParameters.getAddress()) }

        try {
            val classLoader = createClassLoaderBasedOnFoldersWithCompiledFiles(this.javaClass.classLoader, project.getCompilerOutputFolders())

            val promenaClass = classLoader
                .loadClass(classDescriptor.canonicalClassName)

            val kryoSerializationService = ClassLoaderKryoSerializationService(classLoader, kryoLockObject)

            val dataDescriptor = DataDescriptorParser.parse(comments, promenaClass)
                .also(runToolWindowTab::logData)
                .also { runToolWindowTab.newLine() }
                .map(DataDescriptorWithFile::dataDescriptor)
                .let(::dataDescriptor)

            val transformation = promenaClass.invokePromenaMethod(classDescriptor.functionName)

            coroutineScope.launch {
                try {
                    HttpPromenaTransformationExecutor(kryoSerializationService).transform(
                        transformationDescriptor(transformation, dataDescriptor, memoryCommunicationParameters()),
                        httpConfigurationParameters
                    ).let { handleSuccessfulTransformation(runToolWindowTab, transformation, it.transformedDataDescriptor, startTimestamp) }
                } catch (e: CancellationException) {
                    // deliberately omitted. Thrown exception isn't printed because the tab was closed
                } catch (e: Exception) {
                    handleFailedTransformation(runToolWindowTab, e)
                }
            }.also { runToolWindowTab.onClose { it.cancel(null) } }
        } catch (e: Throwable) {
            handleFailedTransformation(runToolWindowTab, e)
        }
    }

    private fun createTabName(className: String, functionName: String): String =
        "$className.$functionName"

    private fun handleSuccessfulTransformation(
        runToolWindowTab: RunToolWindowTab,
        transformation: Transformation,
        transformedDataDescriptor: TransformedDataDescriptor,
        startTimestamp: Long
    ) {
        val targetMediaType = transformation.transformers.last().targetMediaType
        invokeLater {
            TransformedDataDescriptorSaver.save(transformedDataDescriptor, targetMediaType)
                .also { runToolWindowTab.logSuccess(transformedDataDescriptor, targetMediaType, it, currentTimeMillis() - startTimestamp) }
        }
    }

    private fun handleFailedTransformation(runToolWindowTab: RunToolWindowTab, exception: Throwable) {
        invokeLater {
            if (exception is HttpException && (exception.responseStatus == -1 || exception.responseStatus == HTTP_NOT_FOUND)) {
                runToolWindowTab.logFailureThrowable("Promena not found. Check if the given parameters point to the running server")
            } else {
                runToolWindowTab.logFailureThrowable(determineExceptionString(exception))
            }
        }
    }

    private fun determineExceptionString(e: Throwable): String {
        val exceptionString = e.toFullString()
        return if (exceptionString.contains(DeserializationException::class.java.canonicalName)) {
            "> It's highly likely that <application-model> module isn't available in Promena" +
                    "\n" + exceptionString
        } else {
            exceptionString
        }
    }
}