//package pl.beone.promena.alfresco.module.connector.http.external
//
//import io.kotlintest.shouldBe
//import io.kotlintest.shouldThrow
//import io.mockk.*
//import io.netty.handler.codec.http.HttpResponseStatus
//import org.alfresco.service.cmr.repository.NodeRef
//import org.alfresco.service.cmr.repository.StoreRef
//import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.reactivestreams.Publisher
//import pl.beone.promena.lib.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
//import pl.beone.lib.typeconverter.internal.getClazz
//import pl.beone.promena.alfresco.module.core.applicationmodel.node.plus
//import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
//import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
//import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
//import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
//import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
//import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
//import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
//import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
//import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
//import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
//import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager
//import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
//import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
//import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
//import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
//import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
//import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
//import pl.beone.promena.core.contract.serialization.SerializationService
//import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
//import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
//import pl.beone.promena.transformer.contract.data.singleDataDescriptor
//import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
//import pl.beone.promena.transformer.contract.transformation.singleTransformation
//import pl.beone.promena.transformer.internal.model.data.toMemoryData
//import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
//import pl.beone.promena.transformer.internal.model.metadata.plus
//import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
//import pl.beone.promena.transformer.internal.model.parameters.plus
//import reactor.core.publisher.Mono
//import reactor.netty.DisposableServer
//import reactor.netty.http.client.HttpClient
//import reactor.netty.http.server.HttpServer
//import reactor.netty.http.server.HttpServerRequest
//import reactor.netty.http.server.HttpServerResponse
//import reactor.test.StepVerifier
//import java.time.Duration
//
//class HttpPromenaTransformerTest {
//
//    companion object {
//        private val threads = 1
//
//
//        private val externalCommunicationParameters = memoryCommunicationParameters()
//
//        private val transformationExecution = transformationExecution("1")
//
//        private val transformation = singleTransformation("transformer-test", MediaTypeConstants.APPLICATION_PDF, emptyParameters() + ("key" to "value"))
//        private val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value"))
//        private val transformationDescriptor = transformationDescriptor(
//            transformation,
//            dataDescriptor,
//            externalCommunicationParameters
//        )
//
//        private val nodeDescriptor =
//            NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c").toSingleNodeDescriptor(emptyMetadata() + ("key" to "value"))
//        private val nodeRefs = nodeDescriptor.toNodeRefs()
//        private val postTransformationExecution = mockk<PostTransformationExecutor>()
//        private val retry = customRetry(3, Duration.ofMillis(1000))
//        private const val nodesChecksum = "123456789"
//        private const val userName = "admin"
////        private val transformationParameters = TransformationParameters(
////            transformation,
////            nodeDescriptor,
////            null,
////            customRetry(3, Duration.ofMillis(1000)),
////            singleDataDescriptor("".toMemoryData(), MediaTypeConstants.APPLICATION_PDF, emptyMetadata()),
////            nodesChecksum,
////            0,
////            userName
////        )
//
//        private val performedTransformationDescriptor = performedTransformationDescriptor(
//            singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value"))
//        )
//        private val transformationExecutionResult = transformationExecutionResult(NodeRef("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff"))
//    }
//
//    private lateinit var promenaMutableTransformationManager: PromenaTransformationManager.PromenaMutableTransformationManager
//    private lateinit var postTransformationExecutorValidator: PostTransformationExecutorValidator
//    private lateinit var nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier
//    private lateinit var nodesChecksumGenerator: NodesChecksumGenerator
//    private lateinit var dataDescriptorGetter: DataDescriptorGetter
//    private lateinit var authorizationService: AuthorizationService
//
//    @Before
//    fun setUp() {
//        promenaMutableTransformationManager = mockk {
//            every { startTransformation() } returns transformationExecution
//        }
//        postTransformationExecutorValidator = mockk {
//            every { validate(any()) } just Runs
//        }
//        nodeInCurrentTransactionVerifier = mockk {
//            every { verify(nodeRefs[0]) } just Runs
//        }
//        nodesChecksumGenerator = mockk {
//            every { generate(nodeRefs) } returns nodesChecksum
//        }
//        dataDescriptorGetter = mockk {
//            every { get(nodeDescriptor) } returns dataDescriptor
//        }
//        authorizationService = mockk {
//            every { getCurrentUser() } returns userName
//        }
//    }
//
////    @After
////    fun stopServer() {
////        httpServer.disposeNow()
////    }
//
//    @Test
//    fun name() {
//        HttpPromenaTransformer(
//            threads,
//            "localhost:8080",
//            externalCommunicationParameters,
//            promenaMutableTransformationManager,
//            retry,
//            postTransformationExecutorValidator,
//            nodeInCurrentTransactionVerifier,
//            nodesChecksumGenerator,
//            mockk(),
//            dataDescriptorGetter,
//            mockk(),
//            mockk()
//
//        )
//    }
//}