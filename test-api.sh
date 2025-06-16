#!/bin/bash

# Test script for LDAP API endpoints
# Make sure the Spring Boot application is running first

BASE_URL="http://localhost:12000/api/ldap"

echo "🧪 Testing LDAP API Endpoints"
echo "================================"

# Test health endpoint
echo "1. Testing health endpoint..."
curl -s "$BASE_URL/health" | jq '.' || echo "Health check failed"
echo ""

# Test authentication
echo "2. Testing user authentication..."
curl -s -X POST "$BASE_URL/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username":"einstein","password":"password"}' | jq '.' || echo "Auth test failed"
echo ""

# Test user info
echo "3. Getting user information..."
curl -s "$BASE_URL/user/newton" | jq '.' || echo "User info failed"
echo ""

# Test user groups
echo "4. Getting user groups..."
curl -s "$BASE_URL/user/euler/groups" | jq '.' || echo "User groups failed"
echo ""

# Test user permissions
echo "5. Getting user permissions..."
curl -s "$BASE_URL/user/tesla/permissions" | jq '.' || echo "User permissions failed"
echo ""

# Test group membership
echo "6. Testing group membership..."
curl -s "$BASE_URL/user/gauss/group/mathematicians" | jq '.' || echo "Group membership failed"
echo ""

# Test users in group
echo "7. Getting users in scientists group..."
curl -s "$BASE_URL/group/scientists/users" | jq '.' || echo "Users in group failed"
echo ""

# Test all groups
echo "8. Getting all groups..."
curl -s "$BASE_URL/groups" | jq '.' || echo "All groups failed"
echo ""

# Test user session
echo "9. Getting user session..."
curl -s "$BASE_URL/session/riemann" | jq '.' || echo "User session failed"
echo ""

echo "✅ API Testing Complete!"