#macro(stringWithLowFiristCharacter $str)$str.substring(0,1).toLowerCase()$str.substring(1)#end
#set($transformerClassNameWithLowFirstChar = "#stringWithLowFiristCharacter($transformerClassName)")
package ${package}.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ${package}.${transformerClassName}

@Configuration
class ${transformerClassName}Context {

    @Bean
    fun ${transformerClassNameWithLowFirstChar}() = ${transformerClassName}()

}