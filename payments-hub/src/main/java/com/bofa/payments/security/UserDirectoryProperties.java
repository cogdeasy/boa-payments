package com.bofa.payments.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Online-banking user directory, fed from the core-banking user store export.
 */
@ConfigurationProperties(prefix = "boa.security")
public class UserDirectoryProperties {

    private List<Entry> users = new ArrayList<>();

    public List<Entry> getUsers() { return users; }
    public void setUsers(List<Entry> users) { this.users = users; }

    public static class Entry {
        private String username;
        private String password;
        private String role;
        private String cif;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getCif() { return cif; }
        public void setCif(String cif) { this.cif = cif; }
    }
}
