### Tighten the liveness probe

~~~
livenessProbe:
          httpGet:
            path: /q/health/live
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 2
          failureThreshold: 3
        resources:
          limits:
            memory: "300Mi"
            cpu: "256m"
          requests:
            memory: "128Mi"
            cpu: "100m"
~~~

The outcome of it is seen in the events:-

~~~
Events:
  Type     Reason     Age                    From               Message
  ----     ------     ----                   ----               -------
  Normal   Scheduled  7m                     default-scheduler  Successfully assigned quarkus-gitops/redis-demo-557dd4454c-k4xnh to cka-nodeone
  Normal   Pulled     6m58s                  kubelet            Container image "docker.io/tarunarcot/redis-demo:1.0" already present on machine
  Normal   Created    6m58s                  kubelet            Created container: app
  Normal   Started    6m58s                  kubelet            Started container app
  Warning  Unhealthy  6m55s (x3 over 6m57s)  kubelet            Readiness probe failed: Get "http://172.17.26.236:8080/q/health/ready": dial tcp 172.17.26.236:8080: connect: connection refused
  Warning  Unhealthy  6m44s                  kubelet            Readiness probe failed: Get "http://172.17.26.236:8080/q/health/ready": context deadline exceeded (Client.Timeout exceeded while awaiting headers)
manager@cka-manager:~/quarkus-gitops$ 
~~~
