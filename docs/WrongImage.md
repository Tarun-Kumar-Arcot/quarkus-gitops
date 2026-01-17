### Add wrong image in k8s/app/deployment.yaml

The application cr is in degraded state:-

~~~
manager@cka-manager:~/quarkus-gitops$ kubectl get applications -n argocd 
NAME             SYNC STATUS   HEALTH STATUS
hello-gitops     Synced        Progressing
quarkus-gitops   Synced        Degraded
~~~

Also the pods keep going into `Crashloopback state`

~~~
manager@cka-manager:~$ kubectl get pods -n quarkus-gitops 
NAME                          READY   STATUS              RESTARTS       AGE
redis-7b986b9f57-8rdvn        1/1     Running             2              11d
redis-demo-646cbdc777-sxgsc   0/1     ImagePullBackOff    0              17m
redis-demo-856f78fd4-q774t    0/1     ContainerCreating   25             11d
redis-demo-856f78fd4-q8d9p    0/1     CrashLoopBackOff    42 (60s ago)   11d
manager@cka-manager:~$ kubectl describe pods redis-demo-646cbdc777-sxgsc
Error from server (NotFound): pods "redis-demo-646cbdc777-sxgsc" not found
manager@cka-manager:~$ kubectl get pods -n quarkus-gitops 
NAME                          READY   STATUS             RESTARTS       AGE
redis-7b986b9f57-8rdvn        1/1     Running            2              11d
redis-demo-646cbdc777-sxgsc   0/1     ImagePullBackOff   0              18m
redis-demo-856f78fd4-q774t    0/1     Running            26 (24s ago)   11d
redis-demo-856f78fd4-q8d9p    0/1     Running            43 (2m ago)    11d
~~~

To rectify thiss change the image name:-

~~~
spec:
      containers:
      - name: app
        image: docker.io/tarunarcot/redis-demo:2.0 # Change 2.0 to 1.0
~~~