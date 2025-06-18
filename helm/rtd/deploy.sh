#!/bin/bash

# Usage:
#   ./deploy.sh <env> <app name or release name>
#
# Example:
#   ./deploy.sh dev my-app-release
#
# This script will:
#   - Read the namespace and keyvault name from the provided values file
#   - Retrieve the workload identity client ID from Azure Key Vault
#   - Show the current Kubernetes context
#   - Render the Helm template for review
#   - Ask for confirmation before running the Helm upgrade/install
#   - Deploy the Helm chart to the current cluster context

set -e  # Exit the script if any command fails

# Function to handle errors
handle_error() {
    echo "❌ Error: $1" >&2
    exit 1
}

# Remove CLUSTER_NAME parameter and logic
# ENV parameter instead of VALUES_FILE_NAME
ENV=$1
APP_NAME=$2
SECRET_NAME="rtd-workload-identity-client-id"

if [ -z "$ENV" ] || [ -z "$APP_NAME" ]; then
    handle_error "All parameters are required: ENV APP_NAME"
fi

# Determine values file paths
VALUES_FILE_ENV="values-$ENV.yaml"
VALUES_FILE_GLOBAL="values.yaml"

if [ ! -f "$VALUES_FILE_ENV" ]; then
    handle_error "File $VALUES_FILE_ENV not found."
fi
if [ ! -f "$VALUES_FILE_GLOBAL" ]; then
    handle_error "File $VALUES_FILE_GLOBAL not found."
fi

# Use ENV-specific values file for extracting namespace/keyvault
NAMESPACE=$(yq eval '.microservice-chart.namespace' "$VALUES_FILE_GLOBAL")

if [ -z "$NAMESPACE" ]; then
    echo "Errore: Impossible to read the namespace"
    exit 1
fi

KEYVAULT_NAME=$(yq eval '.microservice-chart.keyvault.name' "$VALUES_FILE_ENV")

if [ -z "$KEYVAULT_NAME" ]; then
    echo "Errore: Impossibile leggere il nome del Key Vault dal file YAML"
    exit 1
fi

CLIENT_ID=$(az keyvault secret show --name "$SECRET_NAME" --vault-name "$KEYVAULT_NAME" --query "value" -o tsv)

# Verifica che il segreto sia stato recuperato correttamente
if [ -z "$CLIENT_ID" ]; then
    echo "Errore: Impossible to read the secret value"
    exit 1
fi

#
# K8s
#
if ! command -v kubectl &> /dev/null; then
    handle_error "kubectl is not installed. Please install it and try again."
fi

if ! command -v helm &> /dev/null; then
    handle_error "Helm is not installed. Please install it and try again."
fi

CURRENT_CONTEXT=$(kubectl config current-context)
echo "ℹ️ Current Kubernetes context: $CURRENT_CONTEXT"

#
# ⎈ HELM
#
echo "🪚 Deleting charts folder"
rm -rf charts || handle_error "Unable to delete charts folder"

echo "🔨 Starting Helm Template"
helm dep build && \
helm template . -f "$VALUES_FILE_ENV" -f "$VALUES_FILE_GLOBAL" \
  --set microservice-chart.azure.workloadIdentityClientId="$CLIENT_ID" \
  --debug

echo "Do you want to proceed with the Helm upgrade? (yes/no)"
read -r user_input
if [[ "$user_input" != "yes" ]]; then
  echo "Helm upgrade aborted by user."
  exit 0
fi

echo "🚀 Launch helm deploy"
# Execute helm upgrade/install command and capture output and exit code
helm upgrade --namespace "$NAMESPACE" \
    --install \
    --set microservice-chart.azure.workloadIdentityClientId="$CLIENT_ID" \
    --values "$VALUES_FILE_ENV" --values "$VALUES_FILE_GLOBAL" \
    --wait --debug --timeout 15m0s "$APP_NAME" .

exit_code=$?

# Check the command result
if [ $exit_code -ne 0 ]; then
    handle_error "Failed to upgrade/install Helm chart"
else
    echo "✅ Release installation completed successfully"
fi
