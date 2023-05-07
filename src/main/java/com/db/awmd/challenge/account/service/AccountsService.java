package com.db.awmd.challenge.account.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.account.domain.Account;
import com.db.awmd.challenge.account.repository.AccountsRepository;
import com.db.awmd.challenge.notification.NotificationService;
import com.db.awmd.challenge.transfer.exception.TransferException;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transfer(String accountFromId, String accountToId, BigDecimal amount) throws TransferException {
		this.accountsRepository.transfer(accountFromId, accountToId, amount);
		sendTransferNotifications(accountFromId, accountToId, amount);
	}

	private void sendTransferNotifications(String accountFromId, String accountToId, BigDecimal amount) {
		notificationService.notifyAboutTransfer(getAccount(accountFromId),
				"Transfer completed - Sent " + amount + " to " + accountToId);
		notificationService.notifyAboutTransfer(getAccount(accountToId),
				"Transfer completed - Received " + amount + " from " + accountFromId);

	}

}