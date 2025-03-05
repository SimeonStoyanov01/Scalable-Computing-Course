resource "openstack_compute_instance_v2" "master" {
  name            = "Master"
  image_name      = data.openstack_images_image_v2.ubuntu.name
  flavor_name     = var.flavor_name_master
  security_groups = ["default", openstack_networking_secgroup_v2.basic.id]

  user_data = data.cloudinit_config.master.rendered

  block_device {
    uuid                  = data.openstack_images_image_v2.ubuntu.id
    source_type           = "image"
    volume_size           = 50
    boot_index            = 0
    destination_type      = "volume"
    delete_on_termination = true
  }

  network {
    uuid = openstack_networking_network_v2.internal.id
  }

  depends_on = [
    openstack_networking_router_v2.router,
    openstack_networking_router_interface_v2.internal
  ]
}

resource "openstack_compute_instance_v2" "worker" {
  count = 2
  name = "Worker-${count.index + 1}"
  image_name      = data.openstack_images_image_v2.ubuntu.name
  flavor_name     = var.flavor_name_worker
  security_groups = ["default", openstack_networking_secgroup_v2.basic.id]

  user_data = data.cloudinit_config.worker.rendered

  block_device {
    uuid                  = data.openstack_images_image_v2.ubuntu.id
    source_type           = "image"
    volume_size           = 50
    boot_index            = 0
    destination_type      = "volume"
  }

  network {
    uuid = openstack_networking_network_v2.internal.id
  }

  depends_on = [
    openstack_networking_router_v2.router,
    openstack_networking_router_interface_v2.internal
  ]
}