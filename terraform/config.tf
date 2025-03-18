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
      K3S_TOKEN = var.K3S_TOKEN
    })
  }
   part {
    content_type = "text/cloud-config"
    content = templatefile("${path.module}/configs/snippets/users.yaml", {
      sshkey   = file("${path.module}/id_rsa.pub"),
      sshprivate   = file("${path.module}/id_rsa"),
      password = var.ubuntu_password
    })
  }

}

data "cloudinit_config" "worker" {
  gzip          = true
  base64_encode = true
  part {
    content_type = "text/cloud-config"
    content = templatefile("${path.module}/configs/snippets/users.yaml", {
      sshkey   = file("${path.module}/id_rsa.pub"),
      sshprivate   = file("${path.module}/id_rsa"),
      password = var.ubuntu_password
    })
  }

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