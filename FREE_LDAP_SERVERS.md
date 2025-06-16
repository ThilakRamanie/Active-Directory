# Free Online LDAP/Active Directory Servers for Testing

## 🎯 Option 1: Forum Systems Free LDAP Server (Ready to Use)

**Best for**: Immediate testing, no setup required

**Connection Details:**
```
Server: ldap.forumsys.com
Port: 389
Bind DN: cn=read-only-admin,dc=example,dc=com
Password: password
Base DN: dc=example,dc=com
```

**Available Users & Groups:**
- **Scientists**: einstein, newton, galieleo, tesla
- **Mathematicians**: riemann, gauss, euler, euclid
- All user passwords: `password`

**Limitations:**
- Read-only access
- Predefined users only
- No custom groups/permissions

---

## 🐳 Option 2: Docker OpenLDAP Server (Self-Hosted)

**Best for**: Full control, custom users/groups, free hosting

### Step 1: Create Docker Compose File

```yaml
# docker-compose.yml
version: '3.8'

services:
  openldap:
    image: osixia/openldap:1.5.0
    container_name: openldap
    environment:
      LDAP_LOG_LEVEL: "256"
      LDAP_ORGANISATION: "Example Inc."
      LDAP_DOMAIN: "example.org"
      LDAP_BASE_DN: ""
      LDAP_ADMIN_PASSWORD: "admin"
      LDAP_CONFIG_PASSWORD: "config"
      LDAP_READONLY_USER: "false"
      LDAP_RFC2307BIS_SCHEMA: "false"
      LDAP_BACKEND: "mdb"
      LDAP_TLS: "true"
      LDAP_TLS_CRT_FILENAME: "ldap.crt"
      LDAP_TLS_KEY_FILENAME: "ldap.key"
      LDAP_TLS_DH_PARAM_FILENAME: "dhparam.pem"
      LDAP_TLS_CA_CRT_FILENAME: "ca.crt"
      LDAP_TLS_ENFORCE: "false"
      LDAP_TLS_CIPHER_SUITE: "SECURE256:-VERS-SSL3.0"
      LDAP_TLS_VERIFY_CLIENT: "demand"
      LDAP_REPLICATION: "false"
      KEEP_EXISTING_CONFIG: "false"
      LDAP_REMOVE_CONFIG_AFTER_SETUP: "true"
      LDAP_SSL_HELPER_PREFIX: "ldap"
    tty: true
    stdin_open: true
    volumes:
      - /var/lib/ldap
      - /etc/ldap/slapd.d
      - /container/service/slapd/assets/certs/
    ports:
      - "389:389"
      - "636:636"
    hostname: "example.org"

  phpldapadmin:
    image: osixia/phpldapadmin:latest
    container_name: phpldapadmin
    environment:
      PHPLDAPADMIN_LDAP_HOSTS: "openldap"
      PHPLDAPADMIN_HTTPS: "false"
    ports:
      - "8080:80"
    depends_on:
      - openldap
```

### Step 2: Deploy on Free Cloud Platforms

#### A. Railway.app (Free Tier)
1. Sign up at [railway.app](https://railway.app)
2. Create new project from GitHub
3. Upload your docker-compose.yml
4. Deploy automatically
5. Get public URL

#### B. Render.com (Free Tier)
1. Sign up at [render.com](https://render.com)
2. Create new Web Service
3. Connect GitHub repository
4. Use Docker deployment
5. Get public URL

#### C. Fly.io (Free Tier)
1. Sign up at [fly.io](https://fly.io)
2. Install flyctl CLI
3. Run `fly launch` in your project directory
4. Deploy with `fly deploy`

### Step 3: Connection Details
```
Server: your-app-url.railway.app (or your platform URL)
Port: 389
Admin DN: cn=admin,dc=example,dc=org
Admin Password: admin
Base DN: dc=example,dc=org
```

---

## ☁️ Option 3: AWS EC2 Free Tier OpenLDAP

**Best for**: Production-like environment, AWS integration

### Step 1: Launch EC2 Instance
1. Sign up for AWS Free Tier
2. Launch t2.micro Ubuntu instance
3. Configure security group (ports 389, 636, 22)

### Step 2: Install OpenLDAP
```bash
# SSH into your instance
sudo apt update
sudo apt install -y slapd ldap-utils

# Configure LDAP
sudo dpkg-reconfigure slapd
# Choose: No, example.com, Example Organization, admin password

# Install phpLDAPadmin (optional)
sudo apt install -y phpldapadmin apache2
sudo systemctl enable apache2
sudo systemctl start apache2
```

### Step 3: Configure Security Group
- Port 389 (LDAP): 0.0.0.0/0
- Port 636 (LDAPS): 0.0.0.0/0  
- Port 80 (Web UI): 0.0.0.0/0
- Port 22 (SSH): Your IP only

### Step 4: Connection Details
```
Server: your-ec2-public-ip
Port: 389
Admin DN: cn=admin,dc=example,dc=com
Admin Password: your-chosen-password
Base DN: dc=example,dc=com
```

---

## 🌐 Option 4: Google Cloud Platform Free Tier

**Best for**: Google Cloud integration, reliable hosting

### Step 1: Create GCP Account
1. Sign up for Google Cloud Free Tier
2. Create new project
3. Enable Compute Engine API

### Step 2: Create VM Instance
```bash
# Create VM
gcloud compute instances create ldap-server \
    --zone=us-central1-a \
    --machine-type=e2-micro \
    --image-family=ubuntu-2004-lts \
    --image-project=ubuntu-os-cloud \
    --tags=ldap-server

# Create firewall rules
gcloud compute firewall-rules create allow-ldap \
    --allow tcp:389,tcp:636,tcp:80 \
    --source-ranges 0.0.0.0/0 \
    --target-tags ldap-server
```

### Step 3: Install OpenLDAP
```bash
# SSH into instance
gcloud compute ssh ldap-server --zone=us-central1-a

# Install OpenLDAP
sudo apt update
sudo apt install -y slapd ldap-utils phpldapadmin apache2
sudo dpkg-reconfigure slapd
```

---

## 🔧 Option 5: DigitalOcean Droplet (Free Credits)

**Best for**: Simple setup, good documentation

### Step 1: Sign Up
1. Create DigitalOcean account
2. Get $200 free credits (new users)
3. Create $5/month droplet (covered by credits)

### Step 2: One-Click OpenLDAP
1. Choose "OpenLDAP" from marketplace
2. Select $5/month droplet
3. Choose datacenter region
4. Add SSH key
5. Create droplet

### Step 3: Access
- SSH: `ssh root@your-droplet-ip`
- Web UI: `http://your-droplet-ip/phpldapadmin`
- LDAP: `ldap://your-droplet-ip:389`

---

## 📋 Quick Setup Script for Ubuntu/Debian

Save this as `setup-ldap.sh`:

```bash
#!/bin/bash

# Quick OpenLDAP setup script
echo "🚀 Setting up OpenLDAP server..."

# Update system
sudo apt update && sudo apt upgrade -y

# Install OpenLDAP
sudo DEBIAN_FRONTEND=noninteractive apt install -y slapd ldap-utils

# Install phpLDAPadmin for web management
sudo apt install -y phpldapadmin apache2

# Configure LDAP domain
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
olcRootPW: {SSHA}$(slappasswd -s "password" | cut -d'}' -f2)
EOF

# Apply configuration
sudo ldapmodify -Y EXTERNAL -H ldapi:/// -f /tmp/ldap-config.ldif

# Create base structure
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
userPassword: {SSHA}$(slappasswd -s "password")

dn: ou=people,dc=example,dc=com
objectClass: organizationalUnit
ou: people

dn: ou=groups,dc=example,dc=com
objectClass: organizationalUnit
ou: groups
EOF

# Add base structure
sudo ldapadd -x -D cn=admin,dc=example,dc=com -w password -f /tmp/base.ldif

# Configure phpLDAPadmin
sudo sed -i "s/dc=example,dc=com/dc=example,dc=com/g" /etc/phpldapadmin/config.php

# Start services
sudo systemctl enable slapd apache2
sudo systemctl start slapd apache2

echo "✅ OpenLDAP setup complete!"
echo "📡 LDAP Server: ldap://$(curl -s ifconfig.me):389"
echo "🔑 Admin DN: cn=admin,dc=example,dc=com"
echo "🔒 Password: password"
echo "🌐 Web UI: http://$(curl -s ifconfig.me)/phpldapadmin"
```

Make it executable and run:
```bash
chmod +x setup-ldap.sh
./setup-ldap.sh
```

---

## 🎯 Recommended Approach

**For immediate testing**: Use Forum Systems (Option 1)
**For custom setup**: Use Docker on Railway/Render (Option 2)
**For production-like**: Use AWS EC2 Free Tier (Option 3)

## 🔗 Java Connection Example

```java
// Connection properties for your server
ldap.url=ldap://your-server-url:389
ldap.base=dc=example,dc=com
ldap.userDn=cn=admin,dc=example,dc=com
ldap.password=password
```

All these options are **completely free** and give you full control over your LDAP server for testing your Java backend integration.