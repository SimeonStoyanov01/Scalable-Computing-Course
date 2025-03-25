#!/bin/bash

set -e

DRIVER_POD_NAME=$(kubectl get pods -o name | grep "ants-simulator-.*-driver" | cut -d'/' -f2)
echo "Driver pod = $DRIVER_POD_NAME"

kubectl delete pod $DRIVER_POD_NAME || true

helm uninstall simulator || true

helm upgrade --install \
    --set cmdParams[0].value="$1" \
    --set cmdParams[1].value="$2" \
    --set cmdParams[2].value="$3" \
    --set cmdParams[3].value="$4" \
    --set cmdParams[4].value="$5" \
    simulator . 

echo "Waiting for driver pod"
DRIVER_POD_NAME=""
while true; do
  DRIVER_POD_NAME=$(kubectl get pods -o name | grep "ants-simulator-.*-driver" | cut -d'/' -f2)

  if [[ -n "$DRIVER_POD_NAME" ]]; then
    DRIVER_POD_STATUS=$(kubectl get pod "$DRIVER_POD_NAME" -o jsonpath='{.status.phase}')

    if [[ "$DRIVER_POD_STATUS" == "Running" ]]; then
      echo "Driver pod '$DRIVER_POD_NAME' is running."
      break # Exit the loop when the pod is running
    else
      echo "Driver pod '$DRIVER_POD_NAME' found, but status is '$DRIVER_POD_STATUS'. Waiting..."
      sleep 1
    fi
  else
    sleep 1
  fi
done

echo "Driver pod name: $DRIVER_POD_NAME"

kubectl logs -f $DRIVER_POD_NAME