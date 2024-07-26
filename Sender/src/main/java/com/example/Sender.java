package com.example;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Sender {
    private final static String EXCHANGE_NAME = "topic_logs";
    private final static int INTERVAL_SECONDS = 5;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            Random random = new Random();
            
            while (true) {
                int randomNumber = random.nextInt(1000);
                String message = "Numero actual: " + randomNumber;
                Instant now = Instant.now();
                String messageWithTimestamp = String.format("Mensaje: %s, Timestamp: %s, Size: %d",
                        message, now.toString(), message.getBytes("UTF-8").length);

                String routingKey = "test.key";
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, messageWithTimestamp.getBytes("UTF-8"));
                System.out.println("Mensaje enviado: '" + messageWithTimestamp + "'");
                
                TimeUnit.SECONDS.sleep(INTERVAL_SECONDS);
            }
        }
    }
}
