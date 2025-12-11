
package com.venkat.boot.service;

import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Prompt-driven MCP tool:
 * - Accepts natural language input.
 * - Extracts shipment ID (regex first, fallback to LLM).
 * - Calls ShipmentTools.getShipment(id).
 * - Summarizes shipment info using local Ollama model.
 */
@Service
public class PromptTools {

    private final ChatModel chatModel;
    private final ShipmentTools shipmentTools;
    private static final Pattern DIGITS = Pattern.compile("\\b\\d+\\b");

    public PromptTools(ChatModel chatModel, ShipmentTools shipmentTools) {
        this.chatModel = chatModel;
        this.shipmentTools = shipmentTools;
    }

    @Tool(name = "shipmentPrompt",
          description = "Understands user query, extracts shipment ID, fetches info, and summarizes it.")
    public String shipmentPrompt(@ToolParam(description = "User query in natural language")
                                 String userQuery) {

        // 1) Regex extraction
        Long shipmentId = tryRegexExtract(userQuery);

        // 2) Fallback to LLM extraction
        if (shipmentId == null) {
            String extractPrompt =
                    "Extract the shipment ID number from this text. Return ONLY the digits. " +
                    "If no shipment ID is present, return NONE.\n\nText: \"" + userQuery + "\"";

            String llmOut = chatModel.call(extractPrompt).trim();

            if (llmOut.equalsIgnoreCase("NONE") || llmOut.isEmpty()) {
                return "I couldn't find a shipment ID in your message. Please include a numeric ID.";
            }

            try {
                shipmentId = Long.parseLong(llmOut);
            } catch (NumberFormatException nfe) {
                shipmentId = tryRegexExtract(llmOut);
                if (shipmentId == null) {
                    return "I couldn't reliably extract a numeric shipment ID. Please include a number like '12345'.";
                }
            }
        }

        // 3) Fetch shipment info
        String shipmentInfo;
        try {
            shipmentInfo = shipmentTools.getShipment(shipmentId);
        } catch (Exception ex) {
            return "Failed to fetch shipment " + shipmentId + " (" + ex.getMessage() + ").";
        }

        if (shipmentInfo == null || shipmentInfo.isBlank()) {
            return "No data returned for shipment " + shipmentId + ".";
        }

        // 4) Summarize
        String summarizePrompt =
                "Summarize this shipment info for a non-technical user. Include ID, status, origin, destination, carrier, ETA if present.\n\n" +
                "Shipment ID: " + shipmentId + "\nRaw Data:\n" + shipmentInfo;

        String summary = chatModel.call(summarizePrompt).trim();
        return summary.isEmpty() ? shipmentInfo : summary;
    }

    private Long tryRegexExtract(String text) {
        Matcher m = DIGITS.matcher(text);
        if (m.find()) {
            try {
                return Long.parseLong(m.group());
            } catch (NumberFormatException ignored) {
                while (m.find()) {
                    try {
                        return Long.parseLong(m.group());
                    } catch (NumberFormatException ignoredToo) { }
                }
            }
        }
        return null;
    }
}