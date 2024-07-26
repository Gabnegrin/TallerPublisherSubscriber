#!/bin/bash
# wait-for-rabbitmq.sh

set -e

host="$1"
shift
cmd="$@"

until nc -z "$host" 5672; do
  echo "Waiting for RabbitMQ..."
  sleep 3
done

echo "RabbitMQ is up - executing command"
exec $cmd
