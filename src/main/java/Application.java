import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 * 
 * This application demonstrates LDAP integration with:
 * - Forum Systems free LDAP test server
 * - User authentication and authorization
 * - Group membership and permissions
 * - REST API endpoints for LDAP operations
 */
@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting LDAP Integration Application...");
        System.out.println("📡 Connecting to Forum Systems LDAP Server");
        System.out.println("🌐 REST API will be available at: http://localhost:8080/api/ldap");
        
        SpringApplication.run(Application.class, args);
    }
}