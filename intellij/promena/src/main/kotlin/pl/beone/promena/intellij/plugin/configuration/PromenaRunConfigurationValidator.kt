package pl.beone.promena.intellij.plugin.configuration

object PromenaRunConfigurationValidator {

    object Host {
        fun validate(value: String): Boolean =
            value.isNotBlank()
    }

    object Port {
        val RANGE = 1..65535

        fun validate(value: Int): Boolean =
            RANGE.contains(value)
    }

    object Repeat {
        val RANGE = 1..1000

        fun validate(value: Int): Boolean =
            RANGE.contains(value)
    }

    object Concurrency {
        val RANGE = 1..100

        fun validate(value: Int): Boolean =
            RANGE.contains(value)
    }
}