# Authentication on the control panel

### Issue:

manager@cka-manager:~/quarkus-gitops/argocd$ kubectl apply -f application.yaml
error: resource mapping not found for name: "quarkus-gitops" namespace: "argocd" from "application.yaml": no matches for kind "Application" in version "argoproj.io/v1alpha1"
ensure CRDs are installed first
manager@cka-manager:~/quarkus-gitops/argocd$ kubectl get applications -n argocd
error: the server doesn't have a resource type "applications"
manager@cka-manager:~/quarkus-gitops/argocd$ kubectl get all -n argocd
Error from server (Forbidden): pods is forbidden: User "kubernetes-admin" cannot list resource "pods" in API group "" in the namespace "argocd"
Error from server (Forbidden): replicationcontrollers is forbidden: User "kubernetes-admin" cannot list resource "replicationcontrollers" in API group "" in the namespace "argocd"
Error from server (Forbidden): services is forbidden: User "kubernetes-admin" cannot list resource "services" in API group "" in the namespace "argocd"
Error from server (Forbidden): daemonsets.apps is forbidden: User "kubernetes-admin" cannot list resource "daemonsets" in API group "apps" in the namespace "argocd"
Error from server (Forbidden): deployments.apps is forbidden: User "kubernetes-admin" cannot list resource "deployments" in API group "apps" in the namespace "argocd"
Error from server (Forbidden): replicasets.apps is forbidden: User "kubernetes-admin" cannot list resource "replicasets" in API group "apps" in the namespace "argocd"
Error from server (Forbidden): statefulsets.apps is forbidden: User "kubernetes-admin" cannot list resource "statefulsets" in API group "apps" in the namespace "argocd"
Error from server (Forbidden): horizontalpodautoscalers.autoscaling is forbidden: User "kubernetes-admin" cannot list resource "horizontalpodautoscalers" in API group "autoscaling" in the namespace "argocd"
Error from server (Forbidden): cronjobs.batch is forbidden: User "kubernetes-admin" cannot list resource "cronjobs" in API group "batch" in the namespace "argocd"
Error from server (Forbidden): jobs.batch is forbidden: User "kubernetes-admin" cannot list resource "jobs" in API group "batch" in the namespace "argocd"
manager@cka-manager:~/quarkus-gitops/argocd$ kubectl get nodes
Error from server (Forbidden): nodes is forbidden: User "kubernetes-admin" cannot list resource "nodes" in API group "" at the cluster scope
manager@cka-manager:~/quarkus-gitops/argocd$ 

### Steps to solve the issue:

manager@cka-manager:~$ mv ~/.kube/config ~/.kube/config.broken
manager@cka-manager:~$ sudo cp /etc/kubernetes/admin.conf ~/.kube/config
manager@cka-manager:~$ sudo chown $(id -u):$(id -g) ~/.kube/config

$ kubectl auth can-i '*' '*'
error: error loading config file "/etc/kubernetes/admin.conf": open /etc/kubernetes/admin.conf: permission denied
manager@cka-manager:~$ unset KUBECONFIG
manager@cka-manager:~$ ls -l ~/.kube/config
-rw------- 1 manager manager 5654 Jan 26 06:15 /home/manager/.kube/config
manager@cka-manager:~$ sudo cp /etc/kubernetes/admin.conf ~/.kube/config
manager@cka-manager:~$ sudo cp /etc/kubernetes/admin.conf ~/.kube/config
manager@cka-manager:~$ chmod 600 ~/.kube/config
manager@cka-manager:~$ kubectl config current-context
kubernetes-admin@kubernetes

$ sudo KUBECONFIG=/etc/kubernetes/super-admin.conf kubectl create clusterrolebinding kubernetes-admin-binding \
  --clusterrole=cluster-admin \
  --user=kubernetes-admin
clusterrolebinding.rbac.authorization.k8s.io/kubernetes-admin-binding created
manager@cka-manager:~$ unset KUBECONFIG
manager@cka-manager:~$ kubectl get nodes
NAME          STATUS   ROLES    AGE    VERSION
cka-manager   Ready    <none>   7d1h   v1.32.10
cka-nodeone   Ready    <none>   7d1h   v1.32.10
cka-nodetwo   Ready    <none>   7d1h   v1.32.1
