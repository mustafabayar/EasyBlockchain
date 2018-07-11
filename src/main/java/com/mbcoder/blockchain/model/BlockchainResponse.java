package com.mbcoder.blockchain.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockchainResponse {

	private String message;
	private int length;
	private List<Block> chain;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public List<Block> getChain() {
		return chain;
	}
	public void setChain(List<Block> chain) {
		this.chain = chain;
	}
	
	

}
