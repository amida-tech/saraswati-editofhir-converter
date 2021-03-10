package com.amida.saraswati.edifhir.configure;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures Kafka clients.
 *
 * @author Warren Lin
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfigure {

    @Value(value = "${kafka.usessl}")
    private boolean useSsl;

    @Value(value = "${kafka.producer.bootstrapAddress}")
    private String bootstrapAddressProducer;

    @Value(value = "${kafka.consumer.bootstrapaddress}")
    private String bootstrapAddressConsumer;

    @Value(value = "${kafka.consumer.group}")
    private String consumerGroupId;

    // SSL settings.
    @Value(value = "${ssl.truststore.location}")
    private String truststoreLocation;

    @Value(value = "${ssl.truststore.password}")
    private String truststorePassword;

    @Value(value = "${ssl.keystore.location}")
    private String keystoreLocation;

    @Value(value = "${ssl.keystore.password}")
    private String keystorePassword;

    @Value(value = "${ssl.key.password}")
    private String keyPassword;

    @Value(value = "${ssl.protocol}")
    private String sslProtocol;

    @Bean
    public KafkaConsumer<String, String> consumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddressConsumer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        if (useSsl) {
            setSSL(props);
        }
        return new KafkaConsumer<>(props);
    }

    @Bean
    public KafkaProducer<String, String> producer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddressProducer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        if (useSsl) {
            setSSL(props);
        }
        return new KafkaProducer<>(props);
    }

    private void setSSL(Map<String, Object> props) {
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
//        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
//        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
//        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
//        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
        props.put(SslConfigs.SSL_PROTOCOL_CONFIG, sslProtocol);
    }
}
