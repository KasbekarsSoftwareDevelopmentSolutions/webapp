#!/bin/bash

# echo 'installing Open jdk 21'
# cd ~
# sudo apt install wget -y
# echo "sleeping....."
# sleep 10
# sudo apt install openjdk-21-jdk -y
# echo "sleeping....."
# sleep 10
# sudo java -version
# echo 'installed java successfully.'

# # Install MySQL 8.0
# echo "Installing MySQL APT repository package..."
# wget https://dev.mysql.com/get/mysql-apt-config_0.8.24-1_all.deb
# sudo dpkg -i mysql-apt-config_0.8.24-1_all.deb

# # Automatically select MySQL 8.0 without prompting for user input
# sudo sed -i 's/mysql-8.0-dmr/mysql-8.0/' /etc/apt/sources.list.d/mysql.list

# echo "Updating package list after adding MySQL repository..."
# sudo apt update -y

# echo "Installing MySQL 8.0 Server..."
# sudo apt install -y mysql-server

# # Start the MySQL service
# echo "Starting MySQL service..."
# sudo systemctl start mysql

# # Check MySQL service status
# echo "Checking MySQL service status..."
# sudo systemctl status mysql --no-pager

# # Optional: Enable MySQL service to start on boot
# sudo systemctl enable mysql

# # Clean up the MySQL APT package
# echo "Cleaning up..."
# rm mysql-apt-config_0.8.24-1_all.deb

# echo "MySQL installation and setup complete."

# echo "sleeping....."

# # Connect to MySQL (optional, uncomment if needed)
# # echo "Connecting to MySQL..."
# # mysql -u root -p

# # Install Amazon CloudWatch Agent
# sudo apt install amazon-cloudwatch-agent -y

# sudo chmod 544 /tmp/cloud-native-app.service
# sudo chmod 774 /tmp/cloudwatch-config.json
# sudo mv /tmp/cloud-native-app.service /etc/systemd/system/health-check-api.service
# sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json
# sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json -s

# pwd
# sudo mkdir logs
# sudo chown -R ubuntu logs
# cd logs
# pwd
