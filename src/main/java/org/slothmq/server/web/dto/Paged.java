package org.slothmq.server.web.dto;

import java.util.List;

public record Paged<T>(List<T> content,
                       int totalPages,
                       int totalElements) {

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
