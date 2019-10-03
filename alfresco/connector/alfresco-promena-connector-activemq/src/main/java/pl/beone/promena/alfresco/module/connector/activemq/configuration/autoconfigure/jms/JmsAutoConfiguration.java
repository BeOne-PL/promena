/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.beone.promena.alfresco.module.connector.activemq.configuration.autoconfigure.jms;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import pl.beone.promena.alfresco.module.connector.activemq.configuration.context.properties.PropertyMapper;

import javax.jms.ConnectionFactory;
import java.time.Duration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring JMS.
 *
 * @author Greg Turnquist
 * @author Stephane Nicoll
 */
@Configuration
@Import(JmsAnnotationDrivenConfiguration.class)
public class JmsAutoConfiguration {

    @Configuration
    protected static class JmsTemplateConfiguration {

        private final JmsProperties properties;

        private final ObjectProvider<DestinationResolver> destinationResolver;

        private final ObjectProvider<MessageConverter> messageConverter;

        public JmsTemplateConfiguration(JmsProperties properties,
                                        ObjectProvider<DestinationResolver> destinationResolver,
                                        ObjectProvider<MessageConverter> messageConverter) {
            this.properties = properties;
            this.destinationResolver = destinationResolver;
            this.messageConverter = messageConverter;
        }

        @Bean
        public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
            PropertyMapper map = PropertyMapper.get();
            JmsTemplate template = new JmsTemplate(connectionFactory);
            template.setPubSubDomain(this.properties.isPubSubDomain());
            map.from(this.destinationResolver::getIfUnique).whenNonNull()
                    .to(template::setDestinationResolver);
            map.from(this.messageConverter::getIfUnique).whenNonNull()
                    .to(template::setMessageConverter);
            mapTemplateProperties(this.properties.getTemplate(), template);
            return template;
        }

        private void mapTemplateProperties(JmsProperties.Template properties, JmsTemplate template) {
            PropertyMapper map = PropertyMapper.get();
            map.from(properties::getDefaultDestination).whenNonNull()
                    .to(template::setDefaultDestinationName);
            map.from(properties::getDeliveryDelay).whenNonNull().as(Duration::toMillis)
                    .to(template::setDeliveryDelay);
            map.from(properties::determineQosEnabled).to(template::setExplicitQosEnabled);
            map.from(properties::getDeliveryMode).whenNonNull().as(JmsProperties.DeliveryMode::getValue)
                    .to(template::setDeliveryMode);
            map.from(properties::getPriority).whenNonNull().to(template::setPriority);
            map.from(properties::getTimeToLive).whenNonNull().as(Duration::toMillis)
                    .to(template::setTimeToLive);
            map.from(properties::getReceiveTimeout).whenNonNull().as(Duration::toMillis)
                    .to(template::setReceiveTimeout);
        }

    }

    @Configuration
    @Import(JmsTemplateConfiguration.class)
    protected static class MessagingTemplateConfiguration {

        @Bean
        public JmsMessagingTemplate jmsMessagingTemplate(JmsTemplate jmsTemplate) {
            return new JmsMessagingTemplate(jmsTemplate);
        }

    }

}
