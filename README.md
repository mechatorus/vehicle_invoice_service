# Vehicle Invoice Service - Indian Standards

A Spring Boot application that generates PDF invoices for vehicle sales with Indian standards compliance and AWS deployment.

## Features

- ✅ Indian vehicle registration number validation
- ✅ GST calculation (CGST + SGST)
- ✅ PDF invoice generation with QR code
- ✅ AWS EC2 deployment ready
- ✅ PostgreSQL database

## Quick Start

### Local Development

```bash
# Build and run
./gradlew build
./gradlew bootRun

# Access: http://localhost:8080
```

### AWS Deployment

```bash
# Build and package
./gradlew clean build
./build-deploy.sh

# Transfer to EC2
scp -i ~/Downloads/your-key.pem -r deployment/ ubuntu@your-ec2-ip:/home/ubuntu/

# Deploy on EC2
ssh -i ~/Downloads/your-key.pem ubuntu@your-ec2-ip
cd deployment && ./deploy.sh
```

## Database

### Local (H2)
- URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

### Production (PostgreSQL)
- Host: localhost
- Database: `vehicle_invoice_db`
- Username: `vehicle_user`
- Password: `vehicle123`

## API Usage

### Generate Invoice

```bash
curl -X POST https://your-ec2-ip/api/invoice/generate \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Rahul Verma",
    "customerAddress": "456 MG Road, Indore, MP 452001",
    "customerPhone": "7311234569",
    "customerEmail": "rahul@email.com",
    "dealerId": 1,
    "vehicleId": 1,
    "subtotal": 1500000.00,
    "cgstAmount": 135000.00,
    "sgstAmount": 135000.00,
    "totalGST": 270000.00,
    "totalAmount": 1770000.00,
    "transactionId": "TXN123456789",
    "qrCodeData": "https://example.com/invoice/123"
  }' \
  --output invoice.pdf
```
Actual Curl

```bash
curl --location 'https://51.21.253.197/api/invoice/generate' \
--header 'Content-Type: application/json' \
--data-raw '{"customerName":"Rahul Verma","customerAddress":"456 MG Road, Indore, MP 452001","customerPhone":"7311234569","customerEmail":"rahul@email.com","dealerId":1,"vehicleId":1}'
```



### Health Check

```bash
curl https://your-ec2-ip/api/invoice/health
```

## Sample Data

### Dealers
- **Mahindra Auto Sales** (ID: 1)
  - GST: 23AABCM1234A1Z5
  - Address: Indore, Madhya Pradesh

- **Tata Motors Showroom** (ID: 2)
  - GST: 33AABCT5678B2Z6
  - Address: Chennai, Tamil Nadu

### Vehicles
- **Mahindra XUV700** (ID: 1)
  - Registration: MP-01-AB-1234
  - Price: ₹15,00,000

- **Tata Nexon EV** (ID: 2)
  - Registration: TN-02-CD-5678
  - Price: ₹18,00,000

## Indian Standards

### Vehicle Fields
- Registration Number (Indian format)
- Make, Model, Year
- Price

### Dealer Fields
- GST Number (15-character format)
- Name, Address, Phone, Email

### Invoice Fields
- CGST Amount (9% of subtotal)
- SGST Amount (9% of subtotal)
- Total GST (18% of subtotal)

## Technology Stack

- Spring Boot 2.7.18
- PostgreSQL / H2
- iText7 (PDF generation)
- ZXing (QR code)
- Nginx (reverse proxy)
- AWS EC2

## Troubleshooting

```bash
# Check service status
sudo systemctl status vehicle-invoice-service

# View logs
sudo journalctl -u vehicle-invoice-service -f

# Restart service
sudo systemctl restart vehicle-invoice-service
```


