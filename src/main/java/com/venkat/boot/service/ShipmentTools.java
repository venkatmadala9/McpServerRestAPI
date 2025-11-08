package com.venkat.boot.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.venkat.boot.config.AuthorizedApiClient;

@Service
public class ShipmentTools {

    private final AuthorizedApiClient authorizedApiClient;

    public ShipmentTools(AuthorizedApiClient authorizedApiClient) {
        this.authorizedApiClient = authorizedApiClient;
    }

    // Replace @Tool/@ToolParam with your actual annotations if different
    @Tool(name = "getShipment", description = "Get shipment info by ID")
    public String getShipment(@ToolParam(description = "Shipment ID") Long id) {
        return authorizedApiClient.get("/LSP-TMS/v1/shipments/{Id}", id);
    }
}