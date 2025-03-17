#
# NETWORKING CONFIGURATION
#
#  ------              --------------------                   --------------------
#  | VM |---- port ----| Internal Network |------router-------| External Network |
#  ------              --------------------                   --------------------
#                          10.10.0.0/16                          195.169.23.0/24
#
# The VM is linked to an external network by first going through
# an internal network (like at home). The internal and external
# networks are linked by a "router" resource (like at home). The
# VM is linked to the internal network through a "port" resource.
#
# A public IP is assigned to the port resource, in addition we
# define security groups (firewall rules) and link them to the
# port.
#
# So in this configuration file:
# - Find the external (public) network by name
# - Create an internal network
#   - Associate a subnet to the internal network
# - Create a router resource
#   - Link the router to the internal and external networks
# - Create a port resource
# - Allocate a new floating IP address from the pool of public IPs
#   - Associate the IP to the port
# - Create security groups

# Create internal network for machines
resource "openstack_networking_network_v2" "internal" {
    name = "internal"
    external = false
    shared = false
}

# Create subnet in this internal network
resource "openstack_networking_subnet_v2" "internal" {
    name = "internal"
    network_id = openstack_networking_network_v2.internal.id
    cidr = "10.10.0.0/16"
    ip_version = 4
    dns_nameservers = [ "1.1.1.1", "9.9.9.9" ]
}

# Find the public network (as data source; we assume it already exists)
data "openstack_networking_network_v2" "public" {
    name = var.public_network
}

# Create a "router" to bridge between private and public networks
resource "openstack_networking_router_v2" "router" {
    name = "internal_bridge"

    # "Outgoing" network
    external_network_id = data.openstack_networking_network_v2.public.id
}

# Associate internal network with the router
resource "openstack_networking_router_interface_v2" "internal" {
    router_id = openstack_networking_router_v2.router.id
    subnet_id = openstack_networking_subnet_v2.internal.id
}

# Create a port resource, linking VM and internal network
resource "openstack_networking_port_v2" "master" {
    network_id = openstack_networking_network_v2.internal.id
    admin_state_up = "true"
    security_group_ids = [
        openstack_networking_secgroup_v2.basic.id
    ]

    depends_on = [
        openstack_networking_subnet_v2.internal
    ]
}

resource "openstack_networking_port_v2" "worker" {
    count = 2
    network_id = openstack_networking_network_v2.internal.id
    admin_state_up = "true"
    security_group_ids = [
        openstack_networking_secgroup_v2.basic.id
    ]

    depends_on = [
        openstack_networking_subnet_v2.internal
    ]
}

# Allocate the floating IP from the public pool
resource "openstack_networking_floatingip_v2" "float_ip" {
    pool = var.public_network
}

resource "openstack_networking_floatingip_associate_v2" "master" {
    floating_ip = openstack_networking_floatingip_v2.float_ip.address
    port_id = openstack_networking_port_v2.master.id

    # We explicitly let the floating IP association depend on the existence
    # of the router between internal and external networks; otherwise we can't
    # make the association. A floating IP association can only be made between
    # connected networks.
    depends_on = [
        openstack_networking_router_interface_v2.internal
    ]
}

### Security group
resource "openstack_networking_secgroup_v2" "basic" {
    name = "Basic"
    description = "Basic security rules for SSH/HTTP(S)"
}

resource "openstack_networking_secgroup_rule_v2" "basic_ssh" {
    direction = "ingress"
    ethertype = "IPv4"
    protocol = "tcp"
    port_range_min = 22
    port_range_max = 22
    remote_ip_prefix = "0.0.0.0/0"
    security_group_id = openstack_networking_secgroup_v2.basic.id
}

resource "openstack_networking_secgroup_rule_v2" "basic_http" {
    direction = "ingress"
    ethertype = "IPv4"
    protocol = "tcp"
    port_range_min = 80
    port_range_max = 80
    remote_ip_prefix = "0.0.0.0/0"
    security_group_id = openstack_networking_secgroup_v2.basic.id
}

resource "openstack_networking_secgroup_rule_v2" "basic_https" {
    direction = "ingress"
    ethertype = "IPv4"
    protocol = "tcp"
    port_range_min = 443
    port_range_max = 443
    remote_ip_prefix = "0.0.0.0/0"
    security_group_id = openstack_networking_secgroup_v2.basic.id
}
