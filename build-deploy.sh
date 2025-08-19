#!/bin/bash

# Build and Deploy Script for Vehicle Invoice Service
# This script builds the application and automatically deploys it to AWS EC2

echo "Building Vehicle Invoice Service..."

# Clean and build the project
./gradlew clean build

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Create deployment directory
    mkdir -p deployment
    
    # Copy JAR file
    cp build/libs/vehicle-invoice-service.jar deployment/
    
    # Copy deployment script
    cp deploy.sh deployment/
    
    # Copy README
    cp README.md deployment/
    
    echo "Deployment package created in 'deployment/' directory"
    echo ""
    echo "Transferring files to EC2..."
    
    # Transfer files to EC2
    scp -i ~/Downloads/somyajain49.pem -r deployment/ ubuntu@51.21.253.197:/home/ubuntu/
    
    if [ $? -eq 0 ]; then
        echo "Files transferred successfully!"
        echo ""
        echo "Deploying on EC2..."
        
        # SSH to EC2 and run deployment
        ssh -i ~/Downloads/somyajain49.pem ubuntu@51.21.253.197 << 'EOF'
            cd deployment
            chmod +x deploy.sh
            ./deploy.sh
            sudo cp vehicle-invoice-service.jar /opt/vehicle-invoice-service/
            sudo systemctl restart vehicle-invoice
            sudo systemctl enable vehicle-invoice
            echo "Deployment completed!"
            echo "Checking service status..."
            sudo systemctl status vehicle-invoice --no-pager
            echo ""
            echo "Testing application..."
            sleep 10
            curl -s http://51.21.253.197/api/invoice/health
            echo ""
            echo "Testing HTTPS..."
            curl -k -s https://51.21.253.197/api/invoice/health
            echo ""
            echo "Application is now running at:"
            echo "HTTP: http://51.21.253.197"
            echo "HTTPS: https://51.21.253.197"
EOF
        
        echo "Complete deployment process finished!"
        
    else
        echo "Failed to transfer files to EC2!"
        exit 1
    fi
    
else
    echo "Build failed!"
    exit 1
fi
