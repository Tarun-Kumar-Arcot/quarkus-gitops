# 🚀 Quarkus GitOps with Argo CD & Redis

A production-style **GitOps demonstration project** using **Quarkus**, **Redis**, **Docker**, **GitHub Actions**, **Kubernetes**, and **Argo CD**.

This repository showcases how to:

* Build and containerize a Quarkus application
* Push images automatically using GitHub Actions
* Deploy applications declaratively using Argo CD (GitOps)
* Run a stateful dependency (Redis) alongside the app
* Troubleshoot real-world GitOps and Argo CD issues

---

## 📌 What This Project Demonstrates

✅ **GitOps workflow with Argo CD**
✅ **CI pipeline using GitHub Actions**
✅ **Quarkus health checks & Redis integration**
✅ **Declarative Kubernetes deployments**
✅ **Hands-on troubleshooting & recovery scenarios**

This is **not a toy example** — the repo includes **real issues faced and fixed**, exactly the kind you encounter in production environments.

---

## 🧩 Architecture Overview

```
Developer → Git Push
              ↓
       GitHub Actions (CI)
              ↓
       Docker Image (Docker Hub)
              ↓
           Argo CD
              ↓
      Kubernetes Cluster
        ├── Quarkus App (2 replicas)
        └── Redis (1 replica)
```

---

## 📁 Repository Structure

```text
quarkus-gitops/
├── app/                     # Quarkus application source
│   └── redis-demo/
├── k8s/                     # Kubernetes manifests (GitOps source)
│   ├── app/
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── redis/
│       ├── deployment.yaml
│       └── service.yaml
├── argocd/
│   └── application.yaml     # Argo CD Application definition
├── docs/
│   └── GitOps-Troubleshooting.md
├── .github/workflows/
│   └── ci-build-push.yaml   # GitHub Actions pipeline
└── README.md
```

---

## ⚙️ CI Pipeline (GitHub Actions)

The CI pipeline performs:

* Checkout source code
* Build Quarkus app using Maven
* Build Docker image
* Push image to Docker Hub

**Triggered on:**

```yaml
on:
  push:
    branches:
      - main
    paths:
      - app/**
```

---

## 🚢 CD with Argo CD (GitOps)

Argo CD continuously syncs Kubernetes manifests from this repository:

```yaml
spec:
  source:
    repoURL: https://github.com/Tarun-Kumar-Arcot/quarkus-gitops
    path: k8s
    targetRevision: main
  destination:
    namespace: quarkus-gitops
```

* **Automated sync**
* **Self-healing**
* **Pruning enabled**

---

## ☸️ Kubernetes Deployment Details

### Quarkus Application

* **Replicas:** 2
* **Health endpoints:**

  * `/q/health/ready`
  * `/q/health/live`
* **Service:** ClusterIP (internal)

### Redis

* **Replicas:** 1
* **Service:** ClusterIP
* Used by Quarkus via in-cluster DNS

---

## 🧠 Health Checks (Production-grade)

Already implemented in `k8s/app/deployment.yaml`:

```yaml
readinessProbe:
  httpGet:
    path: /q/health/ready
    port: 8080

livenessProbe:
  httpGet:
    path: /q/health/live
    port: 8080
```

✔ Ensures zero traffic to unhealthy pods
✔ Prevents crash loops from impacting service

---

## 📦 Resource Limits (Recommended)

**Add this to the Quarkus container** for production realism:

```yaml
resources:
  requests:
    cpu: "100m"
    memory: "256Mi"
  limits:
    cpu: "500m"
    memory: "512Mi"
```

This demonstrates:

* Capacity planning
* Scheduler awareness
* Production hygiene

---

## 🌐 Accessing the Application

### Port-forward (local access)

```bash
kubectl port-forward svc/redis-demo -n quarkus-gitops 8081:8080
```

### Verify health

```bash
curl http://localhost:8081/q/health
```

Expected output:

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Redis connection health check",
      "status": "UP",
      "data": {
        "default": "PONG"
      }
    }
  ]
}
```

---

## 🔐 Accessing Argo CD UI

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Username:

```
admin
```

Password:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d
```

---

## 🧯 Troubleshooting & Real Issues Faced

All **real GitOps problems encountered and fixed** are documented here:

📄 **`docs/GitOps-Troubleshooting.md`**

Includes:

* `OutOfSync: Missing` root cause
* Wrong `repoURL` in Application
* Namespace mismanagement
* Argo CD cache refresh issues
* GitHub Actions auth failures
* Docker Hub permission errors

👉 These are **excellent interview discussion points**.

---