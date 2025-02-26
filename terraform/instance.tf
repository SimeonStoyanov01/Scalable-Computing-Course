# The actual Virtual Machine instance running Flatcar
resource "openstack_compute_instance_v2" "flatcar" {
    name = "Flatcar VM"
    image_id = data.openstack_images_image_v2.flatcar.id
    flavor_name = var.flavor_name
    
    user_data = data.ct_config.flatcar.rendered

    network {
        port = openstack_networking_port_v2.flatcar.id
    }
}
