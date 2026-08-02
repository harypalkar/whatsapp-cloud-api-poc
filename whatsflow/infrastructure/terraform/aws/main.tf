terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" { region = var.region }

variable "region" { default = "ap-south-1" }

resource "aws_vpc" "whatsflow" {
  cidr_block = "10.20.0.0/16"
  tags = { Name = "whatsflow" }
}

# Extend with subnets, EKS/ECS, RDS, S3, ALB in environment-specific layers.
output "vpc_id" { value = aws_vpc.whatsflow.id }
