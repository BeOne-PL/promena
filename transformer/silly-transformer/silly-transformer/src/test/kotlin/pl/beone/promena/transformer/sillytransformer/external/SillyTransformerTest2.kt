package pl.beone.promena.transformer.sillytransformer.external

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class SillyTransformerTest2 {

    @Test
    fun canTransform() {
        Assert.assertEquals(1, 2)
    }


    @Test
    fun canTransform1() {
        Assert.assertEquals(3, 4)
    }

}