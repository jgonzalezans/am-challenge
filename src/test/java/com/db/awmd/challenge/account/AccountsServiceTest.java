package com.db.awmd.challenge.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.db.awmd.challenge.account.domain.Account;
import com.db.awmd.challenge.account.exception.AccountNotFoundException;
import com.db.awmd.challenge.account.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.account.exception.InsufficientBalanceException;
import com.db.awmd.challenge.account.service.AccountsService;
import com.db.awmd.challenge.notification.NotificationService;
import com.db.awmd.challenge.transfer.exception.InvalidTransferAmountException;
import com.db.awmd.challenge.transfer.exception.SameAccountTransferException;
import com.db.awmd.challenge.transfer.exception.TransferException;

@SpringBootTest
class AccountsServiceTest {

	@MockBean
	private NotificationService notificationService;

	@Autowired
	private AccountsService accountsService;

	@BeforeEach
	void clearAccountsBeforeTest() {
		this.accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	@DisplayName("Add account")
	void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	@DisplayName("Add account should fail on duplicate ID")
	void addAccount_failsOnDuplicateId() throws Exception {
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
	@DisplayName("Transfer amount must be greater than zero")
	void transferInvalidAmount() {

		String accountFromId = "123";
		String accountToId = "456";
		BigDecimal amount = BigDecimal.ZERO;

		assertThrows(InvalidTransferAmountException.class,
				() -> accountsService.transfer(accountFromId, accountToId, amount));
	}

	@Test
	@DisplayName("Cannot transfer money to the same account")
	void transferSameAccount() {

		String accountFromId = "123";
		String accountToId = "123";
		BigDecimal amount = BigDecimal.TEN;

		assertThrows(SameAccountTransferException.class,
				() -> accountsService.transfer(accountFromId, accountToId, amount));
	}

	@Test
	@DisplayName("Account not found")
	void transferAccountNotFound() {

		String accountFromId = "123";
		String accountToId = "456";
		BigDecimal amount = BigDecimal.TEN;

		assertThrows(AccountNotFoundException.class,
				() -> accountsService.transfer(accountFromId, accountToId, amount));
	}

	@Test
	@DisplayName("Insufficient balance")
	void transferInsufficientBalance() {

		BigDecimal amount = BigDecimal.TEN;

		Account accountFrom = new Account("Id-1123");
		accountFrom.setBalance(new BigDecimal(0));
		accountsService.createAccount(accountFrom);

		Account accountTo = new Account("Id-212");
		accountTo.setBalance(new BigDecimal(1000));
		accountsService.createAccount(accountTo);

		assertThrows(InsufficientBalanceException.class, () -> accountsService.transfer("Id-1123", "Id-212", amount));
	}

	@Test
	@DisplayName("Successful transfer")
	void transferSuccess() throws TransferException {
		// Given

		BigDecimal initialBalance = new BigDecimal(1000);

		Account accountFrom = new Account("Id-1");
		accountFrom.setBalance(initialBalance);
		accountsService.createAccount(accountFrom);

		Account accountTo = new Account("Id-2");
		accountTo.setBalance(new BigDecimal(0));
		accountsService.createAccount(accountTo);

		BigDecimal transferAmount = BigDecimal.TEN;

		// When
		accountsService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), transferAmount);

		// Then

		BigDecimal expectedFromBalance = initialBalance.subtract(transferAmount);
		BigDecimal expectedToBalance = transferAmount;
		assertEquals(expectedFromBalance, accountFrom.getBalance());
		assertEquals(expectedToBalance, accountTo.getBalance());
	}

	@Test
	@DisplayName("Successful concurrent transfer")
	void testConcurrentTransferSuccess() throws Exception {

		Account accountFrom = new Account("Id-1");
		accountFrom.setBalance(new BigDecimal(1000));
		accountsService.createAccount(accountFrom);

		Account accountTo = new Account("Id-2");
		accountTo.setBalance(new BigDecimal(1000));
		accountsService.createAccount(accountTo);

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		Runnable transfer1 = () -> {
			try {
				accountsService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), new BigDecimal("500"));
			} catch (TransferException e) {
				throw new RuntimeException(e);
			}
		};

		Runnable transfer2 = () -> {
			try {
				accountsService.transfer(accountTo.getAccountId(), accountFrom.getAccountId(), new BigDecimal("100"));
			} catch (TransferException e) {
				throw new RuntimeException(e);
			}
		};

		// Execute the transfers concurrently
		executorService.submit(transfer1);
		executorService.submit(transfer2);

		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.SECONDS);

		// Verify that the transfers have been completed
		assertThat(accountFrom.getBalance()).isEqualByComparingTo("600");
		assertThat(accountTo.getBalance()).isEqualByComparingTo("1400");
	}

	@Test
	@DisplayName("Successful transfer calls notification service")
	void transferSuccessfulTransferCallsNotificationService() throws TransferException {

		String accountFromId = "accountFrom";
		String accountToId = "accountTo";
		BigDecimal amount = new BigDecimal("100.00");
		Account accountFrom = new Account(accountFromId, new BigDecimal("500.00"));
		Account accountTo = new Account(accountToId, new BigDecimal("200.00"));
		accountsService.createAccount(accountFrom);
		accountsService.createAccount(accountTo);

		accountsService.transfer(accountFromId, accountToId, amount);

		// Assert
		verify(notificationService, times(1)).notifyAboutTransfer(accountFrom,
				"Transfer completed - Sent 100.00 to accountTo");
		verify(notificationService, times(1)).notifyAboutTransfer(accountTo,
				"Transfer completed - Received 100.00 from accountFrom");
	}
}