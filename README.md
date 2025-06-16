# Free Online Active Directory/LDAP Solutions for Java Testing

This repository provides simple ways to set up and test Active Directory/LDAP integration with your Java backend using **free online services**.

## 🎯 Quick Start Options

### Option 1: Forum Systems Free LDAP Test Server (Immediate Access)

**Best for**: Quick testing, proof of concept, learning LDAP integration

**Server Details:**
- **Server**: `ldap.forumsys.com`
- **Port**: `389`
- **Bind DN**: `cn=read-only-admin,dc=example,dc=com`
- **Bind Password**: `password`
- **All user passwords**: `password`

**Available Users & Groups:**
- **Mathematicians Group** (`ou=mathematicians,dc=example,dc=com`):
  - riemann, gauss, euler, euclid
- **Scientists Group** (`ou=scientists,dc=example,dc=com`):
  - einstein, newton, galieleo, tesla

### Option 2: Deploy Your Own Free LDAP Server

**Best for**: Full control, custom users/groups, production-like testing

**Quick Deploy Options:**
- **Railway.app**: One-click deploy with Docker Compose (500 hrs/month free)
- **AWS EC2**: Free tier Ubuntu + OpenLDAP setup script
- **DigitalOcean**: $200 free credits for new users
- **Local Docker**: Full development environment

📋 **See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for detailed setup instructions**
📋 **See [FREE_LDAP_SERVERS.md](FREE_LDAP_SERVERS.md) for all available options**

## 🚀 Java Integration Examples

### Maven Dependencies

Add these to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ldap</groupId>
        <artifactId>spring-ldap-core</artifactId>
        <version>3.1.1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-ldap</artifactId>
        <version>6.1.4</version>
    </dependency>
</dependencies>
```

### Basic LDAP Connection Test

See `src/main/java/LdapConnectionTest.java` for a complete example.

### Spring Boot Integration

See `src/main/java/LdapConfig.java` for Spring Boot configuration.

## 📋 Testing Scenarios

1. **User Authentication**: Verify user credentials
2. **Group Membership**: Check if user belongs to specific groups
3. **User Search**: Find users by attributes
4. **Permission Mapping**: Map LDAP groups to application roles

## 🔧 Setup Instructions

1. Clone this repository
2. Run the Java examples to test connectivity
3. Modify the configuration for your specific needs
4. Integrate with your existing Java backend

## 🛡️ Security Notes

- Forum Systems server is read-only and for testing only
- Never use test credentials in production
- Always use secure connections (LDAPS) in production
- Implement proper error handling and logging

## 📚 Additional Resources

- [Apache Directory Studio](https://directory.apache.org/studio/) - LDAP browser/editor
- [Spring LDAP Documentation](https://docs.spring.io/spring-ldap/docs/current/reference/)
- [JumpCloud Documentation](https://docs.jumpcloud.com/)