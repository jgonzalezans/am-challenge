package com.db.awmd.challenge.account.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.account.domain.Account;
import com.db.awmd.challenge.account.exception.AccountNotFoundException;
import com.db.awmd.challenge.account.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.account.exception.InsufficientBalanceException;
import com.db.awmd.challenge.transfer.exception.InvalidTransferAmountException;
import com.db.awmd.challenge.transfer.exception.SameAccountTransferException;
import com.db.awmd.challenge.transfer.exception.TransferException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) throws AccountNotFoundException {
		Account account = accounts.get(accountId);
		if (account == null) {
			throw new AccountNotFoundException("Account id " + accountId + " not found");
		}
		return account;
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	private void deposit(Account account, BigDecimal amount) {
		accounts.computeIfPresent(account.getAccountId(), (id, acc) -> {
			acc.setBalance(acc.getBalance().add(amount));
			return acc;
		});

	}

	private void withdraw(Account account, BigDecimal amount) {
		accounts.computeIfPresent(account.getAccountId(), (id, acc) -> {
			acc.setBalance(acc.getBalance().subtract(amount));
			return acc;
		});
	}

	public void transfer(String accountFromId, String accountToId, BigDecimal amount) throws TransferException {

		validateTransfer(accountFromId, accountToId, amount);

		Account accountFrom = getAccount(accountFromId);
		Account accountTo = getAccount(accountToId);

		checkBalance(accountFrom, amount);

		withdraw(accountFrom, amount);
		deposit(accountTo, amount);

	}

	private void validateTransfer(String accountFromId, String accountToId, BigDecimal amount)
			throws TransferException {
		if (accountFromId.equals(accountToId)) {
			throw new SameAccountTransferException("Cannot transfer money to the same account");
		}

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidTransferAmountException("Transfer amount must be greater than zero");
		}
	}

	private void checkBalance(Account account, BigDecimal amount) throws InsufficientBalanceException {
		if (amount.compareTo(account.getBalance()) > 0) {
			throw new InsufficientBalanceException("Account "+ account.getAccountId() + " has insufficient balance");
		}
	}

}