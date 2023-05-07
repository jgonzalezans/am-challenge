package com.db.awmd.challenge.transfer.domain;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    private String accountFromId;
    private String accountToId;
    private BigDecimal amount;
}
