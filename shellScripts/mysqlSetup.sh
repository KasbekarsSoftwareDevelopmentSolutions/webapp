#!/bin/bash

set -e

# Start the installation process for MySQL 8.0
echo "Installing MySQL APT repository package... (using mysqlSetup.sh)"

# Install MySQL Server
echo "Installing MySQL 8.0 Server..."
sudo apt install -y mysql-server

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Start the MySQL service
echo "Starting MySQL service..."
sudo systemctl start mysql

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Check MySQL service status
echo "Checking MySQL service status..."
sudo systemctl status mysql --no-pager

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Optional: Enable MySQL service to start on boot
echo "Enabling MySQL service to start on boot..."
sudo systemctl enable mysql

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Completion message
echo "MySQL installation and setup complete."

# Log in as root user and create the user aadityaKasbekar
echo "Logging in to MySQL as root user to create new user..."
# Use the following command to log in; this assumes no password is set for root.
sudo mysql -u root -e "CREATE USER 'aadityaKasbekar'@'localhost' IDENTIFIED BY 'REclassic@3504792';"

# Check if the user was created successfully
if [ $? -eq 0 ]; then
    echo "User 'aadityaKasbekar' created successfully."
else
    echo "Failed to create user 'aadityaKasbekar'."
fi

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Create the CloudAppDB database
echo "Creating the CloudAppDB database..."
sudo mysql -u root -e "CREATE DATABASE CloudAppDB;"

# Check if the database was created successfully
if [ $? -eq 0 ]; then
    echo "Database 'CloudAppDB' created successfully."
else
    echo "Failed to create database 'CloudAppDB'."
fi

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Grant all privileges to the new user on the CloudAppDB database
echo "Granting privileges to 'aadityaKasbekar' on 'CloudAppDB'..."
sudo mysql -u root -e "GRANT ALL PRIVILEGES ON CloudAppDB.* TO 'aadityaKasbekar'@'localhost';"

# Flush privileges to ensure that the changes take effect
sudo mysql -u root -e "FLUSH PRIVILEGES;"

# List all databases
echo "Listing all databases..."
sudo mysql -u root -e "SHOW DATABASES;"

# List all tables in CloudAppDB
echo "Listing all tables in CloudAppDB..."
sudo mysql -u root -D CloudAppDB -e "SHOW TABLES;"

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds after mysql setup completed..."
sleep 10