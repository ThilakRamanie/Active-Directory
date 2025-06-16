# 🚀 Free LDAP Server Deployment Guide

## Quick Start Options (No JumpCloud)

### 🎯 Option 1: Railway.app (Recommended - Easiest)

**Steps:**
1. Fork this repository to your GitHub
2. Sign up at [railway.app](https://railway.app) (free tier)
3. Click "Deploy from GitHub"
4. Select your forked repository
5. Railway will auto-detect the `docker-compose.yml`
6. Deploy and get your public URL

**Result:**
- LDAP Server: `your-app.railway.app:389`
- Web UI: `your-app.railway.app:8080`
- Free for 500 hours/month

---

### 🐳 Option 2: Render.com

**Steps:**
1. Sign up at [render.com](https://render.com)
2. Create new "Web Service"
3. Connect your GitHub repository
4. Choose "Docker" as environment
5. Set port to `389`
6. Deploy

**Configuration:**
```yaml
# render.yaml
services:
  - type: web
    name: ldap-server
    env: docker
    dockerfilePath: ./Dockerfile
    plan: free
```

---

### ☁️ Option 3: AWS EC2 Free Tier

**Launch Script:**
```bash
# 1. Launch t2.micro Ubuntu instance
# 2. SSH into instance
# 3. Run setup script:

wget https://raw.githubusercontent.com/your-repo/Active-Directory/main/setup-ldap.sh
chmod +x setup-ldap.sh
./setup-ldap.sh
```

**Security Group Rules:**
- Port 389 (LDAP): 0.0.0.0/0
- Port 80 (Web): 0.0.0.0/0
- Port 22 (SSH): Your IP

---

### 🌊 Option 4: DigitalOcean (Free Credits)

**One-Click Deployment:**
1. Sign up for DigitalOcean ($200 free credits)
2. Create new Droplet
3. Choose "Docker" from marketplace
4. Upload docker-compose.yml
5. Run: `docker-compose up -d`

---

### 🏠 Option 5: Local Docker (Development)

**Quick Start:**
```bash
# Clone repository
git clone https://github.com/your-repo/Active-Directory.git
cd Active-Directory

# Start LDAP server
docker-compose up -d

# Access services
# LDAP: localhost:389
# Web UI: localhost:8080
```

---

## 🔧 Configuration for Your Java App

### Update application.properties:

```properties
# Replace YOUR_SERVER_URL with your deployed server
ldap.url=ldap://YOUR_SERVER_URL:389
ldap.base=dc=example,dc=org
ldap.userDn=cn=admin,dc=example,dc=org
ldap.password=admin
ldap.userSearchBase=ou=people
ldap.userSearchFilter=(uid={0})
ldap.groupSearchBase=ou=groups
ldap.groupSearchFilter=(member={0})
```

### Test Connection:
```bash
# Test LDAP connectivity
ldapsearch -x -H ldap://YOUR_SERVER_URL:389 \
  -D "cn=admin,dc=example,dc=org" \
  -w admin \
  -b "dc=example,dc=org" \
  "(objectclass=*)"
```

---

## 👥 Default Users & Groups

### Users:
- **admin** / password: `admin`
- **user1** / password: `password`
- **user2** / password: `password`

### Groups:
- **admins**: admin
- **users**: user1, user2
- **developers**: user1

---

## 🌐 Web Management

Access phpLDAPadmin at: `http://YOUR_SERVER_URL:8080`

**Login:**
- Login DN: `cn=admin,dc=example,dc=org`
- Password: `admin`

---

## 🔒 Security Notes

### For Production:
1. Change default passwords
2. Enable LDAPS (port 636)
3. Restrict network access
4. Use environment variables for secrets
5. Enable logging and monitoring

### Environment Variables:
```bash
export LDAP_ADMIN_PASSWORD="your-secure-password"
export LDAP_DOMAIN="your-domain.com"
export LDAP_ORGANISATION="Your Organization"
```

---

## 🚨 Troubleshooting

### Common Issues:

**Connection Refused:**
- Check firewall rules
- Verify port 389 is open
- Check service status: `systemctl status slapd`

**Authentication Failed:**
- Verify admin DN format
- Check password
- Test with ldapsearch command

**Web UI Not Loading:**
- Check Apache status: `systemctl status apache2`
- Verify port 80/8080 is open
- Check phpLDAPadmin configuration

### Debug Commands:
```bash
# Check LDAP service
sudo systemctl status slapd

# Test local connection
ldapsearch -x -H ldap://localhost -b dc=example,dc=org

# View logs
sudo journalctl -u slapd -f

# Check listening ports
sudo netstat -tlnp | grep :389
```

---

## 📊 Monitoring & Logs

### Log Locations:
- LDAP logs: `/var/log/syslog`
- Apache logs: `/var/log/apache2/`
- Docker logs: `docker logs openldap`

### Health Check:
```bash
# Simple health check script
#!/bin/bash
if ldapsearch -x -H ldap://localhost -b dc=example,dc=org -s base > /dev/null 2>&1; then
    echo "LDAP server is healthy"
    exit 0
else
    echo "LDAP server is down"
    exit 1
fi
```

---

## 🔄 Backup & Restore

### Backup:
```bash
# Export LDAP data
sudo slapcat > ldap-backup.ldif

# Backup with Docker
docker exec openldap slapcat > ldap-backup.ldif
```

### Restore:
```bash
# Stop service
sudo systemctl stop slapd

# Clear database
sudo rm -rf /var/lib/ldap/*

# Restore
sudo slapadd < ldap-backup.ldif

# Start service
sudo systemctl start slapd
```

---

## 💰 Cost Comparison

| Platform | Free Tier | Limitations |
|----------|-----------|-------------|
| Railway.app | 500 hrs/month | Sleep after inactivity |
| Render.com | 750 hrs/month | Sleep after 15 min |
| AWS EC2 | 750 hrs/month | t2.micro only |
| DigitalOcean | $200 credits | 60 days |
| Google Cloud | $300 credits | 90 days |

**Recommendation:** Start with Railway.app for testing, move to AWS/GCP for production.

---

## 🎯 Next Steps

1. **Choose deployment option** based on your needs
2. **Deploy LDAP server** using provided scripts
3. **Update Java configuration** with your server URL
4. **Test integration** with provided examples
5. **Add custom users/groups** as needed
6. **Implement security measures** for production use

All options are **completely free** and give you full control over your LDAP server!