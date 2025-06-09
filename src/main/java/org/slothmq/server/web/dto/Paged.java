package org.slothmq.server.web.dto;

import java.util.List;

public record Paged<T>(List<T> content,
                       long totalPages,
                       long totalElements) {

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
