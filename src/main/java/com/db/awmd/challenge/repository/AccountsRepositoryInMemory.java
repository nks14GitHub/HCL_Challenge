package com.db.awmd.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.FundTransferException;
import com.db.awmd.challenge.service.NotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {
	
	@Autowired
	NotificationService notifyCustomer;

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException(
					"Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public int fundTransferRequest(MoneyTransferRequest fundTransfer) {
		
		synchronized (this) {
			if(!accounts.containsKey(fundTransfer.getFromAccountId())||!accounts.containsKey(fundTransfer.getToAccountId())) {
				throw new FundTransferException("To/From account doesnot exists");
			}
			
			if(accounts.get(fundTransfer.getFromAccountId()).getBalance().compareTo(fundTransfer.getAmount())==-1) {
				throw new FundTransferException(
						"Account id " + fundTransfer.getFromAccountId() + " Has insufficient funds for transfer");
			}
			
			Account fromAccount=accounts.get(fundTransfer.getFromAccountId());
			Account toAccount=accounts.get(fundTransfer.getToAccountId());
			
			fromAccount.setBalance(fromAccount.getBalance().subtract(fundTransfer.getAmount()));
			accounts.put(fundTransfer.getFromAccountId(),fromAccount);
			
			notifyCustomer.notifyAboutTransfer(fromAccount, "Amount :"+fundTransfer.getAmount()+ " debited from your account");
			
			toAccount.setBalance(toAccount.getBalance().add(fundTransfer.getAmount()));
			accounts.put(fundTransfer.getToAccountId(),toAccount);
			
			notifyCustomer.notifyAboutTransfer(toAccount, "Amount :"+fundTransfer.getAmount()+ " credited to your account");
			
			return 1;
		}
		
		
	}

}
