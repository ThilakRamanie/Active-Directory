# JumpCloud Free Trial Setup Guide

JumpCloud offers a 30-day free trial with full access to their cloud directory platform, including LDAP services. This is perfect for more advanced testing scenarios.

## 🚀 Getting Started with JumpCloud

### Step 1: Sign Up for Free Trial

1. Visit [JumpCloud](https://jumpcloud.com/)
2. Click "Start Free Trial" or "Get Started"
3. Fill out the registration form with your business email
4. Verify your email address
5. Complete the initial setup wizard

### Step 2: Configure Your Directory

1. **Add Users**:
   - Go to "User Management" → "Users"
   - Click "+" to add new users
   - Fill in user details (name, email, username)
   - Set temporary passwords

2. **Create Groups**:
   - Go to "User Management" → "User Groups"
   - Click "+" to create new groups
   - Add users to appropriate groups
   - Set group permissions and attributes

3. **Enable LDAP**:
   - Go to "Directory Services" → "LDAP"
   - Enable "LDAP as a Service"
   - Note down your LDAP connection details

### Step 3: Get Your LDAP Connection Details

After enabling LDAP, you'll get:

```
LDAP Server: ldap.jumpcloud.com
Port: 389 (LDAP) or 636 (LDAPS - recommended)
Base DN: o=<your-org-id>,dc=jumpcloud,dc=com
Bind DN: uid=<service-account>,ou=Users,o=<your-org-id>,dc=jumpcloud,dc=com
Bind Password: <your-service-account-password>
```

### Step 4: Update Your Java Configuration

Update your `application.properties`:

```properties
# JumpCloud LDAP Configuration
ldap.url=ldaps://ldap.jumpcloud.com:636
ldap.base=o=YOUR_ORG_ID,dc=jumpcloud,dc=com
ldap.userDn=uid=SERVICE_ACCOUNT,ou=Users,o=YOUR_ORG_ID,dc=jumpcloud,dc=com
ldap.password=YOUR_SERVICE_PASSWORD
ldap.userSearchBase=ou=Users
ldap.userSearchFilter=(uid={0})
ldap.groupSearchBase=ou=Users
ldap.groupSearchFilter=(memberOf=cn={0},ou=Users,o=YOUR_ORG_ID,dc=jumpcloud,dc=com)
```

### Step 5: Test Your Connection

Use the provided Java examples to test connectivity:

```bash
# Compile and run the connection test
mvn compile exec:java -Dexec.mainClass="LdapConnectionTest"
```

## 🔧 Advanced Features Available in JumpCloud

### 1. Custom Attributes
- Add custom user attributes
- Map attributes to application roles
- Use attributes for fine-grained permissions

### 2. Multi-Factor Authentication (MFA)
- Enable MFA for enhanced security
- Support for TOTP, SMS, push notifications
- Conditional access policies

### 3. Single Sign-On (SSO)
- Connect to popular SaaS applications
- SAML and OAuth support
- Custom application integration

### 4. Device Management
- Manage user devices
- Enforce security policies
- Remote device control

### 5. API Access
- REST API for programmatic access
- Automate user and group management
- Integration with CI/CD pipelines

## 📊 Comparison: Forum Systems vs JumpCloud

| Feature | Forum Systems (Free) | JumpCloud (30-day trial) |
|---------|---------------------|---------------------------|
| **Cost** | Always free | Free for 30 days |
| **Users** | 8 predefined users | Unlimited custom users |
| **Groups** | 2 predefined groups | Unlimited custom groups |
| **Customization** | None | Full customization |
| **Security** | Basic LDAP | LDAPS, MFA, advanced security |
| **Support** | Community | Professional support |
| **Production Ready** | No (testing only) | Yes |
| **API Access** | LDAP only | LDAP + REST API |
| **SSO Integration** | No | Yes |

## 🛡️ Security Best Practices for JumpCloud

1. **Use LDAPS (Secure LDAP)**:
   ```properties
   ldap.url=ldaps://ldap.jumpcloud.com:636
   ```

2. **Create Dedicated Service Account**:
   - Don't use admin credentials for application binding
   - Create a service account with minimal required permissions
   - Rotate service account passwords regularly

3. **Enable MFA**:
   - Enable MFA for all admin accounts
   - Consider MFA for service accounts if supported

4. **Monitor Access**:
   - Review LDAP access logs regularly
   - Set up alerts for suspicious activity
   - Monitor failed authentication attempts

5. **Network Security**:
   - Restrict LDAP access to specific IP ranges
   - Use VPN or private networks when possible
   - Implement proper firewall rules

## 🔄 Migration Path

### From Forum Systems to JumpCloud:

1. **Update Configuration**:
   - Change LDAP URL and credentials
   - Update base DN and search filters
   - Enable LDAPS for security

2. **Recreate Test Data**:
   - Create users matching your test scenarios
   - Set up groups with appropriate permissions
   - Test all authentication flows

3. **Update Java Code**:
   - Modify attribute mappings if needed
   - Update group membership logic
   - Test permission mapping

### From JumpCloud Trial to Production:

1. **Choose Subscription Plan**:
   - Evaluate user count and features needed
   - Consider annual vs monthly billing
   - Review pricing tiers

2. **Production Hardening**:
   - Enable all security features
   - Set up monitoring and alerting
   - Implement backup and disaster recovery

3. **Integration Planning**:
   - Plan SSO integrations
   - Set up automated user provisioning
   - Implement lifecycle management

## 📞 Getting Help

- **JumpCloud Documentation**: [docs.jumpcloud.com](https://docs.jumpcloud.com/)
- **Community Forum**: [community.jumpcloud.com](https://community.jumpcloud.com/)
- **Support**: Available during trial period
- **Sales**: For pricing and feature questions

## 💡 Pro Tips

1. **Start Simple**: Begin with basic LDAP authentication before adding advanced features
2. **Test Thoroughly**: Use the trial period to test all your use cases
3. **Document Everything**: Keep track of your configuration for production deployment
4. **Plan for Scale**: Consider future user growth and feature needs
5. **Security First**: Always use LDAPS and enable MFA in production