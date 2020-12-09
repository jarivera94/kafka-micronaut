package co.com.sunset.order.kafka.consumer;

import co.com.sunset.order.models.OrderDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.util.function.Predicate;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.reactivestreams.Publisher;

@Slf4j
@Singleton
@KafkaListener(
    groupId = "orderDetailsConsumer",
    pollTimeout = "500ms",
    properties = {
      @Property(name = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, value = "10000"),
      @Property(name = ConsumerConfig.MAX_POLL_RECORDS_CONFIG, value = "1"),
      @Property(name = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, value = "earliest")
    })
@ServerWebSocket("/ws/order/{username}")
public class OrderDetailsKafkaConsumer {

  private final WebSocketBroadcaster broadcaster;
  private final ObjectMapper objectMapper;

  public OrderDetailsKafkaConsumer(WebSocketBroadcaster broadcaster, ObjectMapper objectMapper) {
    this.broadcaster = broadcaster;
    this.objectMapper = objectMapper;
  }

  @Topic("order.details")
  public Publisher<String> consumeNewMessage(OrderDetail orderDetail) throws JsonProcessingException {
    log.info(
        "OrderDetailsKafkaConsumer::consumeNewMessage message gotten {}",
        orderDetail.getKeyOrder());

    return broadcaster.broadcast(
        objectMapper.writeValueAsString(orderDetail), isValid(orderDetail.getUser()));
    
  }

  @OnOpen
  public Publisher<String> onOpen(String username, WebSocketSession session) {
    String msg = "[" + username + "] Joined!";
    log.info(msg);
    session.getAttributes().put("username", username);
    return broadcaster.broadcast(msg, isValid(username));
  }

  @OnMessage
  public Publisher<String> onMessage(String username, String message, WebSocketSession session) {
    String msg = "[" + username + "] " + message;
    log.info(msg);
    return broadcaster.broadcast(msg, isValid(username));
  }

  @OnClose
  public Publisher<String> onClose(String username, WebSocketSession session) {
    String msg = "[" + username + "] Disconnected!";
    log.info(msg);
    return broadcaster.broadcast(msg, isValid(username));
  }

  private Predicate<WebSocketSession> isValid(String username) {
    // return s -> username.equalsIgnoreCase(s.getUriVariables().get("username", String.class,
    // null))
    return s -> username.equalsIgnoreCase(s.getAttributes().asMap().get("username").toString());
  }
}
