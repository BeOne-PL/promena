#macro( ccase $str )
#foreach( $word in $str.split('-') )$word.substring(0,1).toUpperCase()$word.substring(1)#end#end
#set( $classNamePrefix = "#ccase( $artifactId )" )
package pl.beone.promena.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration("${rootArtifactId}-context")
@ComponentScan(basePackages = [
    "${package}.configuration"
])
@PropertySource("${rootArtifactId}-transformer.properties")
class ModuleContext