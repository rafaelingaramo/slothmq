package org.slothmq.server.user;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.UUID;

public class UserMapperTest {
    @ParameterizedTest

    @CsvSource({"lorem ipsum,lorem.ipsum,viewer,true",
                "peter parker,peter.parker,admin,true",
                "mary jane,mary.jane,viewer,false"})
    public void givenCorrectInformationValidateTranslation(String name, String userName, String accessGroups, String active) {
        //given
        var isActive = Boolean.parseBoolean(active);
        var id = UUID.randomUUID().toString();
        var document = new Document("id", id)
                .append("name", name)
                .append("userName", userName)
                .append("accessGroups", accessGroups)
                .append("active", isActive);

        //when
        User from = UserMapper.from(document);

        //then
        assert from.getId().toString().equals(id);
        assert from.getName().equals(name);
        assert from.getUserName().equals(userName);
        assert Arrays.stream(from.getAccessGroups()).allMatch(a -> Arrays.asList(accessGroups.split(",")).contains(a));
        assert from.getActive().equals(isActive);
    }

    @Test
    public void givenNullDocumentExpectException() {
        //when
        Assertions.assertThrows(RuntimeException.class, () -> {
           UserMapper.from(null);
        });
    }

}
