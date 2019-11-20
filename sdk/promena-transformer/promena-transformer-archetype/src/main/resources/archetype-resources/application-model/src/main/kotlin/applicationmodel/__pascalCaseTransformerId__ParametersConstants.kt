package ${package}.applicationmodel

object ${pascalCaseTransformerId}ParametersConstants {

    object Mandatory {
        const val NAME = "mandatory"
        @JvmField
        val CLASS = String::class.java
    }

    object Optional {
        const val NAME = "optional"
        @JvmField
        val CLASS = String::class.java
    }

    object OptionalLimitedValue {
        const val NAME = "optionalLimitedValue"
        @JvmField
        val CLASS = Int::class.java
    }
}