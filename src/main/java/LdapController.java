import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for LDAP operations
 * 
 * Provides REST endpoints for:
 * - User authentication
 * - User information retrieval
 * - Group membership queries
 * - Permission checks
 */
@RestController
@RequestMapping("/api/ldap")
@CrossOrigin(origins = "*") // Allow CORS for testing
public class LdapController {
    
    @Autowired
    private LdapService ldapService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "LDAP Integration");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Authenticate user
     */
    @PostMapping("/authenticate")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody AuthRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isAuthenticated = ldapService.authenticateUser(request.getUsername(), request.getPassword());
            
            if (isAuthenticated) {
                LdapService.UserSession session = ldapService.validateAndGetUserSession(request.getUsername());
                
                response.put("authenticated", true);
                response.put("user", session.getUser());
                response.put("groups", session.getGroups());
                response.put("permissions", session.getPermissions());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("authenticated", false);
                response.put("message", "Invalid credentials");
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get user information
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<LdapService.UserInfo> getUser(@PathVariable String username) {
        try {
            LdapService.UserInfo user = ldapService.findUser(username);
            
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get user groups
     */
    @GetMapping("/user/{username}/groups")
    public ResponseEntity<Set<String>> getUserGroups(@PathVariable String username) {
        try {
            Set<String> groups = ldapService.getUserGroups(username);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get user permissions
     */
    @GetMapping("/user/{username}/permissions")
    public ResponseEntity<Set<String>> getUserPermissions(@PathVariable String username) {
        try {
            Set<String> permissions = ldapService.getUserPermissions(username);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Check if user has specific permission
     */
    @GetMapping("/user/{username}/permission/{permission}")
    public ResponseEntity<Map<String, Boolean>> hasPermission(
            @PathVariable String username, 
            @PathVariable String permission) {
        try {
            boolean hasPermission = ldapService.hasPermission(username, permission);
            Map<String, Boolean> response = new HashMap<>();
            response.put("hasPermission", hasPermission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Check if user is in specific group
     */
    @GetMapping("/user/{username}/group/{groupName}")
    public ResponseEntity<Map<String, Boolean>> isUserInGroup(
            @PathVariable String username, 
            @PathVariable String groupName) {
        try {
            boolean inGroup = ldapService.isUserInGroup(username, groupName);
            Map<String, Boolean> response = new HashMap<>();
            response.put("inGroup", inGroup);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get all users in a group
     */
    @GetMapping("/group/{groupName}/users")
    public ResponseEntity<List<LdapService.UserInfo>> getUsersInGroup(@PathVariable String groupName) {
        try {
            List<LdapService.UserInfo> users = ldapService.getUsersInGroup(groupName);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get all available groups
     */
    @GetMapping("/groups")
    public ResponseEntity<List<String>> getAllGroups() {
        try {
            List<String> groups = ldapService.getAllGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get user session with all details
     */
    @GetMapping("/session/{username}")
    public ResponseEntity<LdapService.UserSession> getUserSession(@PathVariable String username) {
        try {
            LdapService.UserSession session = ldapService.validateAndGetUserSession(username);
            
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Request/Response classes
    public static class AuthRequest {
        private String username;
        private String password;
        
        // Constructors
        public AuthRequest() {}
        
        public AuthRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}