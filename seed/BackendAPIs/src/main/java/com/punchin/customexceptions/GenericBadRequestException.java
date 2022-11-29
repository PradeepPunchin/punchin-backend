package com.punchin.customexceptions;

public class GenericBadRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GenericBadRequestException(String msg) {
		super(msg);
	}
}