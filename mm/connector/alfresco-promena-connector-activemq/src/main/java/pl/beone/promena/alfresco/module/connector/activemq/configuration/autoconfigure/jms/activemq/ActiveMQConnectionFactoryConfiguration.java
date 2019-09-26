/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.beone.promena.alfresco.module.connector.activemq.configuration.autoconfigure.jms.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.beone.promena.alfresco.module.connector.activemq.configuration.autoconfigure.jms.JmsProperties;

import javax.jms.ConnectionFactory;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration for ActiveMQ {@link ConnectionFactory}.
 *
 * @author Greg Turnquist
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Aurélien Leboulanger
 * @since 1.1.0
 */
@Configuration
class ActiveMQConnectionFactoryConfiguration {

    private final List<ActiveMQConnectionFactoryCustomizer> connectionFactoryCustomizers;

    private final JmsProperties jmsProperties;

    private final ActiveMQProperties properties;

    ActiveMQConnectionFactoryConfiguration(JmsProperties jmsProperties,
                                           ActiveMQProperties properties,
                                           ObjectProvider<ActiveMQConnectionFactoryCustomizer> connectionFactoryCustomizers) {
        this.jmsProperties = jmsProperties;
        this.properties = properties;
        this.connectionFactoryCustomizers = connectionFactoryCustomizers
                .orderedStream().collect(Collectors.toList());
    }

    @Bean
    public ActiveMQConnectionFactory jmsConnectionFactory() {
        return createConnectionFactory();
    }

    private ActiveMQConnectionFactory createConnectionFactory() {
        return new ActiveMQConnectionFactoryFactory(this.properties, this.connectionFactoryCustomizers)
                .createConnectionFactory(ActiveMQConnectionFactory.class);
    }
}
