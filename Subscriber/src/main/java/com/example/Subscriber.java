package com.example;

import com.rabbitmq.client.*;

import java.time.Instant;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subscriber {
    private final static String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();
            String routingKey = "test.key";
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);

            System.out.println("Esperando mensajes, caso de error: CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Se recibió el mensaje: '" + message + "'");

                String timestampStr = extractTimestamp(message);
                Integer expectedSize = extractSize(message);

                if (timestampStr != null && expectedSize != null) {
                    Instant receivedTimestamp = Instant.now();
                    Instant sentTimestamp = Instant.parse(timestampStr);

                    Duration duration = Duration.between(sentTimestamp, receivedTimestamp);
                    System.out.println("Tiempo transcurrido: " + duration.toMillis() + " milisegundos");

                    int actualSize = delivery.getBody().length;
                    System.out.println("Tamaño real del mensaje: " + actualSize + " bytes");

                    if (expectedSize.equals(actualSize)) {
                        System.out.println("El tamaño del mensaje coincide con el tamaño esperado.");
                    } else {
                        //System.out.println("El tamaño del mensaje NO coincide con el tamaño esperado.");
                    }
                }
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
            synchronized (Subscriber.class) {
                Subscriber.class.wait();
            }
        }
    }
    private static String extractTimestamp(String message) {
        Pattern pattern = Pattern.compile("Timestamp: ([^,]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    private static Integer extractSize(String message) {
        Pattern pattern = Pattern.compile("Size: (\\d+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
