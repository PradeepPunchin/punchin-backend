package com.punchin.customexceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CustomErrorResponse {
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date date;
	private long errorId;
    private int status;
    private List<HashMap<String,Object>> errors;
    
    CustomErrorResponse(int status, List<HashMap<String,Object>> errors){
       this.date = new Date();
       this.errorId = date.getTime();
       this.status = status;
       this.errors = errors;
    }
	public Date getTimestamp() {
		return date;
	}
	public void setTimestamp(Date date) {
		this.date = date;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<HashMap<String,Object>> getErrors() {
		return errors;
	}
	public void setErrors(List<HashMap<String,Object>> errors) {
		this.errors = errors;
	}
	public long getErrorId() {
		return errorId;
	}
	public void setErrorId(long errorId) {
		this.errorId = errorId;
	}
	
}
