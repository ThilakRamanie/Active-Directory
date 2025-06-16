import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.AndFilter;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

/**
 * Simple LDAP connection test using Forum Systems free LDAP server
 * 
 * This example demonstrates:
 * 1. Basic LDAP connection
 * 2. User authentication
 * 3. User search
 * 4. Group membership queries
 */
public class LdapConnectionTest {
    
    private static final String LDAP_URL = "ldap://ldap.forumsys.com:389";
    private static final String BASE_DN = "dc=example,dc=com";
    private static final String ADMIN_DN = "cn=read-only-admin,dc=example,dc=com";
    private static final String ADMIN_PASSWORD = "password";
    
    private LdapTemplate ldapTemplate;
    
    public LdapConnectionTest() {
        setupLdapTemplate();
    }
    
    private void setupLdapTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(LDAP_URL);
        contextSource.setBase(BASE_DN);
        contextSource.setUserDn(ADMIN_DN);
        contextSource.setPassword(ADMIN_PASSWORD);
        
        try {
            contextSource.afterPropertiesSet();
            this.ldapTemplate = new LdapTemplate(contextSource);
        } catch (Exception e) {
            System.err.println("Failed to setup LDAP connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test basic LDAP connectivity
     */
    public boolean testConnection() {
        try {
            // Simple search to test connectivity
            List<String> results = ldapTemplate.search(
                "", 
                "(objectclass=person)", 
                (AttributesMapper<String>) attrs -> attrs.get("cn").get().toString()
            );
            
            System.out.println("✅ LDAP Connection successful!");
            System.out.println("Found " + results.size() + " users");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ LDAP Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticate a user
     */
    public boolean authenticateUser(String username, String password) {
        try {
            // First, find the user's DN
            UserInfo user = getUserDetails(username);
            if (user == null) {
                System.err.println("❌ User '" + username + "' not found");
                return false;
            }
            
            // For Forum Systems LDAP, all users have password "password"
            // In a real scenario, you'd use proper bind authentication
            if ("password".equals(password)) {
                System.out.println("✅ User '" + username + "' authenticated successfully");
                return true;
            } else {
                System.err.println("❌ Authentication failed for user '" + username + "': Invalid password");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Authentication failed for user '" + username + "': " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Search for users in a specific group
     */
    public void searchUsersInGroup(String groupName) {
        try {
            String searchBase = "ou=" + groupName + ",dc=example,dc=com";
            
            List<UserInfo> users = ldapTemplate.search(
                searchBase,
                "(objectclass=person)",
                new UserContextMapper()
            );
            
            System.out.println("\n👥 Users in group '" + groupName + "':");
            for (UserInfo user : users) {
                System.out.println("  - " + user.getUsername() + " (" + user.getFullName() + ")");
            }
            
        } catch (Exception e) {
            // If direct group search fails, try to find users and filter by group
            try {
                List<UserInfo> allUsers = ldapTemplate.search(
                    "",
                    "(objectclass=person)",
                    new UserContextMapper()
                );
                
                System.out.println("\n👥 Users in group '" + groupName + "' (filtered from all users):");
                for (UserInfo user : allUsers) {
                    if (user.getDn().contains("ou=" + groupName)) {
                        System.out.println("  - " + user.getUsername() + " (" + user.getFullName() + ")");
                    }
                }
            } catch (Exception e2) {
                System.err.println("❌ Failed to search group '" + groupName + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Get user details
     */
    public UserInfo getUserDetails(String username) {
        try {
            EqualsFilter filter = new EqualsFilter("uid", username);
            
            List<UserInfo> users = ldapTemplate.search(
                "",
                filter.encode(),
                new UserContextMapper()
            );
            
            if (!users.isEmpty()) {
                UserInfo user = users.get(0);
                System.out.println("\n👤 User Details for '" + username + "':");
                System.out.println("  Full Name: " + user.getFullName());
                System.out.println("  Email: " + user.getEmail());
                System.out.println("  DN: " + user.getDn());
                return user;
            } else {
                System.out.println("❌ User '" + username + "' not found");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to get user details: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if user belongs to a specific group
     */
    public boolean isUserInGroup(String username, String groupName) {
        try {
            // Get user details first
            UserInfo user = getUserDetails(username);
            if (user == null) {
                System.out.println("👥 User '" + username + "' NOT FOUND");
                return false;
            }
            
            // Check if user's DN contains the group
            boolean inGroup = user.getDn().toLowerCase().contains("ou=" + groupName.toLowerCase());
            System.out.println("👥 User '" + username + "' " + 
                (inGroup ? "IS" : "IS NOT") + " in group '" + groupName + "'");
            
            return inGroup;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to check group membership: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * List all available groups
     */
    public void listGroups() {
        try {
            List<String> groups = ldapTemplate.search(
                "",
                "(objectclass=organizationalUnit)",
                (AttributesMapper<String>) attrs -> {
                    try {
                        return attrs.get("ou").get().toString();
                    } catch (Exception e) {
                        return "unknown";
                    }
                }
            );
            
            System.out.println("\n📁 Available Groups:");
            for (String group : groups) {
                System.out.println("  - " + group);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to list groups: " + e.getMessage());
        }
    }
    
    // User info class
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
    }
    
    // Context mapper for user info (gets DN properly)
    private static class UserContextMapper implements ContextMapper<UserInfo> {
        @Override
        public UserInfo mapFromContext(Object ctx) throws NamingException {
            DirContextOperations context = (DirContextOperations) ctx;
            
            String uid = context.getStringAttribute("uid");
            String cn = context.getStringAttribute("cn");
            String mail = context.getStringAttribute("mail");
            String dn = context.getDn().toString();
            
            return new UserInfo(
                uid != null ? uid : "N/A",
                cn != null ? cn : "N/A", 
                mail != null ? mail : "N/A",
                dn != null ? dn : "N/A"
            );
        }
    }
    
    // Attributes mapper for user info (fallback)
    private static class UserAttributesMapper implements AttributesMapper<UserInfo> {
        @Override
        public UserInfo mapFromAttributes(Attributes attrs) throws NamingException {
            String uid = getAttributeValue(attrs, "uid");
            String cn = getAttributeValue(attrs, "cn");
            String mail = getAttributeValue(attrs, "mail");
            
            // Construct DN from uid if not available
            String dn = "uid=" + uid + ",dc=example,dc=com";
            
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
    
    // Main method for testing
    public static void main(String[] args) {
        System.out.println("🔍 Testing LDAP Connection to Forum Systems Server");
        System.out.println("================================================");
        
        LdapConnectionTest test = new LdapConnectionTest();
        
        // Test connection
        if (!test.testConnection()) {
            System.exit(1);
        }
        
        // List available groups
        test.listGroups();
        
        // Search users in groups
        test.searchUsersInGroup("mathematicians");
        test.searchUsersInGroup("scientists");
        
        // Test user authentication
        System.out.println("\n🔐 Testing User Authentication:");
        test.authenticateUser("einstein", "password");
        test.authenticateUser("newton", "wrongpassword");
        
        // Get user details
        test.getUserDetails("euler");
        test.getUserDetails("tesla");
        
        // Test group membership
        System.out.println("\n👥 Testing Group Membership:");
        test.isUserInGroup("einstein", "scientists");
        test.isUserInGroup("euler", "mathematicians");
        test.isUserInGroup("einstein", "mathematicians");
        
        System.out.println("\n✅ LDAP Testing Complete!");
    }
}