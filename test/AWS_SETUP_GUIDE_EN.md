# 🚀 AWS Cloud Service Deployment Guide

## 📋 Prerequisites

### 1. AWS Account
- Visit [AWS Console](https://aws.amazon.com/console/)
- Create or log in to your account
- Obtain Access Key ID and Secret Access Key

### 2. Local Environment
- Java 17+ (Installed)
- Maven 3.6+ (Installed)
- AWS CLI 2.x (Installed)

## 🔐 AWS Credentials Configuration

### Step 1: Restart PowerShell
Since AWS CLI was just installed, please:
1. **Close the current PowerShell window**
2. **Open a new PowerShell window**
3. **Navigate to the project directory**

### Step 2: Configure AWS Credentials
```bash
# Execute in PowerShell
aws configure
```

The system will prompt for the following information:
```
AWS Access Key ID [None]: YOUR_ACCESS_KEY_ID
AWS Secret Access Key [None]: YOUR_SECRET_ACCESS_KEY
Default region name [None]: us-east-1
Default output format [None]: json
```

### Step 3: Verify Configuration
```bash
aws sts get-caller-identity
```

You should see output similar to:
```json
{
    "UserId": "AIDACKCEVSQ6C2EXAMPLE",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/YourUserName"
}
```

## Start Deployment

### Method 1: Using Batch File (Recommended for Windows Users)
```bash
# Execute in project root directory
.\deploy-to-aws.bat
```

### Method 2: Using Shell Script (Linux/Mac Users)
```bash
# Execute in project root directory
./deploy-to-aws.sh
```

### Method 3: Manual Command Execution
```bash
# 1. Build backend
cd springboot
mvn clean package -DskipTests
cd ..

# 2. Create S3 bucket
aws s3 mb s3://elderly-companion-us-east-1 --region us-east-1

# 3. Configure static website hosting
aws s3 website s3://elderly-companion-us-east-1 --index-document index.html --error-document error.html

# 4. Deploy frontend
aws s3 sync src/ s3://elderly-companion-us-east-1 --delete

# 5. Create DynamoDB tables
aws dynamodb create-table \
    --table-name pet_mood_us-east-1 \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region us-east-1
```

## 📊 Post-Deployment Verification

### 1. Check S3 Website
- Visit the S3 website URL
- Verify frontend functionality is normal

### 2. Check Backend API
- Test API endpoints
- Verify database connectivity

### 3. Check DynamoDB
- Verify tables were created successfully
- Test data read/write operations

## 🔧 Troubleshooting

### Common Issue 1: AWS CLI Command Not Found
**Solution**: Restart PowerShell or restart your computer

### Common Issue 2: Insufficient Permissions
**Solution**: Ensure IAM user has sufficient permissions:
- S3FullAccess
- DynamoDBFullAccess
- ElasticBeanstalkFullAccess

### Common Issue 3: Region Mismatch
**Solution**: Ensure all services use the same region (us-east-1)

## Getting Help

If you encounter issues:
1. Check error logs in AWS Console
2. Review CloudTrail audit logs
3. Contact AWS Support

## Next Steps

After deployment is complete:
1. Update frontend API configuration
2. Test all functionality
3. Configure domain and HTTPS
4. Set up monitoring and alerts

---

**Note**: First deployment may take 10-20 minutes, please be patient!
