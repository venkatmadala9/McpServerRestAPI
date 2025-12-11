package com.venkat.boot.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class APIController {

	@GetMapping("/shipment/{id}")
	public ResponseEntity<Map<String, Object>> getShipment(@PathVariable String id){

		Map<String, Object> shipment = new HashMap<>();
		
		shipment.put("shipmentId", id);
		shipment.put("status", "IN_TRANSIT");
		
		return ResponseEntity.ok(shipment);
	}
}
