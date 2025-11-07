# 🚀 Quick Deployment Reference

## Jenkins Credentials Required

Add these in Jenkins → Manage Jenkins → Credentials:

| Credential ID | Type | Description | How to Get |
|--------------|------|-------------|------------|
| `aws-access-key-id` | Secret text | AWS Access Key | IAM → Users → Security credentials |
| `aws-secret-access-key` | Secret text | AWS Secret Key | IAM → Users → Security credentials |
| `ec2-ssh-key` | SSH Username with private key | EC2 SSH Key | Your `.pem` file from EC2 creation |
| `ec2-host` | Secret text | EC2 Public IP/DNS | EC2 Console → Instance details |
| `frontend-url-prod` | Secret text | Production Frontend URL | Your frontend URL |

## EC2 Setup Commands (One-time)

```bash
# 1. Install Docker
sudo yum update -y
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# 2. Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# 3. Test ECR access
aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin 351889158954.dkr.ecr.eu-north-1.amazonaws.com
```

## Security Group Rules

| Type | Port | Source | Description |
|------|------|--------|-------------|
| SSH | 22 | Your IP | SSH access |
| Custom TCP | 8090 | 0.0.0.0/0 | Application port |
| MySQL/Aurora | 3306 | EC2 Security Group | Database (if RDS) |

## Deployment Flow

```
1. Push to 'dev' branch
   ↓
2. Jenkins: Build & Test
   ↓
3. Jenkins: Build Docker image
   ↓
4. Jenkins: Push to ECR
   ↓
5. Jenkins: SSH to EC2 → Run deploy.sh
   ↓
6. EC2: Pull from ECR → Start container
   ↓
7. ✅ Deployed!
```

## Useful Commands on EC2

```bash
# Check running containers
docker ps

# View logs
docker logs -f ead-backend-app

# Restart app
docker restart ead-backend-app

# Stop app
docker stop ead-backend-app

# Remove old images
docker image prune -a

# Check disk space
df -h
```

## Troubleshooting Quick Fixes

**Can't SSH to EC2:**
```bash
# Check security group allows port 22 from your IP
# Verify key permissions: chmod 600 your-key.pem
```

**ECR login fails:**
```bash
# On EC2, verify IAM role or run:
aws configure
```

**Container won't start:**
```bash
# Check logs:
docker logs ead-backend-app

# Check if port is in use:
sudo netstat -tulpn | grep 8090
```

**Can't access app:**
```bash
# On EC2:
curl localhost:8090/health

# Check security group allows port 8090
```

## Manual Deployment (if Jenkins fails)

```bash
# On EC2:
aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin 351889158954.dkr.ecr.eu-north-1.amazonaws.com

docker pull 351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend:latest

docker stop ead-backend-app || true
docker rm ead-backend-app || true

docker run -d --name ead-backend-app --restart unless-stopped \
  -p 8090:8090 \
  -e DATASOURCE_URL="your_db_url" \
  -e DATASOURCE_USERNAME="your_user" \
  -e DATASOURCE_PASSWORD="your_pass" \
  351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend:latest
```

## Rollback to Previous Version

```bash
# On EC2:
docker stop ead-backend-app
docker rm ead-backend-app

# Pull specific version (use build number)
docker pull 351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend:123

# Start with that version
docker run -d --name ead-backend-app --restart unless-stopped \
  -p 8090:8090 \
  [environment variables...] \
  351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend:123
```
