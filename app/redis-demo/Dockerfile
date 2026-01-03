FROM registry.access.redhat.com/ubi8/openjdk-17-runtime
COPY target/quarkus-app /deployments/
CMD ["java","-jar","/deployments/quarkus-run.jar"]
