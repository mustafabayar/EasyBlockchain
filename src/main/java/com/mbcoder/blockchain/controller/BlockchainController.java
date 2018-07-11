package com.mbcoder.blockchain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mbcoder.blockchain.model.Block;
import com.mbcoder.blockchain.model.BlockchainResponse;
import com.mbcoder.blockchain.model.Transaction;
import com.mbcoder.blockchain.service.BlockchainService;

@RestController
public class BlockchainController {
	
	public static final int MINE_REWARD = 1;
	
	@Autowired
	BlockchainService blockchainService;
	
	@GetMapping(path="/mine")
	public ResponseEntity<?> mine() {
		// We run the proof of work algorithm to get the next proof
		Block lastBlock = blockchainService.getLastBlock();
		int lastProof = lastBlock.getProof();
		int proof = blockchainService.proofOfWork(lastProof);
		
		// We must receive a reward for finding the proof.
		// The sender is "Blockchain" to signify that this node has mined a new coin.
		blockchainService.createNewTransaction("Blockchain", "Miner", MINE_REWARD);
		
		// Forge the new Block by adding it to the chain
		String previousHash = blockchainService.hashBlock(lastBlock);
		Block block = blockchainService.createNewBlock(proof, previousHash);
		
		return ResponseEntity.ok(block);
	}
	
	@PostMapping(path="/transactions/new")
	public ResponseEntity<?> newTransaction(@RequestBody Transaction transaction) {
		int index = blockchainService.addNewTransaction(transaction);
		return ResponseEntity.ok("Transaction will be added to Block " + index);
	}
	
	@GetMapping(path="/chain")
	public ResponseEntity<?> chain() {
		BlockchainResponse response = new BlockchainResponse();
		response.setLength(blockchainService.getChain().size());
		response.setChain(blockchainService.getChain());
		return ResponseEntity.ok(response);
	}
}
