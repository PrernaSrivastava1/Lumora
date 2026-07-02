package com.semanticvault.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDocument {
    private String text;
    private Map<String, String> metadata;
    private int pageCount;
}
