package com.mbcoder.blockchain.model;

public class Transaction {

	private String sender;
	private String receipent;
	private int amount;
	
	public Transaction() {
	}

	public Transaction(String sender, String receipent, int amount) {
		this.sender = sender;
		this.receipent = receipent;
		this.amount = amount;
	}

	public String getSender() {
		return sender;
	}

	public String getReceipent() {
		return receipent;
	}

	public int getAmount() {
		return amount;
	}

}
