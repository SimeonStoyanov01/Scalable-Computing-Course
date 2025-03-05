# Defining required providers
terraform {
    required_providers {
        openstack = {
            source = "terraform-provider-openstack/openstack"
            version = "3.0.0"
        }

        ct = {
            source = "poseidon/ct"
            version = "0.13.0"
        }

        cloudinit = {
            source  = "hashicorp/cloudinit"
            version = "2.2.0"
        }
    }
}

# Configuration for OpenStack provider
provider "openstack" {
    auth_url = var.openstack_url

    user_domain_name = var.openstack_domainname
    user_domain_id = var.openstack_domainid

    region = var.openstack_region
    application_credential_id = var.openstack_credentialid
    application_credential_secret = var.openstack_credentialsecret
    
    tenant_id = var.openstack_projectid
}
