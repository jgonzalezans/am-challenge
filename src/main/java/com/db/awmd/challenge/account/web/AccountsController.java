package com.db.awmd.challenge.account.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.account.domain.Account;
import com.db.awmd.challenge.account.exception.AccountNotFoundException;
import com.db.awmd.challenge.account.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.account.exception.InsufficientBalanceException;
import com.db.awmd.challenge.account.service.AccountsService;
import com.db.awmd.challenge.transfer.exception.InvalidTransferAmountException;
import com.db.awmd.challenge.transfer.exception.SameAccountTransferException;
import com.db.awmd.challenge.transfer.web.TransferRequest;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;

	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public ResponseEntity<Object> getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		try {
			Account account = accountsService.getAccount(accountId);
			return ResponseEntity.ok(account);
		} catch (AccountNotFoundException anfe) {
			return new ResponseEntity<>(anfe.getMessage(), HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> transfer(@RequestBody @Valid TransferRequest transferRequest) {
		try {
			accountsService.transfer(transferRequest.getAccountFromId(), transferRequest.getAccountToId(),
					transferRequest.getAmount());
			return ResponseEntity.ok("Transfer completed successfully");
		} catch (InvalidTransferAmountException | SameAccountTransferException | InsufficientBalanceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (AccountNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}
	}
}
