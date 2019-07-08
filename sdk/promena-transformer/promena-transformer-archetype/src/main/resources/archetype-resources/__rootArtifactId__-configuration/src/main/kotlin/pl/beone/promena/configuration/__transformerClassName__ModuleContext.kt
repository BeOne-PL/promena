#macro( ccase $str )
#foreach( $word in $str.split('-') )$word.substring(0,1).toUpperCase()$word.substring(1)#end#end
#set( $classNamePrefix = "#ccase( $artifactId )" )
package pl.beone.promena.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan(basePackages = [
    "${package}.configuration"
])
@PropertySource("classpath:${rootArtifactId}-transformer.properties")
class ${transformerClassName}ModuleContext