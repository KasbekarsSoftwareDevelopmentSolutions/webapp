#!/bin/bash

set -e

# Notify starting of the process
echo "Starting the Cloud Watch setup process for ec2_instance. (using cloudWatchSetup.sh)"

# Step 1: Move the CloudWatch Agent configuration file to the correct directory
sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json

# Step 2: Configure and start the Amazon CloudWatch Agent
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json -s