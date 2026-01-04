# GitOps Troubleshooting & Fixes (Quarkus + Redis + ArgoCD)

This document captures **all major issues faced during the setup of this project** and the **exact steps used to resolve them**. It is intentionally detailed so it can be used for:

* Interview discussion
* Future self‑reference
* Debugging similar GitOps issues

---

## 1. GitHub Actions Workflow Not Triggering

### ❌ Problem

* `.github/workflows` folder existed but **no workflow was running**
* GitHub Actions page showed **no workflows**

### 🔍 Root Cause

* Workflow file was either:

  * Not committed
  * Not under `.github/workflows/`
  * `on:` trigger paths didn’t match repo structure

### ✅ Fix

1. Created workflow at correct path:

   ```
   .github/workflows/ci-build-push.yaml
   ```
2. Ensured correct trigger:

   ```yaml
   on:
     push:
       branches:
         - main
   ```
3. Committed and pushed to `main`

📌 **Learning**: GitHub Actions only detects workflows in `.github/workflows` on default branch.

---
## 2. Use GitHub Personal Access Token (HTTPS)

### Problem

```md
git push -u origin main Missing or invalid credentials. Error: connect ECONNREFUSED /run/user/1000/vscode-git-b4fd3ae6aa.sock at PipeConnectWrap.afterConnect [as oncomplete] (node:net:1637:16) { errno: -111, code: 'ECONNREFUSED', syscall: 'connect', address: '/run/user/1000/vscode-git-b4fd3ae6aa.sock' } Missing or invalid credentials. Error: connect ECONNREFUSED /run/user/1000/vscode-git-b4fd3ae6aa.sock at PipeConnectWrap.afterConnect [as oncomplete] (node:net:1637:16) { errno: -111, code: 'ECONNREFUSED', syscall: 'connect', address: '/run/user/1000/vscode-git-b4fd3ae6aa.sock' } remote: No anonymous write access. fatal: Authentication failed for 'https://github.com/Tarun-Kumar-Arcot/quarkus-gitops.git/' manager@cka-manager:~/quarkus-gitops$ mkdir -p .github/workflows manager@cka-manager:~/quarkus-gitops$
```
### Root Cause

1. Git tried to use VS Code’s Git credential helper

    * That socket (vscode-git-*.sock) exists only when VS Code is running

    * You’re on an Ubuntu server → VS Code isn’t running → socket refused

2. GitHub does NOT allow username/password pushes anymore

    * Password-based Git pushes were deprecated

    * You must use a Personal Access Token (PAT) or SSH

### Fix

1. Create a GitHub Personal Access Token (once)

    On GitHub (browser):

    * Go to
      GitHub → Settings → Developer settings → Personal access tokens

    * Choose Fine-grained token (or classic is OK)

    * Permissions required:

        - ✅ Repository access (read + write)

    * Generate token

    * Copy it (you won’t see it again)
2. Remove VS Code Git helper (important)

  ```md
  git config --system --get credential.helper
  git config --global --get credential.helper
  git config --local --get credential.helper
  ```
3. Kill VS Code Git env vars (THIS IS THE KEY)

  ```md
  env | grep -i vscode
  VSCODE_GIT_ASKPASS=...
  GIT_ASKPASS=...
  unset GIT_ASKPASS
  unset VSCODE_GIT_ASKPASS
  unset SSH_ASKPASS
  ```
4. Force Git to ask for credentials (no helpers)

  ```md
  GIT_ASKPASS= git push -u origin main
  Username for 'https://github.com': 
  Password for 'https://Tarun-Kumar-Arcot@github.com':
  ```

## 3. Docker Push Failed – Unauthorized / Username & Password Required


### ❌ Problem

```
unauthorized: incorrect username or password
```

### 🔍 Root Cause

* DockerHub credentials were **missing or incorrect** in GitHub Secrets
* Using Docker password instead of **Docker Access Token**

### ✅ Fix

1. Created DockerHub Access Token
2. Added GitHub secrets:

   * `DOCKERHUB_USERNAME`
   * `DOCKERHUB_TOKEN`
3. Updated workflow:

   ```yaml
   - name: Login to Docker Hub
     uses: docker/login-action@v3
     with:
       username: ${{ secrets.DOCKERHUB_USERNAME }}
       password: ${{ secrets.DOCKERHUB_TOKEN }}
   ```

📌 **Learning**: Always use tokens, not passwords, in CI.

---

## 4. ArgoCD Application Stuck in `Unknown` / `OutOfSync` / `Missing`

### ❌ Problem

```
SYNC STATUS: OutOfSync
HEALTH: Missing
No resources found in namespace
```

### 🔍 Root Causes (Multiple)

1. Wrong `repoURL` (still pointing to `hello-gitops`)
2. ArgoCD could not recurse into subfolders
3. Namespace handling was inconsistent

---

## 5. Wrong Repository URL in Application

### ❌ Problem

ArgoCD logs:

```
failed to list refs: repository not found
```

### 🔍 Root Cause

* Application YAML still used:

  ```
  https://github.com/<youruser>/hello-gitops
  ```

### ✅ Fix

Updated `argocd/application.yaml`:

```yaml
source:
  repoURL: https://github.com/Tarun-Kumar-Arcot/quarkus-gitops
```

📌 **Learning**: ArgoCD does NOT auto‑update repo URLs.

---

## 6. ArgoCD Not Reading Nested `k8s/` Folders

### ❌ Problem

* Repo structure:

  ```
  k8s/
    app/
    redis/
  ```
* ArgoCD showed **no manifests applied**

### 🔍 Root Cause

* ArgoCD only reads top‑level files unless recursion is enabled

### ✅ Fix

Enabled directory recursion:

```yaml
source:
  path: k8s
  directory:
    recurse: true
```

📌 **Learning**: Always enable recursion for structured manifests.

---

## 7. Namespace Confusion (Hardcoded vs ArgoCD Managed)

### ❌ Problem

* Resources were never created
* `kubectl get all -n quarkus-gitops` returned nothing

### 🔍 Root Cause

* Mixed usage of:

  * Hardcoded `namespace:` in manifests
  * ArgoCD `destination.namespace`

### ✅ Final Correct Approach

* **Let ArgoCD manage the namespace**
* Removed `namespace:` from ALL manifests
* Used only:

```yaml
destination:
  namespace: quarkus-gitops
```

📌 **Learning**: Never hardcode namespaces in GitOps unless necessary.

---

## 8. Application Needed Re‑Creation

### ❌ Problem

ArgoCD stuck despite fixes
```md
manager@cka-manager:~/quarkus-gitops$ kubectl get applications -n argocd 
NAME SYNC STATUS HEALTH STATUS 
hello-gitops Synced Healthy 
quarkus-gitops OutOfSync Missing
```

### ✅ Fix

```bash
kubectl delete application quarkus-gitops -n argocd
kubectl apply -f argocd/application.yaml
kubectl annotate application quarkus-gitops -n argocd argocd.argoproj.io/refresh=hard --overwrite
```

---

## 9. Final State – Everything Working 🎉

### ✅ ArgoCD

```
SYNC STATUS: Synced
HEALTH: Healthy
```

### ✅ Kubernetes Resources

* Redis Deployment & Service
* Quarkus Deployment (2 replicas)
* Health probes passing

### ✅ Verification

```bash
kubectl get all -n quarkus-gitops
kubectl port-forward svc/redis-demo -n quarkus-gitops 8081:8080
```

Browser:

```
http://localhost:8081/q/health
```

Returns Redis `PONG`

---

## 🧠 What Actually Fixed It (Final Timeline)

1. Fixed `repoURL`
2. Enabled `directory.recurse: true`
3. Added the following solve the issue:-
```md
syncOptions:
      - CreateNamespace=true
```
4. Recreated ArgoCD Application:-
```md
kubectl annotate application quarkus-gitops \
  -n argocd argocd.argoproj.io/refresh=hard --overwrite
```
5. Forced refresh

This is the **minimal correct GitOps setup**.

---

## 📌 Interview‑Ready Summary

> “I debugged multiple real‑world GitOps issues including ArgoCD sync failures, repository misconfiguration, directory recursion, namespace conflicts, and CI authentication problems, and resolved them systematically.”

---

✅ End of document
