locals {
   timestamp = regex_replace(timestamp(), "[- TZ:]", "")
 }

packer {
  required_plugins {
    amazon = {
      source  = "github.com/hashicorp/amazon"
      version = "~> 1"
    }
  }
}

 source "amazon-ebs" "oqm-ami" {
   ami_name      = "oqm-ubuntu-aws-${local.timestamp}"
   instance_type = var.instance_type
   region        = var.aws_region
   source_ami    = "ami-07d9b9ddc6cd8dd30"
   ssh_username = "ubuntu"
   ami_description = "The first official OQM AMI released on Ubuntu 22.04.  Use this AMI to deploy a Open Quarter Master single instance"

   tags = {
     "Name"        = "OQM Deployment"
     "Environment" = "Production"
     "OS_Version"  = "Ubuntu 22.04"
     "Created-by"  = "Epic Breakfast Productions"
   }
 }