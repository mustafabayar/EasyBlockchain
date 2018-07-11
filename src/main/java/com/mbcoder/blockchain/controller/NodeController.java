package com.mbcoder.blockchain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mbcoder.blockchain.model.BlockchainResponse;
import com.mbcoder.blockchain.model.Node;
import com.mbcoder.blockchain.service.BlockchainService;

@RestController
public class NodeController {

	@Autowired
	BlockchainService blockchainService;

	@PostMapping(path = "/nodes/register")
	public ResponseEntity<?> register(@RequestBody Node node) {
		blockchainService.registerNode(node);
		return ResponseEntity.status(HttpStatus.CREATED).body(blockchainService.getNodes());
	}

	@GetMapping(path = "/nodes/resolve")
	public ResponseEntity<?> resolve() {
		boolean replaced = blockchainService.resolveConflicts();

		BlockchainResponse response = new BlockchainResponse();
		
		if (replaced) {
			response.setMessage("Chain is replaced");
		} else {
			response.setMessage("Chain is not replaced");
		}
		response.setLength(blockchainService.getChain().size());
		response.setChain(blockchainService.getChain());
		return ResponseEntity.ok(response);
	}

}
