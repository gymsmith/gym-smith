package com.todoteg.model.util;


//import java.util.List;
import java.util.Map;

import com.todoteg.model.Client;

//import com.fasterxml.jackson.annotation.JsonProperty;

public class PageSupportUtil {
	
	private Client[] content;
	private Map<String, Long>[] totalElements;
	
	public PageSupportUtil() {
		
	}
	
	public PageSupportUtil(Client[] content, int pageNumber, int pageSize, Map<String, Long>[] totalElements) {
		this.content = content;
		this.totalElements = totalElements;
	}
	
	public Client[] getContent() {
		return content;
	}
	
	public void setContent(Client[] content) {
		this.content = content;
	}

	public Long getTotalElements() {
		return totalElements.length > 0? totalElements[0].get("count"): 0;
	}

	public void setTotalElements(Map<String, Long>[] totalElements) {
		this.totalElements = totalElements;
	}
	
}

