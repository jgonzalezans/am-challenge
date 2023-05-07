package com.db.awmd.challenge.transfer.web;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

	@NotEmpty
	private String accountFromId;
	@NotEmpty
	private String accountToId;
	@Min(value = 0, message = "Transfer value must be positive.")
	private BigDecimal amount;
}
