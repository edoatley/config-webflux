# Steps to install mongodb on k8s

```shell
git clone https://github.com/mongodb/mongodb-kubernetes-operator.git
cd mongodb-kubernetes-operator
kubectl create -f deploy/crds/mongodb.com_mongodb_crd.yaml
kubectl create -f deploy/operator/ --namespace mongodb
```

