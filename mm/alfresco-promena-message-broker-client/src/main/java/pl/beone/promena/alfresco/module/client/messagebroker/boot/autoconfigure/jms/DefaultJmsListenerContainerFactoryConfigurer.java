/*
 * Copyright 2012-2017 the original author or authors.
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

package pl.beone.promena.alfresco.module.client.messagebroker.boot.autoconfigure.jms;

import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.Assert;

import javax.jms.ConnectionFactory;

/**
 * Configure {@link DefaultJmsListenerContainerFactory} with sensible defaults.
 *
 * @author Stephane Nicoll
 * @since 1.3.3
 */
public final class DefaultJmsListenerContainerFactoryConfigurer {

    private DestinationResolver destinationResolver;

    private MessageConverter messageConverter;

    private JtaTransactionManager transactionManager;

    private JmsProperties jmsProperties;

    /**
     * Set the {@link DestinationResolver} to use or {@code null} if no destination
     * resolver should be associated with the factory by default.
     * @param destinationResolver the {@link DestinationResolver}
     */
    void setDestinationResolver(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    /**
     * Set the {@link MessageConverter} to use or {@code null} if the out-of-the-box
     * converter should be used.
     * @param messageConverter the {@link MessageConverter}
     */
    void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Set the {@link JtaTransactionManager} to use or {@code null} if the JTA support
     * should not be used.
     * @param transactionManager the {@link JtaTransactionManager}
     */
    void setTransactionManager(JtaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Set the {@link JmsProperties} to use.
     * @param jmsProperties the {@link JmsProperties}
     */
    void setJmsProperties(JmsProperties jmsProperties) {
        this.jmsProperties = jmsProperties;
    }

    /**
     * Configure the specified jms listener container factory. The factory can be further
     * tuned and default settings can be overridden.
     * @param factory the {@link DefaultJmsListenerContainerFactory} instance to configure
     * @param connectionFactory the {@link ConnectionFactory} to use
     */
    public void configure(DefaultJmsListenerContainerFactory factory,
                          ConnectionFactory connectionFactory) {
        Assert.notNull(factory, "Factory must not be null");
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(this.jmsProperties.isPubSubDomain());
        if (this.transactionManager != null) {
            factory.setTransactionManager(this.transactionManager);
        }
        else {
            factory.setSessionTransacted(true);
        }
        if (this.destinationResolver != null) {
            factory.setDestinationResolver(this.destinationResolver);
        }
        if (this.messageConverter != null) {
            factory.setMessageConverter(this.messageConverter);
        }
        JmsProperties.Listener listener = this.jmsProperties.getListener();
        factory.setAutoStartup(listener.isAutoStartup());
        if (listener.getAcknowledgeMode() != null) {
            factory.setSessionAcknowledgeMode(listener.getAcknowledgeMode().getMode());
        }
        String concurrency = listener.formatConcurrency();
        if (concurrency != null) {
            factory.setConcurrency(concurrency);
        }
    }

}
