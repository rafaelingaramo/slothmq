package org.slothmq.server.user;

import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String userName;
    private String[] accessGroups;
    private String passkey;
    private Boolean active;

    public User(UUID id, String name, String userName, String[] accessGroups, Boolean active) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.accessGroups = accessGroups;
        this.active = active;
    }

    public User() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String[] getAccessGroups() {
        return accessGroups;
    }

    public void setAccessGroups(String[] accessGroups) {
        this.accessGroups = accessGroups;
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
