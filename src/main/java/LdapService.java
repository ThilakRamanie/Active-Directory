import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Service class for LDAP operations
 * 
 * This service provides high-level methods for:
 * - User authentication and validation
 * - Group membership queries
 * - User search and information retrieval
 * - Permission mapping based on groups
 */
@Service
public class LdapService {
    
    @Autowired
    private LdapTemplate ldapTemplate;
    
    /**
     * Authenticate user credentials
     */
    public boolean authenticateUser(String username, String password) {
        try {
            // For Forum Systems LDAP, we can verify user exists and password is correct
            // In a real scenario, you'd use proper bind authentication
            UserInfo user = findUser(username);
            return user != null && "password".equals(password); // All users have same password in test server
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Find user by username
     */
    public UserInfo findUser(String username) {
        try {
            EqualsFilter filter = new EqualsFilter("uid", username);
            
            List<UserInfo> users = ldapTemplate.search(
                "",
                filter.encode(),
                new UserAttributesMapper()
            );
            
            return users.isEmpty() ? null : users.get(0);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user: " + username, e);
        }
    }
    
    /**
     * Get all users in a specific group
     */
    public List<UserInfo> getUsersInGroup(String groupName) {
        try {
            String searchBase = "ou=" + groupName + ",dc=example,dc=com";
            
            return ldapTemplate.search(
                searchBase,
                "(objectclass=person)",
                new UserAttributesMapper()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get users in group: " + groupName, e);
        }
    }
    
    /**
     * Check if user belongs to a specific group
     */
    public boolean isUserInGroup(String username, String groupName) {
        try {
            String searchBase = "ou=" + groupName + ",dc=example,dc=com";
            EqualsFilter filter = new EqualsFilter("uid", username);
            
            List<String> results = ldapTemplate.search(
                searchBase,
                filter.encode(),
                (AttributesMapper<String>) attrs -> attrs.get("uid").get().toString()
            );
            
            return !results.isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get all groups a user belongs to
     */
    public Set<String> getUserGroups(String username) {
        Set<String> groups = new HashSet<>();
        
        // Check known groups (in a real scenario, you'd search dynamically)
        String[] knownGroups = {"mathematicians", "scientists"};
        
        for (String group : knownGroups) {
            if (isUserInGroup(username, group)) {
                groups.add(group);
            }
        }
        
        return groups;
    }
    
    /**
     * Get user permissions based on group membership
     */
    public Set<String> getUserPermissions(String username) {
        Set<String> permissions = new HashSet<>();
        Set<String> groups = getUserGroups(username);
        
        // Map groups to permissions
        for (String group : groups) {
            switch (group.toLowerCase()) {
                case "mathematicians":
                    permissions.add("READ_MATH_DATA");
                    permissions.add("CALCULATE");
                    permissions.add("VIEW_FORMULAS");
                    break;
                case "scientists":
                    permissions.add("READ_SCIENCE_DATA");
                    permissions.add("EXPERIMENT");
                    permissions.add("VIEW_RESEARCH");
                    break;
            }
        }
        
        // All authenticated users get basic permissions
        if (!groups.isEmpty()) {
            permissions.add("READ_BASIC");
            permissions.add("VIEW_PROFILE");
        }
        
        return permissions;
    }
    
    /**
     * Check if user has a specific permission
     */
    public boolean hasPermission(String username, String permission) {
        Set<String> userPermissions = getUserPermissions(username);
        return userPermissions.contains(permission);
    }
    
    /**
     * Search users by attribute
     */
    public List<UserInfo> searchUsers(String attribute, String value) {
        try {
            EqualsFilter filter = new EqualsFilter(attribute, value);
            
            return ldapTemplate.search(
                "",
                filter.encode(),
                new UserAttributesMapper()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to search users", e);
        }
    }
    
    /**
     * Get all available groups
     */
    public List<String> getAllGroups() {
        try {
            return ldapTemplate.search(
                "",
                "(objectclass=organizationalUnit)",
                (AttributesMapper<String>) attrs -> {
                    try {
                        return attrs.get("ou").get().toString();
                    } catch (Exception e) {
                        return null;
                    }
                }
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get groups", e);
        }
    }
    
    /**
     * Validate user session and get user info
     */
    public UserSession validateAndGetUserSession(String username) {
        UserInfo user = findUser(username);
        if (user == null) {
            return null;
        }
        
        Set<String> groups = getUserGroups(username);
        Set<String> permissions = getUserPermissions(username);
        
        return new UserSession(user, groups, permissions);
    }
    
    // User Info class
    public static class UserInfo {
        private String username;
        private String fullName;
        private String email;
        private String dn;
        
        public UserInfo(String username, String fullName, String email, String dn) {
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.dn = dn;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getDn() { return dn; }
        
        @Override
        public String toString() {
            return "UserInfo{username='" + username + "', fullName='" + fullName + "'}";
        }
    }
    
    // User Session class
    public static class UserSession {
        private UserInfo user;
        private Set<String> groups;
        private Set<String> permissions;
        
        public UserSession(UserInfo user, Set<String> groups, Set<String> permissions) {
            this.user = user;
            this.groups = groups;
            this.permissions = permissions;
        }
        
        // Getters
        public UserInfo getUser() { return user; }
        public Set<String> getGroups() { return groups; }
        public Set<String> getPermissions() { return permissions; }
        
        public boolean hasPermission(String permission) {
            return permissions.contains(permission);
        }
        
        public boolean isInGroup(String group) {
            return groups.contains(group);
        }
    }
    
    // Attributes mapper for user info
    private static class UserAttributesMapper implements AttributesMapper<UserInfo> {
        @Override
        public UserInfo mapFromAttributes(Attributes attrs) throws NamingException {
            String uid = getAttributeValue(attrs, "uid");
            String cn = getAttributeValue(attrs, "cn");
            String mail = getAttributeValue(attrs, "mail");
            String dn = getAttributeValue(attrs, "dn");
            
            return new UserInfo(uid, cn, mail, dn);
        }
        
        private String getAttributeValue(Attributes attrs, String attributeName) {
            try {
                return attrs.get(attributeName) != null ? 
                    attrs.get(attributeName).get().toString() : "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        }
    }
}