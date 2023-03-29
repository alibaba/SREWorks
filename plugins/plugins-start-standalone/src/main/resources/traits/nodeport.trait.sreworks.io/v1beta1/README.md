```
name: nodeport.trait.sreworks.io/v1beta1
runtime: post
spec:
    nodePort: 30080
    backendPort: 8080
    selector:
      app: my-app
```