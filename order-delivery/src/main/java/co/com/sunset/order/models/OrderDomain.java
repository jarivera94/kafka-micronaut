package co.com.sunset.order.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDomain {

  private String user;
  private String product;
  private String key;
}
