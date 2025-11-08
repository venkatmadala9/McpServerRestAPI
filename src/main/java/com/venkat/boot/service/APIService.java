package com.venkat.boot.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class APIService {

	@Tool(description = "Get the instance info...........")
	public String getInfo(){
		return RestClient.create()
				.get()
				.uri("http://dbslsp-stg01-fr4:8083/info")
				.retrieve()
				.body(String.class);
	}
	
	@Tool(description = "Let me ping the instance")
	public String pingMe(){
		return RestClient.create()
				.get()
				.uri("http://dbslsp-stg01-fr4:8083/ping")
				.retrieve()
				.body(String.class);
	}
	
	@Tool(description = "Get shipment info by ID")
	public String getShipment(@ToolParam(description = "Shipment ID") Long Id ) {
		
		return RestClient.create("https://lsp-dev02-fr4.blujay.global:8096")
            .get()
            .uri("/LSP-TMS/v1/shipments/{Id}", Id)
            .header("Authorization", "Bearer " + "${token}")
            .retrieve()
            .body(String.class);

	}
}
