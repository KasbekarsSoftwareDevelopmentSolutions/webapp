packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-east-2"
}

variable "source_ami_id" {
  type    = string
  default = "ami-0ea3c35c5c3284d82" # Ubuntu 24.04 LTS 
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "profile" {
  type    = string
  default = "aadityaDevelopmentUser"
}

variable "aws_demoacc" {
  type    = string
  default = "820242918362"
}

variable "aws_devacc" {
  type    = string
  default = "390403856687"
}

variable "subnet_id" {
  type    = string
  default = "subnet-0d6a01e42fbb60bda"
}

source "amazon-ebs" "ubuntu" {
  region          = "${var.aws_region}"
  profile         = "${var.profile}"
  ami_name        = "CloudNativeApp_assignment4_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "AMI for CSYE 6225 of assignment 4"
  ami_regions = [
    "${var.aws_region}"
  ]
  ami_users = [
    "${var.aws_demoacc}",
    "${var.aws_demoacc}",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  instance_type = "t2.medium"
  source_ami    = "${var.source_ami_id}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"


  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 50
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "shell" {
    script = "updateOS.sh"
  }

  provisioner "shell" {
    script = "appDIRCreation.sh"
  }

  provisioner "file" {
    sources     = ["./target/movieRetirvalWebApp-0.0.1-SNAPSHOT.jar"]
    destination = "/tmp/"
  }

  # provisioner "file" {
  #   source      = "cloudwatch-config.json"
  #   destination = "/tmp/cloudwatch-config.json"
  # }

  provisioner "file" {
    sources     = ["cloud-native-app.service"]
    destination = "/tmp/"
  }

  provisioner "shell" {
    script = "jdkSetup.sh"
  }

  provisioner "shell" {
    script = "mysqlSetup.sh"
  }

  provisioner "shell" {
    script = "appSetup.sh"
  }

  post-processor "manifest" {
    output     = "manifest.json"
    strip_path = true
  }

  # provisioner "shell" {
  #   script = "cloudNativeApp-setup.sh"
  # }
}