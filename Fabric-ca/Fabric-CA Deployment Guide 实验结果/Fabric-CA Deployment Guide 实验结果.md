## CA部署

实验来源：[CA Deployment steps](<https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/cadeploy.html#>)

### 文件结构

> fabric-ca-client存放的是所有客户端，包括中间msp、组织msp、boot msp、
>
> 其它带server，是各个部署的CA服务，包括boot (TLS)ca 服务 、org ca 服务、intermediate ca服务

![1589618757646](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric_CA\Fabric-CA Deployment Guide 实验结果.assets\1589618757646.png)

### fabric-ca-client

> 这个文件夹存的是生成的MSP

![1589618516415](Fabric-CA Deployment Guide 实验结果.assets\1589618516415.png)

boot/tls ca 服务

![1589618602020](Fabric-CA Deployment Guide 实验结果.assets\1589618602020.png)

中间ca服务 由boot ca 颁发的

![1589618616999](Fabric-CA Deployment Guide 实验结果.assets\1589618616999.png)

Org ca 服务，由boot ca 颁发的

![1589618634731](Fabric-CA Deployment Guide 实验结果.assets\1589618634731.png)



