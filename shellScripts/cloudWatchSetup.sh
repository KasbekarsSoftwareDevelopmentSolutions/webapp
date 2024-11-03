#!/bin/bash

set -e

# Notify starting of the process
echo "Starting the CloudWatch setup process for EC2 instance using cloudWatchSetup.sh."

# Step 1: Download and install the CloudWatch Agent using the .deb package
echo "Downloading and installing Amazon CloudWatch Agent."
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i -E ./amazon-cloudwatch-agent.deb

# Step 2: Move the CloudWatch Agent configuration file to the correct directory
echo "Moving CloudWatch Agent configuration file to /opt/aws/amazon-cloudwatch-agent."
sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json

# Step 3: Set permissions for the configuration file
echo "Setting permissions for CloudWatch configuration file."
sudo chmod 744 /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json

# Step 4: Configure and start the Amazon CloudWatch Agent
echo "Configuring and starting the Amazon CloudWatch Agent."
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json -s

# Clean up
rm -f amazon-cloudwatch-agent.deb

# Notify completion
echo "CloudWatch Agent setup completed successfully."
