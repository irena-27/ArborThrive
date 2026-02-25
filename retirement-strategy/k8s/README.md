# Kubernetes deploy order

1. `kubectl apply -f k8s/namespace.yaml`
2. Replace `<ACR_LOGIN_SERVER>` in deployment YAMLs
3. `kubectl apply -f k8s/command-deployment.yaml -f k8s/command-service.yaml -f k8s/command-hpa.yaml`
4. `kubectl apply -f k8s/query-deployment.yaml -f k8s/query-service.yaml -f k8s/query-hpa.yaml`
5. `kubectl apply -f k8s/ingress.yaml`
