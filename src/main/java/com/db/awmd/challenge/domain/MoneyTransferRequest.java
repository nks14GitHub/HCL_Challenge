package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MoneyTransferRequest {
	
	@NotNull
	@JsonProperty("fromAccountId")
	private String fromAccountId;
	@NotNull
	@JsonProperty("toAccountId")
	private String toAccountId;
	@NotNull
	@Min(value = 1, message = "Invalid Amount recieved.")
	@JsonProperty("amount")
	private BigDecimal amount;

}
