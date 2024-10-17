#!/bin/bash

# Notify that the script has started
echo "Starting application directory setup... (using appDirCreation.sh)"

# Stop script execution on any error
set -e

# Step 1: Define the directory for the application
DIR=/opt/cloudNativeApplicationFolder

# Step 2: Create the application directory if it doesn't exist
echo "Creating application directory at ${DIR}..."
sudo mkdir -p "${DIR}"

# Step 3: Add a new user for the application
echo "Adding user 'csye6225' with no login shell..."
sudo adduser csye6225 --shell /usr/sbin/nologin

# Notify that the script has completed
echo "Application directory setup completed successfully."
