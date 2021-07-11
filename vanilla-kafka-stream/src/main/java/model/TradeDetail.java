package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TradeDetail {
  private String accountKey;
  private Integer tradedQuantity;
  private Double netAmount;
  private String companyId;

  public TradeDetail (Trade trade, Account account) {
    this.accountKey = trade.getAccountKey();
    this.tradedQuantity = trade.getQuantity();
    this.netAmount = trade.getPrice();
    this.companyId = account.getCompanyId();
  }
}
