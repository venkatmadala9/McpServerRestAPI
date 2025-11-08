package com.venkat.boot;

import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.venkat.boot.service.APIService;
import com.venkat.boot.service.ShipmentTools;

@SpringBootApplication
public class McpServerRestApi {

	public static void main(String[] args) {
		SpringApplication.run(McpServerRestApi.class, args);
	}

	/*
	 * @Bean public List<ToolCallback> toolCallbacks1(APIService apiservice) {
	 * return List.of(ToolCallbacks.from(apiservice)); }
	 */
	
	@Bean
	public List<ToolCallback> toolCallbacks1(ShipmentTools apiservice) {
		return List.of(ToolCallbacks.from(apiservice));
	}
}
