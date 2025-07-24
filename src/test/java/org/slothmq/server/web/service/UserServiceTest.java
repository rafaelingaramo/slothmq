package org.slothmq.server.web.service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonString;
import org.bson.BsonValue;
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

import java.util.*;

import static org.mockito.Mockito.*;
import static org.slothmq.server.web.service.UserService.USER_COLLECTION;

public class UserServiceTest {
    private static final String BASE_USERNAME = "admin";
    private static final String BASE_USER_PASSKEY = "admin:admin";
    private static final String BASE_USER_ACCESS_GROUP = "admin";
    private static final String BASE_NAME = "Admin";
//TODO flaky?
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
                doc.get("passKey").equals(new String(Base64.getEncoder().encode(BASE_USER_PASSKEY.getBytes())))));
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
    public void givenInvalidUserAttributesCheckThrownExceptionOnInsertOne(String name, String userName, String accessGroups) {
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
    public void givenInsertOneIsCalledCheckCallsAndArguments() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var expectedId = UUID.randomUUID().toString();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);

        when(userCollection.insertOne(any()))
                .thenReturn(new InsertOneResult() {
                    @Override
                    public boolean wasAcknowledged() {
                        return true;
                    }

                    @Override
                    public BsonValue getInsertedId() {
                        return new BsonString("lorem");
                    }
                });
        when(userCollection.find(any(Document.class)))
                .thenReturn(findIterable);
        when(findIterable.first()).thenReturn(new Document("id", expectedId)
                .append("name", "lorem")
                .append("passKey", "lorem")
                .append("userName", "ipsum")
                .append("active", true)
                .append("accessGroups", "admin"));

        //when
        User user = userService.insertOne(new User(null, "lorem", "ipsum", new String[]{"admin"}, true));

        //then
        assert user.getId() != null;
        assert Objects.equals(user.getName(), "lorem");
        assert Objects.equals(user.getUserName(), "ipsum");
        assert Arrays.equals(user.getAccessGroups(), new String[]{"admin"});
        verify(mongoDatabase, times(1)).getCollection(USER_COLLECTION);
        verify(userCollection, times(1)).insertOne(argThat(item ->
           item.get("name").equals("lorem") &&
                item.get("userName").equals("ipsum") &&
                item.get("active") == Boolean.TRUE &&
                item.get("accessGroups").equals("admin")
        ));

        verify(userCollection, times(1)).find(any(Document.class));
        verify(findIterable, times(1)).first();
    }

    @Test
    public void givenRemoveOneIsAbleToRemoveNoExceptionIsThrown() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var identifier = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);
        when(userCollection.deleteOne(any()))
                .thenReturn(new DeleteResult() {
                    @Override
                    public boolean wasAcknowledged() {
                        return true;
                    }

                    @Override
                    public long getDeletedCount() {
                        return 1;
                    }
                });

        //when
        Assertions.assertDoesNotThrow(() -> userService.removeOne(identifier.toString()));
    }


    @Test
    public void givenRemoveOneIsNotAbleToRemoveExceptionIsThrown() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var identifier = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);
        when(userCollection.deleteOne(any()))
                .thenReturn(new DeleteResult() {
                    @Override
                    public boolean wasAcknowledged() {
                        return false;
                    }

                    @Override
                    public long getDeletedCount() {
                        return 0;
                    }
                });

        //when
        Assertions.assertThrows(AssertionError.class, () -> userService.removeOne(identifier.toString()));
    }

    @Test
    public void givenEditOneIsCorrectlyCalledCheckCalls() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var id = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        var databaseRecord = new Document("id" , id.toString())
                .append("name", "peter")
                .append("userName", "peter")
                .append("active", true)
                .append("accessGroups", "viewer");
        var theReplacement = new User(id, "eddie brock", "venom", new String[]{"admin"}, true);

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION)).thenReturn(userCollection);
        when(userCollection.find(any(Document.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(databaseRecord);
        when(userCollection.replaceOne(any(), any())).thenReturn(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 0;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        //when
        userService.editOne(id.toString(), theReplacement);

        //then
        verify(mongoDatabase, times(1)).getCollection(USER_COLLECTION);
        verify(userCollection, times(1)).find(eq(new Document("id", id.toString())));
        verify(findIterable, times(1)).first();
        verify(userCollection, times(1)).replaceOne(eq(new Document("id", id.toString())), argThat(item ->
                item.get("name").equals("eddie brock") && item.get("userName").equals("venom") && item.get("accessGroups").equals("admin")
        ));
    }

    @Test
    public void givenEditOneFailsToUpdateCheckAssertions() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var id = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        var databaseRecord = new Document("id" , id.toString())
                .append("name", "peter")
                .append("userName", "peter")
                .append("active", true)
                .append("accessGroups", "viewer");
        var theReplacement = new User(id, "eddie brock", "venom", new String[]{"admin"}, true);

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION)).thenReturn(userCollection);
        when(userCollection.find(any(Document.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(databaseRecord);
        when(userCollection.replaceOne(any(), any())).thenReturn(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 0;
            }

            @Override
            public long getModifiedCount() {
                return 0;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        //when
        Assertions.assertThrows(AssertionError.class, () -> userService.editOne(id.toString(), theReplacement));

        //then
        verify(mongoDatabase, times(1)).getCollection(USER_COLLECTION);
        verify(userCollection, times(1)).find(eq(new Document("id", id.toString())));
        verify(findIterable, times(1)).first();
        verify(userCollection, times(1)).replaceOne(eq(new Document("id", id.toString())), argThat(item ->
                item.get("name").equals("eddie brock") && item.get("userName").equals("venom") && item.get("accessGroups").equals("admin")
        ));
    }


    @Test
    public void givenFindOneIsCalledCheckCallsAndResponse() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var id = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        var databaseRecord = new Document("id" , id.toString())
                .append("name", "peter")
                .append("userName", "peter")
                .append("active", true)
                .append("accessGroups", "viewer");

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);
        when(userCollection.find(any(Document.class)))
                .thenReturn(findIterable);
        when(findIterable.first()).thenReturn(databaseRecord);

        //when
        User one = userService.findOne(id.toString());

        //then
        assert Objects.equals(one.getName(), "peter");
        assert Objects.equals(one.getUserName(), "peter");
        assert one.getActive();
        assert Arrays.equals(one.getAccessGroups(), new String[]{"viewer"});

        verify(mongoDatabase, times(1)).getCollection(USER_COLLECTION);
        verify(userCollection, times(1)).find(any(Document.class));
        verify(findIterable, times(1)).first();
    }

    @Test
    public void givenChangePasswordIsCalledVerifyCallsAndArguments() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var id = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        var databaseRecord = new Document("id" , id.toString())
                .append("name", "peter")
                .append("userName", "peter")
                .append("active", true)
                .append("accessGroups", "viewer");
        var user = new User();
        user.setPasskey("maryjane");

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);
        when(userCollection.find(any(Document.class)))
                .thenReturn(findIterable);
        when(findIterable.first()).thenReturn(databaseRecord);
        when(userCollection.replaceOne(any(Document.class), any())).thenReturn(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 0;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });

        //when
        userService.changePassword(id.toString(), user);

        //then
        verify(mongoDatabase, times(1)).getCollection(USER_COLLECTION);
        verify(userCollection, times(1)).find(any(Document.class));
        verify(findIterable, times(1)).first();
        verify(userCollection, times(1)).replaceOne(eq(new Document("id", id.toString())), argThat(item ->
                item.get("passKey").equals(new String(Base64.getEncoder().encode(("peter:maryjane").getBytes())))
        ));
    }

    @Test
    public void givenLoginIsCalledMakeSureCallsAndArgumentsAreCorrect() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var userService = new UserService(mongoDatabase);
        var id = UUID.randomUUID();
        MongoCollection<Document> userCollection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        var databaseRecord = new Document("id" , id.toString())
                .append("name", "peter")
                .append("userName", "peter")
                .append("active", true)
                .append("accessGroups", "viewer");
        var credentials = new String(Base64.getEncoder().encode("maryjane:peter".getBytes()));

        //stubs
        when(mongoDatabase.getCollection(USER_COLLECTION))
                .thenReturn(userCollection);
        when(userCollection.find(any(Document.class)))
                .thenReturn(findIterable);
        when(findIterable.first()).thenReturn(databaseRecord);

        //when
        User loggedUser = userService.login(credentials);

        //then
        verify(mongoDatabase, times(1)).getCollection(USER_COLLECTION);
        verify(findIterable, times(1)).first();
        verify(userCollection, times(1)).find(eq(new Document("passKey", credentials)));

        assert Objects.equals(loggedUser.getName(), "peter");
        assert Objects.equals(loggedUser.getUserName(), "peter");
        assert loggedUser.getActive();
        assert Arrays.equals(loggedUser.getAccessGroups(), new String[]{"viewer"});
    }
}
