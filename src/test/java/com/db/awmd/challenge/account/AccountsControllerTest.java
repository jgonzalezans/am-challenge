package com.db.awmd.challenge.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.account.domain.Account;
import com.db.awmd.challenge.account.service.AccountsService;

@SpringBootTest
@AutoConfigureMockMvc
class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}
	
	@Test
	void getAccountNotFound() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
				.andExpect(status().isNotFound());
	}

	@Test
	void transferInvalidAmount() throws Exception {

		Account sourceAccount = new Account("id-1", new BigDecimal("100"));
		accountsService.createAccount(sourceAccount);

		Account destinationAccount = new Account("id-2", new BigDecimal("0"));
		accountsService.createAccount(destinationAccount);

		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"id-1\",\"accountToId\":\"id-2\",\"amount\":-1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void transferSameAccount() throws Exception {

		Account sourceAccount = new Account("id-1", new BigDecimal("100"));
		accountsService.createAccount(sourceAccount);

		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"id-1\",\"accountToId\":\"id-1\",\"amount\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void transferAccountNotFound() throws Exception {
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"id-1\",\"accountToId\":\"id-12312\",\"amount\":1000}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void transferInsufficientBalance() throws Exception {
		Account sourceAccount = new Account("id-1", new BigDecimal("100"));
		accountsService.createAccount(sourceAccount);

		Account destinationAccount = new Account("id-2", new BigDecimal("0"));
		accountsService.createAccount(destinationAccount);

		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"id-1\",\"accountToId\":\"id-2\",\"amount\":200}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testSuccessfulTransfer() throws Exception {
		Account sourceAccount = new Account("sourceAccountId", new BigDecimal("1000"));
		Account destinationAccount = new Account("destinationAccountId", new BigDecimal("500"));
		accountsService.createAccount(sourceAccount);
		accountsService.createAccount(destinationAccount);

		this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(
				"{\"accountFromId\":\"sourceAccountId\",\"accountToId\":\"destinationAccountId\",\"amount\":200}"))
				.andExpect(status().isOk());

		// Verify balances after transfer
		Account updatedSourceAccount = accountsService.getAccount("sourceAccountId");
		Account updatedDestinationAccount = accountsService.getAccount("destinationAccountId");

		assertThat(updatedSourceAccount.getBalance()).isEqualByComparingTo("800");
		assertThat(updatedDestinationAccount.getBalance()).isEqualByComparingTo("700");
	}

	@Test
	void testSimultaneousTransfers() throws Exception {
		// Create source and destination accounts
		Account sourceAccount = new Account("sourceAccountId", new BigDecimal("1000"));
		Account destinationAccount = new Account("destinationAccountId", new BigDecimal("500"));
		accountsService.createAccount(sourceAccount);
		accountsService.createAccount(destinationAccount);

		// Define the two transfer operations to be executed simultaneously
		Runnable transfer1 = () -> {
			try {
				this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(
						"{\"accountFromId\":\"sourceAccountId\",\"accountToId\":\"destinationAccountId\",\"amount\":200}"))
						.andExpect(status().isOk());
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Runnable transfer2 = () -> {
			try {
				this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(
						"{\"accountFromId\":\"destinationAccountId\",\"accountToId\":\"sourceAccountId\",\"amount\":300}"))
						.andExpect(status().isOk());
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		// Create an ExecutorService with a fixed thread pool size
		ExecutorService executorService = Executors.newFixedThreadPool(2);

		// Submit the transfer tasks to the executor
		executorService.submit(transfer1);
		executorService.submit(transfer2);

		// Shut down the executor and wait for the tasks to complete
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		// Verify balances after concurrent transfers
		Account updatedSourceAccount = accountsService.getAccount("sourceAccountId");
		Account updatedDestinationAccount = accountsService.getAccount("destinationAccountId");

		assertThat(updatedSourceAccount.getBalance()).isEqualByComparingTo("1100");
		assertThat(updatedDestinationAccount.getBalance()).isEqualByComparingTo("400");
	}

}