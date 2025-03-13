#!/bin/sh
SCRIPT_DIR=$(dirname "$0")
helm upgrade --install ants oci://registry-1.docker.io/bitnamicharts/kafka -f $SCRIPT_DIR/values.yaml
