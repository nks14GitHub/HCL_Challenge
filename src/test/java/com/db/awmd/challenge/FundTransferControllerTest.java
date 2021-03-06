package com.db.awmd.challenge;

import static org.mockito.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class FundTransferControllerTest {
	
	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	NotificationService notifyCustomer;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}
	
	@Test
	public void validFundTransfer() throws Exception {
		this.accountsService.createAccount(new Account("BOB0001", new BigDecimal("123.45")));
		this.accountsService.createAccount(new Account("BOB0002", new BigDecimal("120.45")));

		Mockito.doNothing().when(notifyCustomer).notifyAboutTransfer(isA(Account.class), isA(String.class));
		notifyCustomer.notifyAboutTransfer(new Account("BOB0001", new BigDecimal("123.45")), "");
		this.mockMvc.perform(put("/v1/fundtransfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\":\"BOB0001\",\"toAccountId\":\"BOB0002\",\"amount\":10}")).andExpect(status().isOk());
	}

	@Test
	public void nullAccount1FundTransfer() throws Exception {
		this.accountsService.createAccount(new Account("BOB0001", new BigDecimal("123.45")));
		this.accountsService.createAccount(new Account("BOB0002", new BigDecimal("120.45")));

		this.mockMvc.perform(put("/v1/fundtransfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"toAccountId\":\"BOB0002\",\"amount\":10}")).andExpect(status().isBadRequest());
	}

	@Test
	public void nullAccount2FundTransfer() throws Exception {
		this.accountsService.createAccount(new Account("BOB0001", new BigDecimal("123.45")));
		this.accountsService.createAccount(new Account("BOB0002", new BigDecimal("120.45")));

		this.mockMvc.perform(put("/v1/fundtransfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\":\"BOB0001\",\"amount\":10}")).andExpect(status().isBadRequest());
	}

	@Test
	public void insufficientAmountFundTransfer() throws Exception {
		this.accountsService.createAccount(new Account("BOB0001", new BigDecimal("12.45")));
		this.accountsService.createAccount(new Account("BOB0002", new BigDecimal("120.45")));

		this.mockMvc.perform(put("/v1/fundtransfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fromAccountId\":\"BOB0001\",\"toAccountId\":\"BOB0002\",\"amount\":30}")).andExpect(status().isBadRequest());
	}

}
