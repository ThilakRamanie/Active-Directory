#!/bin/bash

# Quick OpenLDAP setup script for Ubuntu/Debian
echo "🚀 Setting up OpenLDAP server..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root for security reasons"
   exit 1
fi

# Update system
print_status "Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install OpenLDAP
print_status "Installing OpenLDAP..."
sudo DEBIAN_FRONTEND=noninteractive apt install -y slapd ldap-utils

# Install phpLDAPadmin for web management
print_status "Installing phpLDAPadmin..."
sudo apt install -y phpldapadmin apache2

# Get server IP
SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || curl -s ipinfo.io/ip 2>/dev/null || echo "localhost")

# Configure LDAP domain
print_status "Configuring LDAP domain..."
sudo tee /tmp/ldap-config.ldif << EOF
dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcSuffix
olcSuffix: dc=example,dc=com

dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcRootDN
olcRootDN: cn=admin,dc=example,dc=com

dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcRootPW
olcRootPW: $(slappasswd -s "password")
EOF

# Apply configuration
print_status "Applying LDAP configuration..."
sudo ldapmodify -Y EXTERNAL -H ldapi:/// -f /tmp/ldap-config.ldif

# Create base structure
print_status "Creating base LDAP structure..."
sudo tee /tmp/base.ldif << EOF
dn: dc=example,dc=com
objectClass: top
objectClass: dcObject
objectClass: organization
o: Example Organization
dc: example

dn: cn=admin,dc=example,dc=com
objectClass: simpleSecurityObject
objectClass: organizationalRole
cn: admin
description: LDAP administrator
userPassword: $(slappasswd -s "password")

dn: ou=people,dc=example,dc=com
objectClass: organizationalUnit
ou: people

dn: ou=groups,dc=example,dc=com
objectClass: organizationalUnit
ou: groups

dn: ou=developers,dc=example,dc=com
objectClass: organizationalUnit
ou: developers

dn: ou=admins,dc=example,dc=com
objectClass: organizationalUnit
ou: admins
EOF

# Add base structure
print_status "Adding base structure to LDAP..."
sudo ldapadd -x -D cn=admin,dc=example,dc=com -w password -f /tmp/base.ldif

# Create sample users
print_status "Creating sample users..."
sudo tee /tmp/users.ldif << EOF
dn: uid=john.doe,ou=people,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: john.doe
sn: Doe
givenName: John
cn: John Doe
displayName: John Doe
uidNumber: 1001
gidNumber: 1001
userPassword: $(slappasswd -s "password")
gecos: John Doe
loginShell: /bin/bash
homeDirectory: /home/john.doe
mail: john.doe@example.com

dn: uid=jane.smith,ou=people,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: jane.smith
sn: Smith
givenName: Jane
cn: Jane Smith
displayName: Jane Smith
uidNumber: 1002
gidNumber: 1002
userPassword: $(slappasswd -s "password")
gecos: Jane Smith
loginShell: /bin/bash
homeDirectory: /home/jane.smith
mail: jane.smith@example.com

dn: uid=admin.user,ou=people,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: admin.user
sn: User
givenName: Admin
cn: Admin User
displayName: Admin User
uidNumber: 1003
gidNumber: 1003
userPassword: $(slappasswd -s "admin123")
gecos: Admin User
loginShell: /bin/bash
homeDirectory: /home/admin.user
mail: admin@example.com
EOF

sudo ldapadd -x -D cn=admin,dc=example,dc=com -w password -f /tmp/users.ldif

# Create sample groups
print_status "Creating sample groups..."
sudo tee /tmp/groups.ldif << EOF
dn: cn=developers,ou=groups,dc=example,dc=com
objectClass: groupOfNames
cn: developers
description: Development team
member: uid=john.doe,ou=people,dc=example,dc=com
member: uid=jane.smith,ou=people,dc=example,dc=com

dn: cn=admins,ou=groups,dc=example,dc=com
objectClass: groupOfNames
cn: admins
description: System administrators
member: uid=admin.user,ou=people,dc=example,dc=com

dn: cn=users,ou=groups,dc=example,dc=com
objectClass: groupOfNames
cn: users
description: Regular users
member: uid=john.doe,ou=people,dc=example,dc=com
member: uid=jane.smith,ou=people,dc=example,dc=com
member: uid=admin.user,ou=people,dc=example,dc=com
EOF

sudo ldapadd -x -D cn=admin,dc=example,dc=com -w password -f /tmp/groups.ldif

# Configure phpLDAPadmin
print_status "Configuring phpLDAPadmin..."
sudo tee /etc/phpldapadmin/config.php << 'EOF'
<?php
$servers = new Datastore();
$servers->newServer('ldap_pla');
$servers->setValue('server','name','Local LDAP Server');
$servers->setValue('server','host','127.0.0.1');
$servers->setValue('server','port',389);
$servers->setValue('server','base',array('dc=example,dc=com'));
$servers->setValue('login','auth_type','cookie');
$servers->setValue('login','bind_id','cn=admin,dc=example,dc=com');
$servers->setValue('server','tls',false);
$servers->setValue('appearance','password_hash','ssha');
$servers->setValue('login','attr','dn');
?>
EOF

# Configure Apache for phpLDAPadmin
sudo tee /etc/apache2/conf-available/phpldapadmin.conf << EOF
Alias /phpldapadmin /usr/share/phpldapadmin/htdocs
<Directory /usr/share/phpldapadmin/htdocs>
    DirectoryIndex index.php
    Options +FollowSymLinks
    AllowOverride None
    Require all granted
</Directory>
EOF

sudo a2enconf phpldapadmin
sudo systemctl reload apache2

# Start and enable services
print_status "Starting services..."
sudo systemctl enable slapd apache2
sudo systemctl start slapd apache2

# Configure firewall (if ufw is installed)
if command -v ufw &> /dev/null; then
    print_status "Configuring firewall..."
    sudo ufw allow 389/tcp
    sudo ufw allow 636/tcp
    sudo ufw allow 80/tcp
fi

# Clean up temporary files
sudo rm -f /tmp/ldap-config.ldif /tmp/base.ldif /tmp/users.ldif /tmp/groups.ldif

# Test LDAP connection
print_status "Testing LDAP connection..."
if ldapsearch -x -H ldap://localhost -b dc=example,dc=com -D cn=admin,dc=example,dc=com -w password "(objectclass=*)" dn &>/dev/null; then
    print_status "LDAP server is working correctly!"
else
    print_error "LDAP server test failed!"
fi

echo ""
echo "================================================================"
echo -e "${GREEN}✅ OpenLDAP setup complete!${NC}"
echo "================================================================"
echo ""
echo -e "${BLUE}📡 LDAP Server Details:${NC}"
echo "   Server: ldap://$SERVER_IP:389"
echo "   Base DN: dc=example,dc=com"
echo "   Admin DN: cn=admin,dc=example,dc=com"
echo "   Admin Password: password"
echo ""
echo -e "${BLUE}🌐 Web Management:${NC}"
echo "   phpLDAPadmin: http://$SERVER_IP/phpldapadmin"
echo "   Login: cn=admin,dc=example,dc=com"
echo "   Password: password"
echo ""
echo -e "${BLUE}👥 Sample Users:${NC}"
echo "   john.doe / password"
echo "   jane.smith / password"
echo "   admin.user / admin123"
echo ""
echo -e "${BLUE}📁 Sample Groups:${NC}"
echo "   developers (john.doe, jane.smith)"
echo "   admins (admin.user)"
echo "   users (all users)"
echo ""
echo -e "${BLUE}🔧 Java Configuration:${NC}"
echo "   ldap.url=ldap://$SERVER_IP:389"
echo "   ldap.base=dc=example,dc=com"
echo "   ldap.userDn=cn=admin,dc=example,dc=com"
echo "   ldap.password=password"
echo ""
echo -e "${YELLOW}💡 Next Steps:${NC}"
echo "   1. Test connection: ldapsearch -x -H ldap://$SERVER_IP -b dc=example,dc=com"
echo "   2. Access web UI: http://$SERVER_IP/phpldapadmin"
echo "   3. Update your Java application configuration"
echo "   4. Add more users/groups as needed"
echo ""