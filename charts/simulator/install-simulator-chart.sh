#!/bin/bash

set -e

DRIVER_POD_NAME=$(kubectl get pods -o name | grep "ants-simulator-.*-driver" | cut -d'/' -f2)

echo "Driver pod = $DRIVER_POD_NAME"

kubectl delete pod $DRIVER_POD_NAME || true

helm uninstall simulator || true

ip_address=$(ip addr show | grep -oE '10\.10\.[0-9]+\.[0-9]+/16' | cut -d '/' -f 1)

until \
    helm upgrade --install \
        --set cmdParams[0].value="$1" \
        --set cmdParams[1].value="$2" \
        --set cmdParams[2].value="$3" \
        --set cmdParams[3].value="$4" \
        --set cmdParams[4].value="$5" \
        --set cmdParams[5].value="$6" \
        --set localmasterip="$ip_address" \
        simulator .; do
    echo "installing simulator failed"
    echo "$(date '+%H:%M:%S') installing simulator failed" >> simtimeslogs.txt
    sleep 5
done

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

echo "$(date '+%H:%M:%S') Driver pod name: $DRIVER_POD_NAME"

until kubectl logs -f $DRIVER_POD_NAME; do
    echo "$(date '+%H:%M:%S') log failed trying to connect to pod again" >> simtimeslogs.txt
    echo "log failed trying to connect to pod again"
    sleep 30
done