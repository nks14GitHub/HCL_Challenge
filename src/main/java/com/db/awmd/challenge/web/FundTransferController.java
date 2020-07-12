package com.db.awmd.challenge.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.exception.FundTransferException;
import com.db.awmd.challenge.service.FundTransferService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/fundtransfer")
@Slf4j
public class FundTransferController {
	@Autowired
	private FundTransferService fundTransferService;
	
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<Object> accountTransfer(@RequestBody @Valid MoneyTransferRequest fundTransfer) {
	    log.info("initiating FundTransfer.....");

	    try {
	    this.fundTransferService.fundTransferRequest(fundTransfer);
	    } catch (FundTransferException daie) {
	    	log.info("FundTransfer failed.....");
	      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
	    }
	    log.info("FundTransfer success.....");
	    return new ResponseEntity<>(HttpStatus.OK);
	  }

}
