package org.slothmq.server.user;

import java.util.UUID;

public record User(UUID id,
                   String name,
                   String userName,
                   String accessGroups,
                   String passkey,
                   Boolean active) {
}
