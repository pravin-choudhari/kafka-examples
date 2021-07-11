package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TradeSummary {
private String accountKey;
private String city;
private String address;
private String companyId;
private Integer tradedQuantity;
private Double netAmount;

    public TradeSummary(TradeDetail detail , Company company) {
      if (detail == null && company == null) {
        this.accountKey = "";
        this.city = "";
        this.address = "";
        this.companyId = company.getCompanyId();
        this.tradedQuantity = detail.getTradedQuantity();
        this.netAmount = detail.getNetAmount();

      } else {
        this.accountKey = detail.getAccountKey();
        this.city = company.getCity();
        this.address = company.getAddress();
        this.companyId = company.getCompanyId();
        this.tradedQuantity = detail.getTradedQuantity();
        this.netAmount = detail.getNetAmount();

        }
    }
}
