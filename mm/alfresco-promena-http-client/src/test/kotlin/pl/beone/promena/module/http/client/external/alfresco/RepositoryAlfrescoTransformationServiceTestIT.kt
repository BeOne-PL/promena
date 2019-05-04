package pl.beone.promena.module.http.client.external.alfresco

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.alfresco.model.ContentModel
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef
import org.alfresco.service.namespace.QName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.lib.http.client.applicationmodel.exception.TransformationValidationException
import pl.beone.promena.lib.http.client.contract.transformation.TransformationServerService
import pl.beone.promena.lib.http.client.internal.transformation.DefaultTransformationServerService
import pl.beone.promena.module.http.client.applicationmodel.descriptor.NodeDescriptor
import pl.beone.promena.module.http.client.contract.alfresco.AlfrescoDataConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.time.LocalDateTime

@RunWith(AlfrescoTestRunner::class)
class RepositoryAlfrescoTransformationServiceTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun transformToNodes() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("0".toByteArray()), MediaTypeConstants.TEXT_PLAIN),
                                 DataDescriptor(InMemoryData("1".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.APPLICATION_JSON,
                          MapParameters(mapOf("testKey" to "testValue")),
                          3000)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData(""" {"value":"0"} """.trim().toByteArray()),
                                              MapMetadata.empty()),
                    TransformedDataDescriptor(InMemoryData(""" {"value":"1"} """.trim().toByteArray()),
                                              MapMetadata.empty())
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(2).apply {
                this.mapIndexed { index, nodeRef ->
                    nodeRef.saveContent(MediaTypeConstants.TEXT_PLAIN, index.toString())
                }
            }
            val targetNodeRefs = integrationTestsFolderNodeRef.createNodes(2)

            getRepositoryAlfrescoTransformationService().transformToNodes(
                    "test",
                    nodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    targetNodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    MediaTypeConstants.APPLICATION_JSON,
                    MapParameters(mapOf("testKey" to "testValue")),
                    3000
            )

            assertThat(targetNodeRefs).hasSize(2)
            assertThat(targetNodeRefs[0].readContent()).isEqualTo(""" {"value":"0"} """.trim().toByteArray())
            assertThat(targetNodeRefs[0].getMimeType()).isEqualTo(MediaTypeConstants.APPLICATION_JSON.mimeType)
            assertThat(targetNodeRefs[1].readContent()).isEqualTo(""" {"value":"1"} """.trim().toByteArray())
            assertThat(targetNodeRefs[1].getMimeType()).isEqualTo(MediaTypeConstants.APPLICATION_JSON.mimeType)
        }
    }

    @Test
    fun transformToNodes_saveAlfrescoMetadata() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.TEXT_PLAIN,
                          MapParameters.empty(),
                          0)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData("test".trim().toByteArray()),
                                              MapMetadata(mapOf(
                                                      "testKeyString" to "testValue",
                                                      "testKeyInt" to 2,
                                                      "alf_text" to "text",
                                                      "alf_int" to 5,
                                                      "alf_date" to LocalDateTime.of(1993, 12, 17, 12, 13),
                                                      "alf_boolean" to true,
                                                      "alf_double" to 10.2
                                              )))
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetNodeRefs = integrationTestsFolderNodeRef.createNodes(1)

            getRepositoryAlfrescoTransformationService().transformToNodes(
                    "test",
                    nodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    targetNodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    MediaTypeConstants.TEXT_PLAIN,
                    MapParameters.empty(),
                    0
            )

            assertThat(targetNodeRefs).hasSize(1)
            assertThat(targetNodeRefs[0].readContent()).isEqualTo("test".trim().toByteArray())
            assertThat(targetNodeRefs[0].getMimeType()).isEqualTo(MediaTypeConstants.TEXT_PLAIN.mimeType)
            assertThat(targetNodeRefs[0].getProperties())
                    .doesNotContainKey(QName.createQName("testKeyString"))
                    .doesNotContainKey(QName.createQName("testKeyInt"))
                    .containsEntry(QName.createQName("text"), "text")
                    .containsEntry(QName.createQName("int"), 5)
                    .containsEntry(QName.createQName("date"), LocalDateTime.of(1993, 12, 17, 12, 13))
                    .containsEntry(QName.createQName("boolean"), true)
                    .containsEntry(QName.createQName("double"), 10.2)
        }
    }

    @Test
    fun transformToNodes_folderType() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.APPLICATION_XML,
                          MapParameters.empty(),
                          0)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData("<tag>test</tag>".trim().toByteArray()),
                                              MapMetadata.empty())
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1, ContentModel.TYPE_FOLDER).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetNodeRefs = integrationTestsFolderNodeRef.createNodes(1, ContentModel.TYPE_FOLDER)

            getRepositoryAlfrescoTransformationService().transformToNodes(
                    "test",
                    nodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    targetNodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    MediaTypeConstants.APPLICATION_XML,
                    MapParameters.empty(),
                    0
            )

            assertThat(targetNodeRefs).hasSize(1)
            assertThat(targetNodeRefs[0].readContent()).isEqualTo("<tag>test</tag>".trim().toByteArray())
            assertThat(targetNodeRefs[0].getMimeType()).isEqualTo(MediaTypeConstants.APPLICATION_XML.mimeType)
        }
    }

    @Test
    fun transformToNodes_differentNumberOfTargetNodesAndTransformedNodes_shouldThrowTransformationValidationException() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.TEXT_PLAIN,
                          MapParameters.empty(),
                          0)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData("".toByteArray()), MapMetadata.empty()),
                    TransformedDataDescriptor(InMemoryData("".toByteArray()), MapMetadata.empty())
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "") }
            }
            val targetNodeRefs = integrationTestsFolderNodeRef.createNodes(1)

            assertThatThrownBy {
                getRepositoryAlfrescoTransformationService().transformToNodes(
                        "test",
                        nodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        targetNodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        MediaTypeConstants.TEXT_PLAIN,
                        MapParameters.empty(),
                        0
                )
            }
                    .isExactlyInstanceOf(TransformationValidationException::class.java)
                    .hasMessage("You passed <1> target nodes but transformation returned <2>")
        }
    }

    @Test
    fun transformToNodes_nodeRefDoesNotExist_shouldThrowTransformationValidationException() {
        mockBeans(mock(), AlfrescoFileDataConverter(null)) {
            val nodeRefs =
                    listOf(NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "00000000-0000-0000-0000-000000000000"))

            assertThatThrownBy {
                getRepositoryAlfrescoTransformationService().transformToNodes(
                        "test",
                        nodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        emptyList(),
                        MediaTypeConstants.TEXT_PLAIN,
                        MapParameters.empty(),
                        3000
                )
            }
                    .isExactlyInstanceOf(TransformationValidationException::class.java)
                    .hasMessage("Node <workspace://SpacesStore/00000000-0000-0000-0000-000000000000> doesn't exist")
        }
    }

    @Test
    fun transformToNodes_targetNodeRefDoesNotExist_shouldThrowTransformationValidationException() {
        mockBeans(mock(), AlfrescoFileDataConverter(null)) {
            val targetNodeRefs =
                    listOf(NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "00000000-0000-0000-0000-000000000000"))

            assertThatThrownBy {
                getRepositoryAlfrescoTransformationService().transformToNodes(
                        "test",
                        emptyList(),
                        targetNodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        MediaTypeConstants.TEXT_PLAIN,
                        MapParameters.empty(),
                        3000
                )
            }
                    .isExactlyInstanceOf(TransformationValidationException::class.java)
                    .hasMessage("Target node <workspace://SpacesStore/00000000-0000-0000-0000-000000000000> doesn't exist")
        }
    }

    @Test
    fun transformToFolder() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.TEXT_PLAIN,
                          MapParameters.empty(),
                          null)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData("test".toByteArray()),
                                              MapMetadata(mapOf("alf_key" to "value"))),
                    TransformedDataDescriptor(InMemoryData("test2".toByteArray()),
                                              MapMetadata(mapOf("alf_key" to "value2")))
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetFolderNodeRef = integrationTestsFolderNodeRef.createFolder()

            val transformedNodeRefs = getRepositoryAlfrescoTransformationService().transformToFolder(
                    "test",
                    nodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    targetFolderNodeRef,
                    MediaTypeConstants.TEXT_PLAIN,
                    MapParameters.empty(),
                    null,
                    null,
                    null,
                    null
            )

            assertThat(targetFolderNodeRef.getNodeRefs()).hasSize(2)
            transformedNodeRefs.sortedBy { it.getProperty(ContentModel.PROP_NAME).toString() }.let {
                assertThat(it).hasSize(2)

                assertThat(it[0].getProperties()).containsEntry(QName.createQName("key"), "value")
                assertThat(it[0].readContent()).isEqualTo("test".toByteArray())
                assertThat(it[0].getProperty(ContentModel.PROP_NAME).toString())
                        .matches("""Result [(]test[)] [\[]\d{4}-\d{2}-\d{2} \d{2}_\d{2}_\d{2}[]] - 0""".toRegex().toPattern())

                assertThat(it[1].getProperties()).containsEntry(QName.createQName("key"), "value2")
                assertThat(it[1].readContent()).isEqualTo("test2".toByteArray())
                assertThat(it[1].getProperty(ContentModel.PROP_NAME).toString())
                        .matches("""Result [(]test[)] [\[]\d{4}-\d{2}-\d{2} \d{2}_\d{2}_\d{2}[]] - 1""".toRegex().toPattern())
            }
        }
    }

    @Test
    fun transformToFolder_folderAsTargetType() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.TEXT_PLAIN,
                          MapParameters.empty(),
                          null)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata.empty())
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetFolderNodeRef = integrationTestsFolderNodeRef.createFolder()

            val transformedNodeRefs = getRepositoryAlfrescoTransformationService().transformToFolder(
                    "test",
                    nodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    targetFolderNodeRef,
                    MediaTypeConstants.TEXT_PLAIN,
                    MapParameters.empty(),
                    null,
                    ContentModel.TYPE_FOLDER,
                    null,
                    null
            )

            transformedNodeRefs.let {
                assertThat(it).hasSize(1)

                assertThat(it[0].readContent()).isEqualTo("test".toByteArray())
                assertThat(it[0].getType()).isEqualTo(ContentModel.TYPE_FOLDER)
            }
        }
    }

    @Test
    fun transformToFolder_namePattern() {
        val mockedTransformationExecutor = mock<TransformationServerService> {
            on {
                transform("test",
                          listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                          MediaTypeConstants.TEXT_PLAIN,
                          MapParameters.empty(),
                          null)
            } doReturn listOf(
                    TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata.empty())
            )
        }

        mockBeans(mockedTransformationExecutor,
                  AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetFolderNodeRef = integrationTestsFolderNodeRef.createFolder()

            val transformedNodeRefs = getRepositoryAlfrescoTransformationService().transformToFolder(
                    "test",
                    nodeRefs.map {
                        NodeDescriptor(it,
                                                                                                       ContentModel.PROP_CONTENT)
                    },
                    targetFolderNodeRef,
                    MediaTypeConstants.TEXT_PLAIN,
                    MapParameters.empty(),
                    null,
                    null,
                    null,
                    "test \${index}"
            )

            transformedNodeRefs.let {
                assertThat(it).hasSize(1)

                assertThat(it[0].getProperty(ContentModel.PROP_NAME).toString())
                        .isEqualTo("test 0")
            }
        }
    }

    @Test
    fun transformToFolder_namePatternWithoutIndex_shouldThrowTransformationValidationException() {
        mockBeans(mock(), AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetFolderNodeRef = integrationTestsFolderNodeRef.createFolder()

            assertThatThrownBy {
                getRepositoryAlfrescoTransformationService().transformToFolder(
                        "test",
                        nodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        targetFolderNodeRef,
                        MediaTypeConstants.TEXT_PLAIN,
                        MapParameters.empty(),
                        null,
                        null,
                        null,
                        "test"
                )
            }
                    .isExactlyInstanceOf(TransformationValidationException::class.java)
                    .hasMessage("Name pattern <test> doesn't contain <\${index}>")
        }
    }

    @Test
    fun transformToFolder_targetFolderDoesNotExist_shouldThrowTransformationValidationException() {
        mockBeans(mock(), AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetFolderNodeRef =
                    NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "00000000-0000-0000-0000-000000000000")

            assertThatThrownBy {
                getRepositoryAlfrescoTransformationService().transformToFolder(
                        "test",
                        nodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        targetFolderNodeRef,
                        MediaTypeConstants.TEXT_PLAIN,
                        MapParameters.empty(),
                        null,
                        null,
                        null,
                        null
                )
            }
                    .isExactlyInstanceOf(TransformationValidationException::class.java)
                    .hasMessage("Folder node <$targetFolderNodeRef> doesn't exist")
        }
    }

    @Test
    fun transformToFolder_targetNodeIsNotFolder_shouldThrowTransformationValidationException() {
        mockBeans(mock(), AlfrescoFileDataConverter(null)) {
            val integrationTestsFolderNodeRef = createOrGetIntegrationTestsFolder()

            val nodeRefs = integrationTestsFolderNodeRef.createNodes(1).apply {
                this.forEach { it.saveContent(MediaTypeConstants.TEXT_PLAIN, "test") }
            }
            val targetFolderNodeRef = integrationTestsFolderNodeRef.createNode()

            assertThatThrownBy {
                getRepositoryAlfrescoTransformationService().transformToFolder(
                        "test",
                        nodeRefs.map {
                            NodeDescriptor(it,
                                                                                                           ContentModel.PROP_CONTENT)
                        },
                        targetFolderNodeRef,
                        MediaTypeConstants.TEXT_PLAIN,
                        MapParameters.empty(),
                        null,
                        null,
                        null,
                        null
                )
            }
                    .isExactlyInstanceOf(TransformationValidationException::class.java)
                    .hasMessage("Node <$targetFolderNodeRef> isn't folder")
        }
    }

    private fun getRepositoryAlfrescoTransformationService() =
            applicationContext.getBean("repositoryAlfrescoTransformationService") as RepositoryAlfrescoTransformationService

    private fun mockBeans(mockedTransformationServerService: TransformationServerService,
                          alfrescoDataConverter: AlfrescoDataConverter,
                          toRun: () -> Unit) {
        val repositoryAlfrescoTransformationService =
                getRepositoryAlfrescoTransformationService()

        val defaultTransformationServerService =
                applicationContext.getBean("defaultTransformationServerService") as DefaultTransformationServerService
        val transformationServerServiceField =
                repositoryAlfrescoTransformationService.javaClass.getDeclaredField("transformationServerService")
                        .apply { isAccessible = true }

        val alfrescoFileDataConverter =
                applicationContext.getBean("alfrescoFileDataConverter") as AlfrescoFileDataConverter
        val alfrescoDataConverterField =
                repositoryAlfrescoTransformationService.javaClass.getDeclaredField("alfrescoDataConverter")
                        .apply { isAccessible = true }

        try {
            transformationServerServiceField.set(repositoryAlfrescoTransformationService,
                                                 mockedTransformationServerService)
            alfrescoDataConverterField.set(repositoryAlfrescoTransformationService,
                                           alfrescoDataConverter)

            toRun()
        } finally {
            transformationServerServiceField.set(repositoryAlfrescoTransformationService,
                                                 defaultTransformationServerService)
            transformationServerServiceField.isAccessible = false

            alfrescoDataConverterField.set(repositoryAlfrescoTransformationService,
                                           alfrescoFileDataConverter)
            alfrescoDataConverterField.isAccessible = false
        }
    }
}