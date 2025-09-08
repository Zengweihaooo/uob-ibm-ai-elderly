@echo off
echo Starting Spring Boot Application in AWS mode...
echo Using AWS SNS, SES, and DynamoDB
echo.
echo Please set your AWS credentials first:
echo set AWS_ACCESS_KEY_ID=your_access_key
echo set AWS_SECRET_ACCESS_KEY=your_secret_key
echo.
echo Current AWS credentials:
if defined AWS_ACCESS_KEY_ID (
    echo AWS_ACCESS_KEY_ID is set
) else (
    echo AWS_ACCESS_KEY_ID is NOT set
)

if defined AWS_SECRET_ACCESS_KEY (
    echo AWS_SECRET_ACCESS_KEY is set
) else (
    echo AWS_SECRET_ACCESS_KEY is NOT set
)
echo.

cd springboot
mvn spring-boot:run -Dspring-boot.run.profiles=aws
pause
