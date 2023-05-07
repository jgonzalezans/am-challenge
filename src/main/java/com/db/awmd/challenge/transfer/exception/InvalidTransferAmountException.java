package com.db.awmd.challenge.transfer.exception;

public class InvalidTransferAmountException extends TransferException {

	private static final long serialVersionUID = 1L;

	public InvalidTransferAmountException(String message) {
		super(message);
	}

}
