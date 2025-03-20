#!/bin/bash

set -e

docker build -t makenjoy/ants-simulator:test2 .
docker push makenjoy/ants-simulator:test2

DRIVER_POD_NAME=$(kubectl get pods -o name | grep "ants-simulator-.*-driver" | cut -d'/' -f2)
gridRows=10
gridCols=10
numAnts=1
job_json="{\"gridRows\": $gridRows,\"gridCols\": $gridCols, \"ants\": $numAnts}"

echo "Driver pod = $DRIVER_POD_NAME"

kubectl delete pod $DRIVER_POD_NAME || true
kubectl delete job ants-simulator || true
kubectl apply -f ../charts/simulator/submit-spark-job.yaml

# for i in $(seq 1 3); do
#     kubectl exec -i kafka-client --namespace default -- kafka-console-producer.sh \
#             --bootstrap-server kafka.default.svc.cluster.local:9092 \
#             --topic jobs <<< $job_json
# done

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
rm simulation_times.txt
rm robin_logs.txt
kubectl logs -f $DRIVER_POD_NAME | tee >(grep -B $gridRows 'Simulation time:') >(stdbuf -oL grep 'Simulation time:' > simulation_times.txt) >(grep 'ROBIN') >(stdbuf -oL grep 'ROBIN' > robin_logs.txt) >(grep 'Received job=') >(grep 'error') > /dev/null