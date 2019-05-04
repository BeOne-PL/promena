package pl.beone.promena.transformer.sillytransformer.external

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.lib.dockertestrunner.external.DockerTestRunner

@RunWith(DockerTestRunner::class)
class SillyTransformerTest {

    @Test
    fun canTransform() {
        Assert.assertEquals(1, 2)
    }

    @Test
    fun canTransform1() {
        Assert.assertEquals(1, 1)
    }


//    @Test
//    fun canTransform1() {
//        Assert.assertEquals(3, 4)
//    }

}