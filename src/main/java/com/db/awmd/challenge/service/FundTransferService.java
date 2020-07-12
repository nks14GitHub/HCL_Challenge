package com.db.awmd.challenge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.repository.AccountsRepository;

@Service
public class FundTransferService {

	@Autowired
	private AccountsRepository accountsRepository;

	public int fundTransferRequest(MoneyTransferRequest fundTransfer) {
		return this.accountsRepository.fundTransferRequest(fundTransfer);
	}

}
