#!/bin/sh
helm install ants oci://registry-1.docker.io/bitnamicharts/kafka -f values.yaml
