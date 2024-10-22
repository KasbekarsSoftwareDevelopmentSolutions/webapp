#!/bin/bash

# Notify that the script has started
echo "Starting application directory setup... (using appDirCreation.sh)"

# Stop script execution on any error
set -e

# Step 1: Define the directory for the application
DIR_APP_BIN=/opt/cloudNativeApplicationFolder
DIR_APP_LOGS=/home/csye6225/logs

# Step 2: Create the application directories if it doesn't exist
echo "Creating application directory at ${DIR_APP_BIN}..."
sudo mkdir -p "${DIR_APP_BIN}"

echo "Creating application logs directory at ${DIR_APP_LOGS}..."
sudo mkdir -p "${DIR_APP_LOGS}"

# Step 3: Add a new user for the application
echo "Adding user 'csye6225' with no login shell..."
sudo adduser csye6225 --shell /usr/sbin/nologin

# Notify that the script has completed
echo "Application directory setup completed successfully."

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10
