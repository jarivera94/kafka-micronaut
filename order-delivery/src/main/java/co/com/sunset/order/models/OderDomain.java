package co.com.sunset.order.models;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OderDomain {

  private String user;
  private List<String> products;
  private String key;
}
