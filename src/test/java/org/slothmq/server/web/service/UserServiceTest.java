package org.slothmq.server.web.service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slothmq.exception.InvalidUserException;
import org.slothmq.server.user.User;
import org.slothmq.server.web.dto.PageRequest;
import org.slothmq.server.web.dto.Paged;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class UserServiceTest {
    private static final String USER_COLLECTION = "private.user.collection";
    private static final String BASE_USERNAME = "admin";
    private static final String BASE_USER_PASSKEY = "admin:admin";
    private static final String BASE_USER_ACCESS_GROUP = "admin";
    private static final String BASE_NAME = "Admin";

    @Test
    public void givenUserCollectionDoesNotExistMakeSureItsCreated() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        ListCollectionsIterable<Document> listCollectionIterable = mock(ListCollectionsIterable.class);
        MongoCollection<Document> userCollection = mock(MongoCollection.class);

        //stubs
        when(mongoDatabase.listCollections())
                .thenReturn(listCollectionIterable);
        doAnswer(invocation -> listCollectionIterable)
                .when(listCollectionIterable).filter(any());

        doNothing().when(mongoDatabase).createCollection(USER_COLLECTION);
        when(mongoDatabase.getCollection(USER_COLLECTION)).thenReturn(userCollection);
        when(userCollection.insertOne(any())).thenReturn(null);

        //when
        userService.initUserCollectionIfNeeded();

        //then
        verify(mongoDatabase, times(1)).createCollection(USER_COLLECTION);
        verify(mongoDatabase, times(1)).listCollections();
        verify(userCollection, times(1)).insertOne(argThat(doc -> doc.get("name").equals(BASE_NAME) &&
                doc.get("accessGroups").equals(BASE_USER_ACCESS_GROUP) &&
                doc.get("userName").equals(BASE_USERNAME) &&
                doc.get("active").equals(true) &&
                doc.get("passkey").equals(new String(Base64.getEncoder().encode(BASE_USER_PASSKEY.getBytes())))));
    }


    @Test
    public void givenUserCollectionDoesExistMakeSureItsNotCreated() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        ListCollectionsIterable<Document> listCollectionIterable = mock(ListCollectionsIterable.class);
        MongoCollection<Document> userCollection = mock(MongoCollection.class);

        //stubs
        when(listCollectionIterable.first()).thenReturn(new Document());
        when(mongoDatabase.listCollections())
                .thenReturn(listCollectionIterable);
        doAnswer(invocation -> listCollectionIterable)
                .when(listCollectionIterable).filter(any());

        doNothing().when(mongoDatabase).createCollection(USER_COLLECTION);
        when(mongoDatabase.getCollection(USER_COLLECTION)).thenReturn(userCollection);
        when(userCollection.insertOne(any())).thenReturn(null);

        //when
        userService.initUserCollectionIfNeeded();

        //then
        verify(mongoDatabase, times(0)).createCollection(USER_COLLECTION);
        verify(mongoDatabase, times(1)).listCollections();
        verify(userCollection, times(0)).insertOne(any());
    }

    @ParameterizedTest
    @CsvSource({"1,10,name,DESC,null", "1,10,null,ASC,null", "1,20,null,null,null", "1,30,name,ASC,gwen"})
    public void givenSortIsNotProvidedMakeSureResultIsCorrect(Integer page,
                                                              String pageSize,
                                                              String sortField,
                                                              String sortOrder,
                                                              String search) {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var pageRequest = PageRequest.parseOrGetDefaults(page, pageSize, Objects.equals(sortField, "null") ? null : sortField,
                Objects.equals(sortOrder, "null") ? null : sortOrder, Objects.equals(search, "null") ? null : search);
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        var collectionOfDocuments = new ArrayList<Document>();
        collectionOfDocuments.add(new Document("id", UUID.randomUUID().toString())
                .append("name", "peter")
                .append("userName", "peter_parker")
                .append("accessGroups", "viewer,admin")
                .append("active", true));
        collectionOfDocuments.add(new Document("id", UUID.randomUUID().toString())
                .append("name", "gwen")
                .append("userName", "gwen_stacy")
                .append("accessGroups", "admin")
                .append("active", false));

        //stubs
        when(findIterable.sort(any()))
                .thenReturn(findIterable);
        when(findIterable.skip(anyInt()))
                .thenReturn(findIterable);
        when(findIterable.limit(anyInt()))
                .thenReturn(findIterable);
        when(findIterable.into(any()))
                .thenReturn(collectionOfDocuments);

        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);
        when(userCollection.countDocuments(any(Bson.class)))
                .thenReturn(2L);
        when(userCollection.find(any(Bson.class)))
                .thenReturn(findIterable);

        //when
        Paged<User> userPaged = userService.listPaged(pageRequest);

        //then
        assert userPaged.content().size() == 2;
        assert userPaged.totalPages() == 1;
        assert userPaged.totalElements() == 2;

        for(int i=0;i<userPaged.content().size();i++) {
            assert Objects.equals(collectionOfDocuments.get(i).get("name"), userPaged.content().get(i).getName());
            assert Objects.equals(collectionOfDocuments.get(i).get("userName"), userPaged.content().get(i).getUserName());
            assert Objects.equals(collectionOfDocuments.get(i).get("accessGroups"), String.join(",", userPaged.content().get(i).getAccessGroups()));
            assert Objects.equals(collectionOfDocuments.get(i).get("active"), userPaged.content().get(i).getActive());
        }
    }

    @ParameterizedTest
    @CsvSource({"name,userName,null", "name,null,accessGroups", "null,userName,accessGroups"})
    public void givenInvalidUserAttributesCheckThrownException(String name, String userName, String accessGroups) {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var toBeInsertedUser = new User(null, Objects.equals(name, "null") ? null : name,
                Objects.equals(userName, "null") ? null : userName,
                Objects.equals(accessGroups, "null") ? null : new String[1],
                true);
        //when
        Assertions.assertThrows(InvalidUserException.class, () -> userService.insertOne(toBeInsertedUser));
    }

    @Test
    public void insertOne() {
        
    }
}
