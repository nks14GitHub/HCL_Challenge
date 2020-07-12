package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.exception.FundTransferException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.FundTransferService;
import com.db.awmd.challenge.service.NotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class FundTransferServiceTest {
	
	@Autowired
	private AccountsService accountsService;
	@Autowired
	private FundTransferService fundTransferService;

	@MockBean
	NotificationService notifyCustomer;
	
	@Before
	public void prepareMockMvc() {

		accountsService.getAccountsRepository().clearAccounts();
	}
	
	@Test
	public void insufficientAccountBalance() throws Exception {


		Mockito.doNothing().when(notifyCustomer).notifyAboutTransfer(new Account("xyz",new BigDecimal(100)), "");
		this.accountsService.createAccount(new Account("BOB001",new BigDecimal(100)));
		this.accountsService.createAccount(new Account("BOB002",new BigDecimal(100)));

		MoneyTransferRequest fundTrsfrRequest=new MoneyTransferRequest();
		fundTrsfrRequest.setAmount(new BigDecimal(1000));
		fundTrsfrRequest.setFromAccountId("BOB001");
		fundTrsfrRequest.setToAccountId("BOB002");

		try {
			this.fundTransferService.fundTransferRequest(fundTrsfrRequest);
			fail("fails for insufficient balance");
		} catch (FundTransferException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + fundTrsfrRequest.getFromAccountId() + " Has insufficient funds for transfer");
		}

	}

	@Test
	public void accountNotPresent() throws Exception {


		Mockito.doNothing().when(notifyCustomer).notifyAboutTransfer(new Account("xyz",new BigDecimal(100)), "");
		this.accountsService.createAccount(new Account("BOB001",new BigDecimal(100)));
		this.accountsService.createAccount(new Account("BOB002",new BigDecimal(100)));

		MoneyTransferRequest fundTrsfrRequest=new MoneyTransferRequest();
		fundTrsfrRequest.setAmount(new BigDecimal(1000));
		fundTrsfrRequest.setFromAccountId("BOB001");
		fundTrsfrRequest.setToAccountId("BOB003");

		try {
			this.fundTransferService.fundTransferRequest(fundTrsfrRequest);
			fail("fails for account doesnt exists");
		} catch (FundTransferException ex) {
			assertThat(ex.getMessage()).isEqualTo("To/From account doesnot exists");
		}

	}

	@Test
	public void validFundTransfer() throws Exception {


		this.accountsService.createAccount(new Account("BOB001",new BigDecimal(100)));
		this.accountsService.createAccount(new Account("BOB002",new BigDecimal(100)));

		MoneyTransferRequest fundTrsfrRequest=new MoneyTransferRequest();
		fundTrsfrRequest.setAmount(new BigDecimal(50));
		fundTrsfrRequest.setFromAccountId("BOB001");
		fundTrsfrRequest.setToAccountId("BOB002");
		
		Mockito.doNothing().when(notifyCustomer).notifyAboutTransfer(isA(Account.class), isA(String.class));
		notifyCustomer.notifyAboutTransfer(new Account("BOB001",new BigDecimal(100)), "");
		
		//doAnswer(answer)
		assertThat(1==this.fundTransferService.fundTransferRequest(fundTrsfrRequest));

	}

}
