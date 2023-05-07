package com.db.awmd.challenge.account.repository;

import java.math.BigDecimal;

import com.db.awmd.challenge.account.domain.Account;
import com.db.awmd.challenge.account.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.transfer.exception.TransferException;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;

	Account getAccount(String accountId);

	void clearAccounts();

	void transfer(String accountFromId, String accountToId, BigDecimal amount) throws TransferException;
}
