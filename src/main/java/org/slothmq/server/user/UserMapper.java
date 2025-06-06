package org.slothmq.server.user;

import org.bson.Document;

import java.util.UUID;

public class UserMapper {
    public static User from(Document document) {
        if (document == null) {
            throw new RuntimeException("Null document provided");
        }

        return new User(UUID.fromString(document.get("id", String.class)),
                document.get("name", String.class),
                document.get("accessGroup", String.class),
                null,
                document.get("active", Boolean.class));
    }
}
