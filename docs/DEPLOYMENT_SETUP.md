# 🚀 CD Pipeline Setup Guide - Deploy to AWS EC2

This guide will help you set up continuous deployment for your Spring Boot backend to AWS EC2.

## 📋 Prerequisites Checklist

- ✅ EC2 instance running (Amazon Linux 2 or Ubuntu)
- ✅ ECR repository created: `351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend`
- ✅ Jenkins server with AWS CLI installed
- ✅ SSH access to EC2 instance

---

## 🔧 Part 1: EC2 Instance Setup (AWS Web Console)

### Step 1: Connect to EC2 via AWS Web Terminal

1. Go to **EC2 Dashboard** → **Instances**
2. Select your instance
3. Click **Connect** → **EC2 Instance Connect** tab
4. Click **Connect** button

### Step 2: Install Docker on EC2

```bash
# Update system
sudo yum update -y

# Install Docker
sudo yum install docker -y

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add ec2-user to docker group (to run docker without sudo)
sudo usermod -a -G docker ec2-user

# Verify installation
docker --version

# Log out and log back in for group changes to take effect
exit
```

**Re-connect to EC2 after logging out**, then verify:
```bash
docker ps  # Should work without sudo
```

### Step 3: Install AWS CLI v2 on EC2

```bash
# Download and install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Verify installation
aws --version
```

### Step 4: Configure AWS Credentials on EC2

**Option A: Using IAM Role (Recommended)**
1. Go to **IAM** → **Roles** → **Create Role**
2. Select **AWS Service** → **EC2**
3. Attach policy: `AmazonEC2ContainerRegistryReadOnly`
4. Name it: `EC2-ECR-Access-Role`
5. Go back to **EC2** → Select instance → **Actions** → **Security** → **Modify IAM Role**
6. Attach the role you just created

**Option B: Using AWS Credentials (If IAM Role doesn't work)**
```bash
aws configure
# Enter:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region: eu-north-1
# - Default output format: json
```

### Step 5: Test ECR Access

```bash
# Test login to ECR
aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin 351889158954.dkr.ecr.eu-north-1.amazonaws.com

# Should see: "Login Succeeded"
```

### Step 6: Configure Security Group

1. Go to **EC2** → **Security Groups**
2. Select your instance's security group
3. Add **Inbound Rules**:
   - Type: **Custom TCP**
   - Port: **8090** (your application port)
   - Source: **0.0.0.0/0** (for public access) or specific IPs
   - Description: `Spring Boot Application`

---

## 🔑 Part 2: Jenkins Credentials Setup (AWS Web Console)

You need to add these credentials to Jenkins. Since you can't use AWS CLI locally, get these values from AWS Console:

### 1. AWS Access Keys (for ECR Push)

**Get from AWS Console:**
1. Go to **IAM** → **Users** → Your user
2. **Security credentials** tab
3. **Create access key** → Choose "Application running on AWS compute service"
4. Save the **Access Key ID** and **Secret Access Key**

**Add to Jenkins:**
1. Jenkins → **Manage Jenkins** → **Credentials** → **System** → **Global credentials**
2. Add credential:
   - Kind: **Secret text**
   - ID: `aws-access-key-id`
   - Secret: *paste your Access Key ID*
3. Add another credential:
   - Kind: **Secret text**
   - ID: `aws-secret-access-key`
   - Secret: *paste your Secret Access Key*

### 2. EC2 SSH Key

**Get your private key:**
- When you created EC2, you downloaded a `.pem` file (e.g., `my-key.pem`)
- If you don't have it, you'll need to create a new key pair

**Add to Jenkins:**
1. Jenkins → **Manage Jenkins** → **Credentials**
2. Add credential:
   - Kind: **SSH Username with private key**
   - ID: `ec2-ssh-key`
   - Username: `ec2-user` (for Amazon Linux) or `ubuntu` (for Ubuntu)
   - Private Key: **Enter directly** → Paste contents of your `.pem` file
   - Passphrase: Leave empty (unless you set one)

### 3. EC2 Host Address

**Get from AWS Console:**
1. Go to **EC2** → **Instances**
2. Select your instance
3. Copy the **Public IPv4 address** or **Public IPv4 DNS**

**Add to Jenkins:**
1. Add credential:
   - Kind: **Secret text**
   - ID: `ec2-host`
   - Secret: *paste your EC2 public IP or DNS* (e.g., `ec2-xx-xxx-xxx-xx.eu-north-1.compute.amazonaws.com`)

### 4. Production Frontend URL

**Add to Jenkins:**
1. Add credential:
   - Kind: **Secret text**
   - ID: `frontend-url-prod`
   - Secret: Your production frontend URL (e.g., `https://myapp.com` or `http://your-frontend-ec2-ip:5173`)

### 5. Database & Other Credentials

Make sure you have these already configured in Jenkins (from your CI setup):
- ✅ `database-credentials` (username/password)
- ✅ `database-url`
- ✅ `jwt-secret`
- ✅ `gemini-api-key`
- ✅ `mail-credentials`
- ✅ `mail-from-address`

---

## 🧪 Part 3: Test the Deployment

### Manual Test First

Before running the full pipeline, test manually on EC2:

1. **Connect to EC2**
2. **Pull an image manually:**
   ```bash
   # Login to ECR
   aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin 351889158954.dkr.ecr.eu-north-1.amazonaws.com
   
   # Pull the image (after Jenkins pushes it)
   docker pull 351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend:latest
   ```

3. **Run container manually:**
   ```bash
   docker run -d \
     --name ead-backend-app \
     -p 8090:8090 \
     -e DATASOURCE_URL="your_db_url" \
     -e DATASOURCE_USERNAME="your_db_user" \
     -e DATASOURCE_PASSWORD="your_db_pass" \
     351889158954.dkr.ecr.eu-north-1.amazonaws.com/ead-backend:latest
   
   # Check logs
   docker logs -f ead-backend-app
   ```

4. **Test from browser:**
   ```
   http://your-ec2-public-ip:8090/health
   ```

### Run Full Jenkins Pipeline

1. Push changes to `dev` branch
2. Jenkins will automatically:
   - ✅ Build → Test → Docker Build
   - ✅ Push to ECR
   - ✅ Deploy to EC2
3. Monitor Jenkins console output
4. Verify app is running: `http://your-ec2-ip:8090/health`

---

## 🔍 Troubleshooting

### Issue: "Cannot connect to EC2"
**Solution:**
1. Check security group allows SSH (port 22) from Jenkins server IP
2. Verify SSH key is correct in Jenkins
3. Test SSH manually: `ssh -i your-key.pem ec2-user@your-ec2-ip`

### Issue: "ECR login failed on EC2"
**Solution:**
1. Verify IAM role is attached to EC2 instance
2. Check role has `AmazonEC2ContainerRegistryReadOnly` policy
3. Try manual login: `aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin 351889158954.dkr.ecr.eu-north-1.amazonaws.com`

### Issue: "Container fails to start"
**Solution:**
1. Check logs: `docker logs ead-backend-app`
2. Verify all environment variables are set correctly
3. Check database connectivity from EC2
4. Verify security groups allow database access

### Issue: "Cannot access app from browser"
**Solution:**
1. Check security group inbound rules for port 8090
2. Verify container is running: `docker ps`
3. Check if app is listening: `curl localhost:8090/health` from EC2
4. Check EC2 instance is running

---

## 📊 Monitoring & Logs

### View Application Logs on EC2
```bash
# Real-time logs
docker logs -f ead-backend-app

# Last 100 lines
docker logs --tail 100 ead-backend-app

# Logs with timestamps
docker logs -t ead-backend-app
```

### Check Container Status
```bash
# List running containers
docker ps

# Container resource usage
docker stats ead-backend-app

# Container details
docker inspect ead-backend-app
```

### Restart Application
```bash
# Restart container
docker restart ead-backend-app

# Or stop and start
docker stop ead-backend-app
docker start ead-backend-app
```

---

## 🔐 Security Best Practices

1. **Use IAM Roles** instead of hardcoded credentials on EC2
2. **Restrict Security Groups** to only necessary ports and IPs
3. **Use Secrets Manager** for sensitive data (advanced)
4. **Enable CloudWatch Logs** for monitoring
5. **Regular Updates**: Keep Docker and system packages updated
6. **Backup**: Regular database backups

---

## 🎯 Next Steps

1. **Set up HTTPS** with SSL certificate (Let's Encrypt or AWS ACM)
2. **Add Load Balancer** for high availability
3. **Configure Auto-scaling** for traffic spikes
4. **Set up CloudWatch Alarms** for monitoring
5. **Implement Blue-Green Deployment** for zero-downtime
6. **Add health check endpoint** verification in deployment

---

## 📝 Notes

- Current setup deploys from `dev` branch only
- To deploy from other branches, modify the `when { branch 'dev' }` condition in Jenkinsfile
- The deployment script uses `latest` tag for quick rollback
- Build number tags allow you to track specific deployments

---

## 🆘 Need Help?

- Check Jenkins console output for detailed error messages
- Review EC2 system logs: `sudo tail -f /var/log/messages`
- Check Docker daemon: `sudo systemctl status docker`
- AWS Support: Check CloudWatch logs and EC2 system logs in AWS Console

---

**Last Updated:** November 2025
