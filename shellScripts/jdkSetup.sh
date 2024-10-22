#!/bin/bash

set -e

# Notify the user that the installation of OpenJDK 21 is starting
echo 'Starting installation of OpenJDK 21... (using jdkSetup.sh)'

# Step 1: Navigate to the home directory
cd ~
echo "Changed directory to the home directory."

# Step 2: Install wget, a utility for downloading files
echo "Installing wget..."
sudo apt install wget -y
echo "wget installed successfully."

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Step 3: Install OpenJDK 21
echo "Installing OpenJDK 21..."
sudo apt install openjdk-21-jdk -y
echo "OpenJDK 21 installation initiated."

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

# Step 4: Verify the installation by checking the Java version
echo "Checking the installed Java version..."
sudo java -version

# Notify the user that the installation was successful
echo 'OpenJDK 21 installed successfully.'

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure all actions are complete..."
sleep 10

