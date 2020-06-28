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

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.FundTransferException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {
	

	@Autowired
	private AccountsService accountsService;

	@MockBean
	NotificationService notifyCustomer;
	
	@Before
	public void prepareMockMvc() {

		accountsService.getAccountsRepository().clearAccounts();
	}


	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

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
			this.accountsService.fundTransferRequest(fundTrsfrRequest);
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
			this.accountsService.fundTransferRequest(fundTrsfrRequest);
			fail("fails for insufficient balance");
		} catch (FundTransferException ex) {
			assertThat(ex.getMessage()).isEqualTo("either of  the account doesnot exists");
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
		assertThat(1==this.accountsService.fundTransferRequest(fundTrsfrRequest));
		
		

		

	}

}
