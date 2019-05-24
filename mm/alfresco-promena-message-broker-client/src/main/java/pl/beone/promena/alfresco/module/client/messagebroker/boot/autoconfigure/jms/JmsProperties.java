package pl.beone.promena.alfresco.module.client.messagebroker.boot.autoconfigure.jms;

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

/**
 * Configuration properties for JMS.
 *
 * @author Greg Turnquist
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
public class JmsProperties {

    private final Cache cache = new Cache();
    private final Listener listener = new Listener();
    /**
     * Whether the default destination type is topic.
     */
    private boolean pubSubDomain = false;

    public boolean isPubSubDomain() {
        return this.pubSubDomain;
    }

    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    public Cache getCache() {
        return this.cache;
    }

    public Listener getListener() {
        return this.listener;
    }

    /**
     * Translate the acknowledge modes defined on the {@link javax.jms.Session}.
     *
     * <p>
     * {@link javax.jms.Session#SESSION_TRANSACTED} is not defined as we take care of this
     * already via a call to {@code setSessionTransacted}.
     */
    public enum AcknowledgeMode {

        /**
         * Messages sent or received from the session are automatically acknowledged. This
         * is the simplest mode and enables once-only message delivery guarantee.
         */
        AUTO(1),

        /**
         * Messages are acknowledged once the message listener implementation has called
         * {@link javax.jms.Message#acknowledge()}. This mode gives the application
         * (rather than the JMS provider) complete control over message acknowledgement.
         */
        CLIENT(2),

        /**
         * Similar to auto acknowledgment except that said acknowledgment is lazy. As a
         * consequence, the messages might be delivered more than once. This mode enables
         * at-least-once message delivery guarantee.
         */
        DUPS_OK(3);

        private final int mode;

        AcknowledgeMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return this.mode;
        }

    }

    public static class Cache {

        /**
         * Whether to cache message consumers.
         */
        private boolean consumers = false;

        /**
         * Whether to cache message producers.
         */
        private boolean producers = true;

        /**
         * Size of the session cache (per JMS Session type).
         */
        private int sessionCacheSize = 1;

        public boolean isConsumers() {
            return this.consumers;
        }

        public void setConsumers(boolean consumers) {
            this.consumers = consumers;
        }

        public boolean isProducers() {
            return this.producers;
        }

        public void setProducers(boolean producers) {
            this.producers = producers;
        }

        public int getSessionCacheSize() {
            return this.sessionCacheSize;
        }

        public void setSessionCacheSize(int sessionCacheSize) {
            this.sessionCacheSize = sessionCacheSize;
        }

    }

    public static class Listener {

        /**
         * Start the container automatically on startup.
         */
        private boolean autoStartup = true;

        /**
         * Acknowledge mode of the container. By default, the listener is transacted with
         * automatic acknowledgment.
         */
        private AcknowledgeMode acknowledgeMode;

        /**
         * Minimum number of concurrent consumers.
         */
        private Integer concurrency;

        /**
         * Maximum number of concurrent consumers.
         */
        private Integer maxConcurrency;

        public boolean isAutoStartup() {
            return this.autoStartup;
        }

        public void setAutoStartup(boolean autoStartup) {
            this.autoStartup = autoStartup;
        }

        public AcknowledgeMode getAcknowledgeMode() {
            return this.acknowledgeMode;
        }

        public void setAcknowledgeMode(AcknowledgeMode acknowledgeMode) {
            this.acknowledgeMode = acknowledgeMode;
        }

        public Integer getConcurrency() {
            return this.concurrency;
        }

        public void setConcurrency(Integer concurrency) {
            this.concurrency = concurrency;
        }

        public Integer getMaxConcurrency() {
            return this.maxConcurrency;
        }

        public void setMaxConcurrency(Integer maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
        }

        public String formatConcurrency() {
            if (this.concurrency == null) {
                return (this.maxConcurrency != null) ? "1-" + this.maxConcurrency : null;
            }
            return ((this.maxConcurrency != null)
                    ? this.concurrency + "-" + this.maxConcurrency
                    : String.valueOf(this.concurrency));
        }

    }

}
