import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

/**
 * Spring Boot LDAP Configuration
 * 
 * This configuration sets up LDAP authentication and provides
 * beans for LDAP operations in your Spring Boot application.
 */
@Configuration
@EnableWebSecurity
public class LdapConfig {
    
    // LDAP Configuration Properties
    // You can externalize these to application.properties
    @Value("${ldap.url:ldap://ldap.forumsys.com:389}")
    private String ldapUrl;
    
    @Value("${ldap.base:dc=example,dc=com}")
    private String ldapBase;
    
    @Value("${ldap.userDn:cn=read-only-admin,dc=example,dc=com}")
    private String ldapUserDn;
    
    @Value("${ldap.password:password}")
    private String ldapPassword;
    
    @Value("${ldap.userSearchBase:}")
    private String userSearchBase;
    
    @Value("${ldap.userSearchFilter:(uid={0})}")
    private String userSearchFilter;
    
    @Value("${ldap.groupSearchBase:}")
    private String groupSearchBase;
    
    @Value("${ldap.groupSearchFilter:(member={0})}")
    private String groupSearchFilter;
    
    /**
     * LDAP Context Source Configuration
     */
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapUserDn);
        contextSource.setPassword(ldapPassword);
        
        // Additional security settings
        contextSource.setPooled(true);
        contextSource.setReferral("follow");
        
        return contextSource;
    }
    
    /**
     * LDAP Template for programmatic LDAP operations
     */
    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
    
    /**
     * LDAP Authentication Provider
     */
    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider() {
        // User search configuration
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
            userSearchBase, userSearchFilter, contextSource());
        
        // Bind authenticator
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource());
        bindAuthenticator.setUserSearch(userSearch);
        
        // Create authentication provider
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(bindAuthenticator);
        
        // Optional: Add authorities populator for roles/groups
        // provider.setAuthoritiesPopulator(ldapAuthoritiesPopulator());
        
        return provider;
    }
    
    /**
     * Security Configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/public/**", "/health", "/actuator/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/scientists/**").hasRole("SCIENTISTS")
                .requestMatchers("/mathematicians/**").hasRole("MATHEMATICIANS")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Disable for testing - enable in production
        
        return http.build();
    }
    
    /**
     * Configure LDAP Authentication
     */
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(ldapAuthenticationProvider());
    }
    
    // Optional: Custom authorities populator for mapping LDAP groups to Spring Security roles
    /*
    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(
            contextSource(), groupSearchBase);
        populator.setGroupSearchFilter(groupSearchFilter);
        populator.setGroupRoleAttribute("ou");
        populator.setRolePrefix("ROLE_");
        populator.setConvertToUpperCase(true);
        return populator;
    }
    */
}