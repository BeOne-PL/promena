package pl.beone.promena.listener.clustermember.configuration.external.akka

import akka.actor.ActorSystem
import akka.actor.Props
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.listener.clustermember.external.akka.MemberClusterListener

@Configuration
class MemberClusterListenerContext {

    @Bean
    fun memberClusterListener(actorSystem: ActorSystem) =
            actorSystem.actorOf(Props.create(MemberClusterListener::class.java) { MemberClusterListener() }, "memberClusterListener")!!
}