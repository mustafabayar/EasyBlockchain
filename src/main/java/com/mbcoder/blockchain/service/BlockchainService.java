package com.mbcoder.blockchain.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.mbcoder.blockchain.model.Block;
import com.mbcoder.blockchain.model.Node;
import com.mbcoder.blockchain.model.Transaction;

@Service
public class BlockchainService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	ObjectMapper mapper;
	
	private List<Block> chain;
	List<Transaction> currentTransactions;
	List<Node> nodes;
	
	int difficulty;

	public BlockchainService() {
		this.difficulty = 4;
		this.chain = new ArrayList<>();
		this.currentTransactions = new ArrayList<>();
		this.nodes = new ArrayList<>();

		createNewBlock(1, "0"); // Genesis Block
	}
	
	/**
	* Create a new Block in the Blockchain
	*
	* @param proof: The proof given by the Proof of Work algorithm
	* @param previousHash: Hash of previous Block
	* @return New Block
	*/
	public Block createNewBlock(int proof, String previousHash) {
		Block block = new Block(this.chain.size() + 1, System.currentTimeMillis(), this.currentTransactions, proof, previousHash);
		this.chain.add(block);
		this.currentTransactions.clear(); // Reset the current list of transactions
		LOGGER.debug("New block is created: {}", block);
		return block;
	}
	
	/**
	* Creates a new transaction to go into the next mined Block
	*
	* @param sender: Address of the Sender
	* @param receipent: Address of the Recipient
	* @param amount: Amount
	* @return The index of the Block that will hold this transaction
	*/
	public int createNewTransaction(String sender, String receipent, int amount) {
		Transaction transaction = new Transaction(sender, receipent, amount);
		this.currentTransactions.add(transaction);
		return getLastBlock().getIndex() + 1;
	}
	
	/**
	* Adds a new transaction to go into the next mined Block
	*
	* @param transaction
	* @return The index of the Block that will hold this transaction
	*/
	public int addNewTransaction(Transaction transaction) {
		this.currentTransactions.add(transaction);
		return getLastBlock().getIndex() + 1;
	}
	
	/**
	* Creates a SHA-256 hash of a Block
	*
	* @param block: Block
	* @return Generated hash string
	* @throws JsonProcessingException 
	*/
	public String hashBlock(Block block) {
		String json = "";
		try {
			json = mapper.writeValueAsString(block);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}
		return Hashing.sha256().hashString(json, StandardCharsets.UTF_8).toString();
	}
	
	/**
	* Simple Proof of Work Algorithm:
	* - Find a number p' such that hash(pp') contains leading 4 zeroes, where p is the previous p'
	* - p is the previous proof, and p' is the new proof
	* 
	* @param lastProof: Previous Proof
	* @return proof: Current Proof
	*/
	public int proofOfWork(int lastProof) {
		int proof = 0;
		while (!validateProof(lastProof, proof)) {
			proof += 1;
		}
		return proof;
	}
	
	/**
	*  Validates the Proof: Does hash(last_proof, proof) contain difficulty level amount leading zeroes?
	*  
	* @param lastProof: Previous Proof
	* @param proof: Current Proof
	* @return True if correct, False if not.
	*/
	public boolean validateProof(int lastProof, int proof) {
		String output = String.valueOf(lastProof) + String.valueOf(proof);
		String guessHash = Hashing.sha256().hashString(output, StandardCharsets.UTF_8).toString();
		return guessHash.startsWith(difficultyString(this.difficulty));
	}
	
	/**
	* Add a new node to the list of nodes
	*  
	* @param address: Address of node. Eg. 'http://127.0.0.1:8080'
	* @return None
	* @throws MalformedURLException 
	*/
	public void registerNode(String address) throws MalformedURLException {
		URL url = new URL(address);
		Node newNode = new Node(url);
		this.nodes.add(newNode);
	}
	
	/**
	* Add a new node to the list of nodes
	*  
	* @param node: New node to be registered
	* @return None
	*/
	public void registerNode(Node node) {
		this.nodes.add(node);
	}
	
	/**
	* Determine if a given blockchain is valid
	*  
	* @param chain: <list> A blockchain
	* @return True if valid, False if not
	* @throws JsonProcessingException 
	*/
	public boolean validateChain(List<Block> chain) {
		if(chain == null || chain.isEmpty()) {
			LOGGER.debug("Chain is null/empty!");
			return false;
		}
		
		Block previousBlock = chain.get(0);
		int currentIndex = 1;
		
		while (currentIndex < chain.size()) {
			Block currentBlock = chain.get(currentIndex);
			
			// Check that the hash of the block is correct
			if(!currentBlock.getPreviousHash().equals(hashBlock(previousBlock))) {
				LOGGER.debug("Chain validation is failed!");
				return false;
			}
			
			previousBlock = currentBlock;
			
			currentIndex += 1;
		}
		LOGGER.debug("Chain is valid!");
		return true;
	}
	
	/**
	* This is the Consensus Algorithm, it resolves conflicts
	* by replacing our chain with the longest one in the network.
	*  
	* @return True if our chain was replaced, False if not
	* @throws JsonProcessingException 
	*/
	public boolean resolveConflicts() {
		
		List<Node> neighbours = this.nodes;
		List<Block> newChain = null;
		
		// We're only looking for chains longer than ours
		int maxLength = this.chain.size();
		
		// Grab and verify the chains from all the nodes in our network
		for (Node node : neighbours) {
			ResponseEntity<List<Block>> response = restTemplate.exchange(node.getUrl().toString() + "/chain", HttpMethod.GET, null, new ParameterizedTypeReference<List<Block>>() {});
			
			if (response.getStatusCode() == HttpStatus.OK) {
				List<Block> currentChain = response.getBody();
				
				// Check if the length is longer and the chain is valid
				if (currentChain.size() > maxLength && validateChain(currentChain)) {
					maxLength = currentChain.size();
					newChain = currentChain;
				}
			}
		}
		
		if (newChain != null) {
			this.chain = newChain;
			return true;
		}
		
		return false;
	}
	
	/**
	* Returns difficulty string target, to compare to hash.
	*  
	* @return difficultyString
	*/
	public String difficultyString(int difficultyLevel) {
		return new String(new char[difficultyLevel]).replace('\0', '0');
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	
	public List<Block> getChain() {
		return this.chain;
	}

	public Block getLastBlock() {
		return this.chain.get(this.chain.size() - 1);
	}

}
