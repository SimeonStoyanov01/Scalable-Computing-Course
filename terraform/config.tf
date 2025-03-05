# Converting the user-readable Container Linux Configuration in flatcar_config.yaml
# into a machine-readable Ignition config file using the "Config Transpiler" ct.
#
# Also performs some template magic on this file to import the ssh keys to use.

data "cloudinit_config" "master" {
  gzip          = true
  base64_encode = true

  part {
    content_type = "text/cloud-config"
    content = templatefile("${path.module}/configs/cloud-init_master.yaml", {
      sshkey   = file("${path.module}/id_rsa.pub"),
      password = var.ubuntu_password,
      K3S_TOKEN = var.K3S_TOKEN
    })
  }

  part {
    filename   = "cloud-init-master.yaml"
    content_type = "text/cloud-config"
    content = templatefile("${path.module}/configs/cloud-init_master.yaml", {
      imagename = var.image_name,
      K3S_TOKEN = var.K3S_TOKEN
    })
  }  
}

data "cloudinit_config" "worker" {
  gzip          = true
  base64_encode = true

  part {
    filename     = "cloud-init-worker.yaml"
    content_type = "text/cloud-config"
    content = templatefile("${path.module}/configs/cloud-init_worker.yaml", {
      # give the internal ip of the master
      K3S_TOKEN           = var.K3S_TOKEN,
      K3S_URL             = "https://${openstack_compute_instance_v2.master.network.0.fixed_ip_v4}:6443"
    })
  }
}