package com.punchin.customexceptions;

public class GenericForbiddenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GenericForbiddenException(String msg) {
		super(msg);
	}
}