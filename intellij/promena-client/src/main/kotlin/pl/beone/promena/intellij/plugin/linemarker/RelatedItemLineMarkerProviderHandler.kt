package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.intellij.plugin.classloader.createClass
import pl.beone.promena.intellij.plugin.classloader.invokePromenaMethod
import pl.beone.promena.intellij.plugin.classloader.loadClasses
import pl.beone.promena.intellij.plugin.common.getOutputFolderFile
import pl.beone.promena.intellij.plugin.connector.HttpConnectorTransformer
import pl.beone.promena.intellij.plugin.parser.HttpConnectorParser
import pl.beone.promena.intellij.plugin.parser.datadescriptor.DataDescriptorParser
import pl.beone.promena.intellij.plugin.parser.datadescriptor.DataDescriptorWithFile
import pl.beone.promena.intellij.plugin.parser.parameter.ParametersParser
import pl.beone.promena.intellij.plugin.saver.TransformedDataDescriptorSaver
import pl.beone.promena.intellij.plugin.toolwindow.*
import pl.beone.promena.transformer.contract.data.dataDescriptor

private val dataDescriptorWithFileParser = DataDescriptorParser()
private val parametersParser = ParametersParser()

private val transformedDataDescriptorSaver = TransformedDataDescriptorSaver()

private val httpConnectorParser = HttpConnectorParser()
private val httpConnectorTransformer = HttpConnectorTransformer()

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
                    val transformedDataDescriptor = httpConnectorTransformer.transform(
                        httpConnectorParser.parseAddress(comments),
                        transformationDescriptor(transformation, dataDescriptor)
                    )

                    val targetMediaType = transformation.transformers.last().targetMediaType
                    transformedDataDescriptorSaver.save(transformedDataDescriptor, targetMediaType)
                        .also { runToolWindowTab.logSuccess(transformedDataDescriptor, targetMediaType, it) }
                }
            } catch (e: Throwable) {
                runToolWindowTab.logFailureThrowable(e)
            }
        }
    }

private fun createTabName(qualifiedClassName: String, methodName: String): String =
    "${qualifiedClassName.split(".").last()}.$methodName"