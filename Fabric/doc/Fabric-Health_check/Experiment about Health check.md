## Health check

实践：在orderer节点的yaml配置文件中设置，RESTful API 的地址和端口

```yaml
# Prometheus
  - ORDERER_OPERATIONS_LISTENADDRESS=0.0.0.0:8008   # operation RESTful API
  - ORDERER_METRICS_PROVIDER=prometheus             # prometheus will pull metrics from orderer via /metrics RESTful API

# StatsD
  - ORDERER_METRICS_PROVIDER=statsd
  - ORDERER_METRICS_STATSD_PREFIX=orderer  # PREFIX来进行区分节点。
  - ORDERER_METRICS_STATSD_ADDRESS=192.168.5.10:8008 # StatsD的地址和端口


```

通过浏览器访问，192.168.5.10:8088/healthz

或者通过curl获取，curl http://orderer.example.com:8088/healthz



Prometheus与StatsD监控参考：

[Hyperledger Fabric 1.4 特性调研之Operations Service](https://www.jianshu.com/p/6cf812a9dc50)