package com.mbcoder.blockchain.model;

import java.util.ArrayList;
import java.util.List;

public class Block {

	private int index;
	private long timestamp;
	private List<Transaction> transactions;
	private int proof;
	private String previousHash;

	public Block(int index, long timestamp, List<Transaction> transactions, int proof, String previousHash) {
		this.index = index;
		this.timestamp = timestamp;
		this.transactions = new ArrayList<>(transactions);
		this.proof = proof;
		this.previousHash = previousHash;
	}

	public int getIndex() {
		return index;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public int getProof() {
		return proof;
	}

	public String getPreviousHash() {
		return previousHash;
	}

}
