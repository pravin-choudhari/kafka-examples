package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {
private String accountKey;
private String firstName;
private String lastName;
private Integer accountNumber;
private String companyId;
}
