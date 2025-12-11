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
    

    @Tool(name = "createShipment", description = "Create a new shipment using POST API")
    public String createShipment(@ToolParam(description = "Shipment request JSON") String shipmentJson) {
        // POST call with JSON body
        return authorizedApiClient.post("/LSP-TMS/v1/shipments", shipmentJson);
    }

    @Tool(name = "getTrip", description = "Get Trip info by ID")
    public String getTrip(@ToolParam(description = "Shipment ID") Long id) {
        return authorizedApiClient.get("/LSP-TMS/v1/trips/{Id}", id);
    }
}