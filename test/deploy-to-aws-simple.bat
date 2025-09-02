@echo off
echo AWS Cloud Service Deployment Script (CMD Version)
echo ================================================

REM Check if AWS CLI is installed
echo Checking AWS CLI...
where aws >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: AWS CLI not installed
    echo Please install: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
    pause
    exit /b 1
)

REM Check Java
echo Checking Java...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java not installed
    pause
    exit /b 1
)

REM Check Maven
echo Checking Maven...
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven not installed
    echo Please ensure Maven is installed and added to PATH
    pause
    exit /b 1
)

REM Set environment variables
set AWS_REGION=us-east-1
set PROJECT_NAME=elderly-companion
set S3_BUCKET=%PROJECT_NAME%-%AWS_REGION%
set EB_APP_NAME=%PROJECT_NAME%-app
set EB_ENV_NAME=%PROJECT_NAME%-env

echo Deployment Configuration:
echo   AWS Region: %AWS_REGION%
echo   S3 Bucket: %S3_BUCKET%
echo   Elastic Beanstalk App: %EB_APP_NAME%
echo   Elastic Beanstalk Env: %EB_ENV_NAME%
echo.

REM Check AWS credentials
echo Checking AWS credentials...
aws sts get-caller-identity >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: AWS credentials not configured
    echo Please run: aws configure
    pause
    exit /b 1
)

REM Get AWS account ID
for /f "tokens=3" %%i in ('aws sts get-caller-identity --query Account --output text') do set AWS_ACCOUNT_ID=%%i
echo AWS Account ID: %AWS_ACCOUNT_ID%

REM Create S3 bucket
echo.
echo Creating S3 bucket...
aws s3 ls "s3://%S3_BUCKET%" >nul 2>nul
if %errorlevel% neq 0 (
    aws s3 mb "s3://%S3_BUCKET%" --region %AWS_REGION%
    echo S3 bucket created: %S3_BUCKET%
) else (
    echo S3 bucket exists: %S3_BUCKET%
)

REM Configure S3 for static website hosting
echo Configuring S3 static website hosting...
aws s3 website "s3://%S3_BUCKET%" --index-document index.html --error-document error.html

REM Create DynamoDB tables
echo.
echo Creating DynamoDB tables...

echo Creating Pet Mood table...
aws dynamodb create-table --table-name "pet_mood_%AWS_REGION%" --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST --region %AWS_REGION%

echo Creating Schedules table...
aws dynamodb create-table --table-name "schedules_%AWS_REGION%" --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST --region %AWS_REGION%

echo Creating Users table...
aws dynamodb create-table --table-name "users_%AWS_REGION%" --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST --region %AWS_REGION%

echo DynamoDB tables created

REM Build backend
echo.
echo Building backend application...
cd springboot
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Backend build failed
    pause
    exit /b 1
)
cd ..

REM Create Elastic Beanstalk application
echo.
echo Creating Elastic Beanstalk application...
aws elasticbeanstalk describe-applications --application-names %EB_APP_NAME% >nul 2>nul
if %errorlevel% neq 0 (
    echo Creating Elastic Beanstalk application...
    aws elasticbeanstalk create-application --application-name %EB_APP_NAME% --description "Elderly Companion AI Application"
    echo Elastic Beanstalk application created
) else (
    echo Elastic Beanstalk application exists
)

REM Create Elastic Beanstalk environment
echo.
echo Creating Elastic Beanstalk environment...
aws elasticbeanstalk describe-environments --application-name %EB_APP_NAME% --environment-names %EB_ENV_NAME% --query "Environments[0].Status" --output text | findstr /i "Ready\|Updating" >nul
if %errorlevel% neq 0 (
    echo Creating Elastic Beanstalk environment...
    aws elasticbeanstalk create-environment --application-name %EB_APP_NAME% --environment-name %EB_ENV_NAME% --solution-stack-name "64bit Amazon Linux 2 v5.8.0 running Corretto 17" --option-settings Namespace=aws:autoscaling:launchconfiguration,OptionName=IamInstanceProfile,Value=aws-elasticbeanstalk-ec2-role Namespace=aws:elasticbeanstalk:environment,OptionName=EnvironmentType,Value=SingleInstance Namespace=aws:elasticbeanstalk:application:environment,OptionName=SPRING_PROFILES_ACTIVE,Value=aws
    echo Elastic Beanstalk environment created
) else (
    echo Elastic Beanstalk environment exists
)

REM Wait for environment to be ready
echo.
echo Waiting for Elastic Beanstalk environment to be ready...
echo This may take several minutes, please wait...
timeout /t 30 /nobreak >nul

REM Get environment URL
echo Getting environment URL...
for /f "tokens=*" %%i in ('aws elasticbeanstalk describe-environments --environment-names %EB_ENV_NAME% --query "Environments[0].CNAME" --output text') do set EB_URL=%%i

if "%EB_URL%"=="" (
    echo Environment may still be creating, please wait...
    set EB_URL=Environment creating...
) else (
    echo Backend service deployed: http://%EB_URL%
)

REM Deploy frontend to S3
echo.
echo Deploying frontend to S3...
cd src
aws s3 sync . "s3://%S3_BUCKET%" --delete
if %errorlevel% neq 0 (
    echo ERROR: Frontend deployment failed
    cd ..
    pause
    exit /b 1
)
cd ..

REM Get S3 website URL
echo Getting S3 website URL...
for /f "tokens=*" %%i in ('aws s3api get-bucket-website --bucket %S3_BUCKET% --query WebsiteEndpoint --output text') do set S3_URL=%%i

if "%S3_URL%"=="" (
    echo Cannot get S3 website URL
    set S3_URL=S3 website configuring...
) else (
    echo Frontend deployed: http://%S3_URL%
)

REM Create deployment configuration file
echo.
echo Creating deployment configuration file...
(
echo {
echo     "aws_region": "%AWS_REGION%",
echo     "s3_bucket": "%S3_BUCKET%",
echo     "s3_website_url": "http://%S3_URL%",
echo     "elastic_beanstalk_app": "%EB_APP_NAME%",
echo     "elastic_beanstalk_env": "%EB_ENV_NAME%",
echo     "backend_url": "http://%EB_URL%",
echo     "deployment_time": "%date% %time%"
echo }
) > aws-deployment-config.json

echo Deployment configuration file created: aws-deployment-config.json

echo.
echo AWS Cloud Service Deployment Complete!
echo ======================================
echo Frontend URL: http://%S3_URL%
echo Backend API URL: http://%EB_URL%
echo Deployment Config: aws-deployment-config.json
echo.
echo Next steps:
echo   1. Visit frontend URL to test functionality
echo   2. Use backend API URL to update frontend configuration
echo   3. Enjoy true cloud service experience!
echo.
echo Note: If some services are still creating, please wait a few minutes and retry

pause
