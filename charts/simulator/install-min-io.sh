#!/bin/sh
helm repo add minio-operator https://operator.min.io
#helm install operator minio-operator/operator
helm install \
  --namespace minio-operator \
  --create-namespace \
  operator minio-operator/operator \
&& helm install \
--namespace myminio \
--create-namespace \
--values minio/values.yaml \
myminio minio-operator/tenant

# helm install --values minio/values.yaml myminio minio-operator/tenant
