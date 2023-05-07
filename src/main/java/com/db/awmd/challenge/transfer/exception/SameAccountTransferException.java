package com.db.awmd.challenge.transfer.exception;

public class SameAccountTransferException extends TransferException {

	private static final long serialVersionUID = 1L;

	public SameAccountTransferException(String message) {
		super(message);
	}

}
