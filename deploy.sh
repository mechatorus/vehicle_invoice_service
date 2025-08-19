#!/bin/bash

# Application Deployment Script for Vehicle Invoice Service
# This script deploys the application on an existing EC2 instance

echo "Starting Application Deployment for Vehicle Invoice Service"

# Create application directory if it doesn't exist
echo "Setting up application directory..."
sudo mkdir -p /opt/vehicle-invoice-service
sudo chown ubuntu:ubuntu /opt/vehicle-invoice-service

# Create systemd service if it doesn't exist
echo "Creating systemd service..."
sudo tee /etc/systemd/system/vehicle-invoice.service << EOF
[Unit]
Description=Vehicle Invoice Service
After=network.target postgresql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/vehicle-invoice-service
ExecStart=/usr/bin/java -Xmx512m -Xms256m -jar vehicle-invoice-service.jar
Restart=always
RestartSec=10
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DATABASE_URL=jdbc:postgresql://localhost:5432/vehicle_invoice_db"
Environment="DATABASE_USERNAME=vehicle_user"
Environment="DATABASE_PASSWORD=vehicle123"

[Install]
WantedBy=multi-user.target
EOF

# Generate self-signed SSL certificate
echo "Generating SSL certificate..."
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/vehicle-invoice.key \
    -out /etc/ssl/certs/vehicle-invoice.crt \
    -subj "/C=IN/ST=State/L=City/O=VehicleInvoice/CN=51.21.253.197"

# Configure Nginx with HTTPS
echo "Configuring Nginx with HTTPS..."
sudo tee /etc/nginx/sites-available/vehicle-invoice-service << EOF
server {
    listen 80;
    server_name 51.21.253.197;
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name 51.21.253.197;

    ssl_certificate /etc/ssl/certs/vehicle-invoice.crt;
    ssl_certificate_key /etc/ssl/private/vehicle-invoice.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    location /health {
        proxy_pass http://localhost:8080/api/invoice/health;
        access_log off;
    }
}
EOF

# Enable the site and restart Nginx
sudo ln -sf /etc/nginx/sites-available/vehicle-invoice-service /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl enable nginx

# Reload systemd
sudo systemctl daemon-reload

echo "Deployment script completed!"
echo ""
echo "Next steps:"
echo "1. Copy the JAR file: sudo cp vehicle-invoice-service.jar /opt/vehicle-invoice-service/"
echo "2. Start the service: sudo systemctl start vehicle-invoice"
echo "3. Enable auto-start: sudo systemctl enable vehicle-invoice"
echo "4. Check status: sudo systemctl status vehicle-invoice"
echo ""
echo "Application will be available at:"
echo "HTTP: http://51.21.253.197"
echo "HTTPS: https://51.21.253.197"
