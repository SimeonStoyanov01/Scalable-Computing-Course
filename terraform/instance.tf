resource "openstack_compute_instance_v2" "master" {
  name            = "Master"
  image_name      = data.openstack_images_image_v2.ubuntu.name
  flavor_name     = var.flavor_name_master

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
    port = openstack_networking_port_v2.master.id
    # uuid = openstack_networking_network_v2.internal.id
  }

  depends_on = [
    openstack_networking_router_v2.router,
    openstack_networking_router_interface_v2.internal
  ]
}

resource "null_resource" "copy_helm_charts" {
  depends_on = [ openstack_compute_instance_v2.master ]

  provisioner "file" {
    source      = "../charts"
    destination = "/home/ubuntu/charts"
  }

  connection {
    type     = "ssh"
    user     = "ubuntu"
    private_key = file("${path.module}/id_rsa")
    host     = "${openstack_networking_floatingip_v2.float_ip.address}"
  }
}

resource "openstack_compute_instance_v2" "worker" {
  count = 1
  name = "Worker-${count.index + 1}"
  image_name      = data.openstack_images_image_v2.ubuntu.name
  flavor_name     = var.flavor_name_worker

  user_data = data.cloudinit_config.worker.rendered

  block_device {
    uuid                  = data.openstack_images_image_v2.ubuntu.id
    source_type           = "image"
    volume_size           = 50
    boot_index            = 0
    destination_type      = "volume"
    delete_on_termination = true
  }

  network {
    # uuid = openstack_networking_network_v2.internal.id
    port = openstack_networking_port_v2.worker[count.index].id
  }

  depends_on = [
    openstack_networking_router_v2.router,
    openstack_networking_router_interface_v2.internal
  ]
}