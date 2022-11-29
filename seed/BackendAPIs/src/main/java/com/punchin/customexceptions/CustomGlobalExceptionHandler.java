package com.punchin.customexceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
	public static final String ERRORS = "errors";
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<HashMap<String, Object>> err = ex.getBindingResult().getFieldErrors().stream().map(x -> {
			HashMap<String, Object> map = new HashMap<>();
			map.put(x.getField(), x.getDefaultMessage());
			return map;
		}).collect(Collectors.toList());

		CustomErrorResponse errorResponse = new CustomErrorResponse(HttpStatus.BAD_REQUEST.value(), err);

		return new ResponseEntity<>(errorResponse, headers, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(value = GenericNotFoundException.class)
	public ResponseEntity<CustomErrorResponse> handleGenericNotFoundException(GenericNotFoundException e) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		HashMap<String, Object> errorHashMap = new HashMap<>();
		errorHashMap.put(CustomGlobalExceptionHandler.ERRORS, e.getMessage());
		list.add(errorHashMap);
		CustomErrorResponse error = new CustomErrorResponse( HttpStatus.NOT_FOUND.value(), list);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	
	@ExceptionHandler(value = GenericForbiddenException.class)
	public ResponseEntity<CustomErrorResponse> handleGenericAuthorizationException(GenericForbiddenException e) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		HashMap<String, Object> errorHashMap = new HashMap<>();
		errorHashMap.put(CustomGlobalExceptionHandler.ERRORS, e.getMessage());
		list.add(errorHashMap);
		CustomErrorResponse error = new CustomErrorResponse( HttpStatus.FORBIDDEN.value(), list);
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(value = GenericAuthenticationException.class)
	public ResponseEntity<CustomErrorResponse> handleGenericAuthenticationException(GenericAuthenticationException e) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		HashMap<String, Object> errorHashMap = new HashMap<>();
		errorHashMap.put(CustomGlobalExceptionHandler.ERRORS, e.getMessage());
		list.add(errorHashMap);
		CustomErrorResponse error = new CustomErrorResponse( HttpStatus.UNAUTHORIZED.value(), list);
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(value = GenericBadRequestException.class)
	public ResponseEntity<CustomErrorResponse> handleGenericBadRequestException(GenericBadRequestException e) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		HashMap<String, Object> errorHashMap = new HashMap<>();
		errorHashMap.put(CustomGlobalExceptionHandler.ERRORS, e.getMessage());
		list.add(errorHashMap);
		CustomErrorResponse error = new CustomErrorResponse( HttpStatus.BAD_REQUEST.value(), list);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
}
