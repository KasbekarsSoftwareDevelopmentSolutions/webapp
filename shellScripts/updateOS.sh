#!/bin/bash

# Notify that the script has started
echo "Starting environment setup... (using updateOS.sh)"

# Stop script execution on any error
set -e

# Step 1: Set environment variables
echo "Setting up environment variables for non-interactive mode and disabling checkpoint."
export DEBIAN_FRONTEND=noninteractive
export CHECKPOINT_DISABLE=1

# Step 2: Update the package list
echo "Updating package list..."
sudo apt-get update -y

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure updates are complete..."
sleep 10

# Step 3: Upgrade all installed packages to their latest versions
echo "Upgrading installed packages to the latest versions..."
sudo apt-get upgrade -y

# Step 4: Clean the local package cache
echo "Cleaning the package cache..."
sudo apt-get clean

# Step 5: Update the package list again (just in case)
echo "Updating package list again..."
sudo apt update -y

# Notify that the script has completed
echo "Environment setup completed successfully."

# Wait for 10 seconds before the next operation (optional)
echo "Waiting for 10 seconds to ensure updates are complete..."
sleep 10
