package org.slothmq.server.user;

import java.util.UUID;

public record User(UUID id,
                   String name,
                   String accessGroups,
                   String passkey) {
}
