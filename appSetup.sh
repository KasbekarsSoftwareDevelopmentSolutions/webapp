#!/bin/bash

set -e

# Notify starting of the process
echo "Starting the setup process for movieRetirvalWebApp. (using appSetup.sh)"

# Step 1: Copy the application JAR to the target directory
echo "Copying the application JAR to /opt/cloudNativeApplicationFolder..."
sudo cp /tmp/movieRetirvalWebApp-0.0.1-SNAPSHOT.jar /opt/cloudNativeApplicationFolder/movieRetirvalWebApp-0.0.1-SNAPSHOT.jar

# Step 2: Copy the systemd service file to the correct location
echo "Copying the systemd service file to /etc/systemd/system/..."
sudo cp /tmp/cloud-native-app.service /etc/systemd/system/cloud-native-app.service

# Step 3: Reload systemd to recognize the new service
echo "Reloading systemd daemon to apply changes..."
sudo systemctl daemon-reload

# Step 4: Enable the service to start at boot
echo "Enabling the service to start at boot..."
sudo systemctl enable cloud-native-app.service

# Step 5: Change ownership of the application folder to the csye6225 user
echo "Changing ownership of /opt/cloudNativeApplicationFolder to user csye6225..."
sudo chown -R csye6225:csye6225 /opt/cloudNativeApplicationFolder

# Step 6: Set the necessary file permissions
echo "Setting file permissions for /opt/cloudNativeApplicationFolder..."
sudo chmod -R 750 /opt/cloudNativeApplicationFolder

# Step 7: Make the JAR file executable (if needed)
echo "Making the JAR file executable..."
sudo chmod +x /opt/cloudNativeApplicationFolder/movieRetirvalWebApp-0.0.1-SNAPSHOT.jar

# Notify completion
echo "Setup process completed successfully."
