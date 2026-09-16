# Developer Setup

## Argo Setup

 1. Have minikube running
 2. `kubectl create ns argocd`
 3. `kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/v2.5.8/manifests/install.yaml`
    - Get latest yaml from: https://github.com/argoproj/argo-cd/releases
 4. Verify: `kubectl get all -n argocd`
 5. Access by: `kubectl port-forward svc/argocd-server -n argocd 8080:443`
    - username: `admin`
    - pass via: `kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo`
 6. 
