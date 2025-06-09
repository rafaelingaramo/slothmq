package org.slothmq.server.user;

import org.bson.Document;

import java.util.UUID;

public class UserMapper {
    public static User from(Document document) {
        if (document == null) {
            throw new RuntimeException("Null document provided");
        }

        User user = new User(
                UUID.fromString(document.get("id", String.class)),
                document.get("name", String.class),
                document.get("userName", String.class),
                document.get("accessGroups", String.class).split(","),
                document.get("active", Boolean.class));
        user.setAccessGroups(document.get("accessGroups", String.class).split(","));
        return user;
    }
}
