# OpenStack auth settings
variable "openstack_url" {
    type = string
    description = "OpenStack Auth URL"
}

variable "openstack_domainid" {
    type = string
    description = "OpenStack Domain ID"
    default = null
}

variable "openstack_domainname" {
    type = string
    description = "OpenStack Domain Name"
    default = null
}

variable "openstack_credentialid" {
    type = string
    description = "OpenStack Application Credential ID"
    sensitive = true
}

variable "openstack_credentialsecret" {
    type = string
    description = "OpenStack Application Credential Secret"
    sensitive = true
}

variable "openstack_projectid" {
    type = string
    description = "OpenStack Project ID"
    sensitive = true
}

variable "openstack_region" {
    type = string
    description = "OpenStack Region"
    default = ""
}

# Other environment settings
variable "image_name" {
    type = string
    description = "Image name to be used"
}

variable "backend_image" {
    type = string
    description = "Backup image name to be used"
  
}

variable "frontend_image" {
    type = string
    description = "Frontend image name to be used"
}

variable "public_network" {
    type = string
    description = "Name of the public network to connect to"
}

variable "K3S_TOKEN" {
    type = string
    description = "K3S authentication token"
    sensitive = true
}

variable "ubuntu_password" {
    type = string
    description = "Password for the Ubuntu user"
    sensitive = true
}

variable "flavor_name_master" {
    type = string
    description = "Flavor to use for the master instance"
}

variable "flavor_name_worker" {
    type = string
    description = "Flavor to use for the worker instances"
}