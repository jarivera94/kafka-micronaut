package co.com.sunset.oder.details.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetail {

  private String user;
  private String product;
  private String keyOrder;
  private int productId;
  private String productName;
  private double productPrice;
  private String productDescription;
  private String productMessage1;
  private String keyProduct;
  private String message2;
}
