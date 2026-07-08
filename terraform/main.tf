# Terraform Infrastructure
#
# Bootstrap placeholder. Cloud infrastructure will be defined in deployment milestone.
#
# Planned modules:
# - neon-postgresql (database clusters per service)
# - redis (Elasticache / managed Redis)
# - kafka (MSK / Confluent)
# - elasticsearch (managed cluster)
# - kubernetes (EKS / GKE / AKS)
# - networking (VPC, subnets, security groups)

terraform {
  required_version = ">= 1.9.0"

  required_providers {
    # Provider blocks will be added per cloud target
  }
}

# variable "environment" {
#   description = "Deployment environment (dev, qa, prod)"
#   type        = string
# }

# variable "region" {
#   description = "Cloud region"
#   type        = string
#   default     = "ap-south-1"
# }
