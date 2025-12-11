package com.venkat.boot.config;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Registers two primitive resources:
 *  - demo://answer  -> "42"
 *  - demo://now     -> System.currentTimeMillis()
 */
@Configuration
public class ResourceConfig {

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> primitiveResources() {

        // Resource 1: demo://answer -> "42"
        McpSchema.Resource answerResource = new McpSchema.Resource(
                "demo://answer",
                "Answer to everything",
                null, // title (optional)
                "A primitive numeric resource for demo purposes.",
                null  // annotations (optional)
        );

        var answerSpec = new McpServerFeatures.SyncResourceSpecification(
                answerResource,
                (exchange, readRequest) -> new McpSchema.ReadResourceResult(
                        List.of(
                                new McpSchema.TextResourceContents(
                                        answerResource.uri(),
                                        "42",
                                        "text/plain"
                                )
                        )
                )
        );


        // 2) System timestamp (ISO-8601), replacing epoch millis
        McpSchema.Resource timestamp = new McpSchema.Resource(
                "demo://timestamp",
                "System Timestamp",
                "Current system time in ISO-8601 format (e.g., 2025-11-11T16:26:47.123Z).",
                "text/plain",
                null
                );

        // Option A: ISO-8601 with offset + short zone tag in brackets
        DateTimeFormatter IST_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX ' [IST]'");

        // If you want just ISO with offset (no [IST]), use:
        // DateTimeFormatter IST_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        var tsSpec = new McpServerFeatures.SyncResourceSpecification(
                timestamp,
                (exchange, readRequest) -> {
                    String istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(IST_FORMATTER);
                    return new McpSchema.ReadResourceResult(
                            List.of(new McpSchema.TextResourceContents(
                                    timestamp.uri(), istNow, "text/plain"))
                    );
                }
        );


    // 3) demo://echo/{value} (emulated template)
    //    We register it as a normal resource, but the handler
    //    actually serves ANY demo://echo/<...> URI by echoing the tail.
    McpSchema.Resource echoAdvertised = new McpSchema.Resource(
            "demo://echo/{value}",
            "Echo (templated)",
            "Echoes back the {value} segment as text/plain. "
                       + "Type any 'demo://echo/<value>' in the Inspector Read form.",
            "text/plain",
            null
            );
    var echoSpec = new McpServerFeatures.SyncResourceSpecification(
            echoAdvertised,
            (exchange, readRequest) -> {
                String requested = readRequest.uri();
                String prefix = "demo://echo/";
                String echoed;

                if (requested.startsWith(prefix)) {
                    echoed = requested.substring(prefix.length());
                } else {
                    // allow entering 'demo://echo/{value}' (exact) as a fallback
                    echoed = parseEchoValueFallback(requested);
                }

                return new McpSchema.ReadResourceResult(
                        List.of(new McpSchema.TextResourceContents(
                                requested, echoed, "text/plain"))
                );
            }
    );

    return List.of(answerSpec, tsSpec, echoSpec);
}
    
private String parseEchoValueFallback(String uri) {
    try {
        URI u = URI.create(uri);
        String path = u.getPath();
        if (path != null && !path.isBlank()) {
            return path.startsWith("/") ? path.substring(1) : path;
        }
        String s = uri;
        int i = s.indexOf("demo://echo/");
        return (i >= 0) ? s.substring(i + "demo://echo/".length()) : s;
    } catch (Exception e) {
        return uri;
    }
}


}