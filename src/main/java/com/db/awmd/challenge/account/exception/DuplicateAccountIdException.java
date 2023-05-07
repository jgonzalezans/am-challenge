package com.db.awmd.challenge.account.exception;

public class DuplicateAccountIdException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateAccountIdException(String message) {
		super(message);
	}
}
