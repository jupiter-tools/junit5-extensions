package com.jupiter.tools.spring.test.activemq.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jupiter.tools.spring.test.activemq.annotation.EnableActiveMqTestContainers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Created on 07.08.2018.
 *
 * @author Korovin Anatoliy
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableActiveMqTestContainers
public class EnableActiveMqTestContainersTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    void testSend() {
        // Act
        jmsTemplate.convertAndSend("simple-queue", "123");

        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> TestConfig.events.size() > 0);
        // Asserts
        assertThat(TestConfig.events).containsOnly("123");
    }

    @TestConfiguration
    public static class TestConfig {

        public static List<String> events = new ArrayList<>();

        @Bean
        public Queue testQueue() {
            return new Queue("simple-queue");
        }

        @Component
        @EnableJms
        public class TestListener {

            @JmsListener(destination = "simple-queue")
            public void processMessage(String message) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                events.add(message);
                System.out.println("!!!! " + message);
            }
        }
    }
}
