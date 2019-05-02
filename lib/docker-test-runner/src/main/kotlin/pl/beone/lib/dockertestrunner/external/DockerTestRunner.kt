package pl.beone.lib.dockertestrunner.external

import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod

class DockerTestRunner(testClass: Class<*>) : BlockJUnit4ClassRunner(testClass) {

    private lateinit var testContainer: TestContainer

    override fun run(notifier: RunNotifier) {

        try {
            runOnHost {
                testContainer = TestContainer(testClass.name).apply {
                    start()
                }
            }

            super.run(notifier)
        } finally {
            runOnHost {
                testContainer.stop()
            }
        }
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        runOnDocker {
            super.runChild(method, notifier)
        }

        runOnHost {
            val description = describeChild(method)

            try {
                notifier.fireTestStarted(description)

                testContainer.runTest(method)
            } catch (e: Throwable) {
                notifier.fireTestFailure(Failure(description, e))
            } finally {
                notifier.fireTestFinished(description)
            }
        }
    }
}