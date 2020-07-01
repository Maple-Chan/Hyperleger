# 新的测试样例——test-network



### Fabric 2.0 部分指令

> 基于test-network网络，clone fabric-sample ，进入test-work文件夹进行测试。

#### 目录

[test-network使用](#test-network使用)

[通道创建和加入](#通道创建和加入)

[新的链码管理](#新的链码管理)

[隐私数据](#隐私数据)

<br>
<br>

<span id="test-network使用"> </span>

##### test-network使用

> Fabric的流程：生成证书、创世块，启动网络，创建创建通道，安装链码。

###### 生成证书

```bash
cryptogen generate --config=./organizations/cryptogen/crypto-config-org1.yaml --output="organizations"  
# 使用crypton生成证书,也可以用CA生成。
```

生成区块

```bash
configtxgen -profile TwoOrgsOrdererGenesis -channelID system-channel -outputBlock ./system-genesis-block/genesis.block 
# 配置文件写在configtx.yaml
```



###### 网络启动

```bash
./network.sh up  # 启动test网络，默认使用leveldb状态数据库'-s couchdb'指定couchdb，cryptogen生成证书。
```

不用脚本就通过docker-compose启动各个容器。

###### 创建channel

```bash
./network.sh createChannel # 创建默认通道 mychannel。 使用 -c channelname 进行指定
# createChannel.sh脚本
```

使用[创建通道脚本](#createChannel)

###### 安装链码

```bash
./network.sh deployCC # 安装智能合约，使用deployCC.sh脚本
```

使用[链码部署脚本进行安装](#deployCC)

---

<br>
<br>

<span id="通道创建和加入"> </span>

##### 通道创建和加入

###### 创建channel

> 创建channel脚本 scripts/createChannel.sh

###### 创建通道tx文件

```bash
configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/${CHANNEL_NAME}.tx -channelID $CHANNEL_NAME
# configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/sucChannel.tx -channelID sucChannel

# configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/succhannel.tx -channelID succhannel

# 创建suc_channel.tx，通道的tx文件
```

```bash
configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/${orgmsp}anchors.tx -channelID $CHANNEL_NAME -asOrg ${orgmsp}
# 创建锚节点的tx文件
```

###### 创建通道

```bash

peer channel create -o localhost:7050 -c $CHANNEL_NAME --ordererTLSHostnameOverride orderer.example.com -f ./channel-artifacts/${CHANNEL_NAME}.tx --outputBlock ./channel-artifacts/${CHANNEL_NAME}.block --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA
# 创建channel，使用CA证书

peer channel create -o orderer.example.com:7050 -c succhannel --ordererTLSHostnameOverride orderer.example.com -f ./channel-artifacts/succhannel.tx --outputBlock ./channel-artifacts/succhannel.block --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem >&createChannel.log


# 


peer channel create -o localhost:7050 -c mychannel -f ./channel-artifacts/mychannel.tx --outputBlock ./channel-artifacts/mychannel.block
# 创建channel，使用TLS
```



###### 加入通道

```bash
peer channel join -b ./channel-artifacts/succhannel.block >&log.txt
# 需要先设置环境变量在进行加入通道
```

###### 更新锚节点

```bash
peer channel update -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c $CHANNEL_NAME -f ./channel-artifacts/${CORE_PEER_LOCALMSPID}anchors.tx >&log.txt
# 更新锚节点，使用TLS

peer channel update -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c $CHANNEL_NAME -f ./channel-artifacts/${CORE_PEER_LOCALMSPID}anchors.tx --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA
# 更新锚节点，使用CA证书
```

---

<br>
<br>

<span id="新的链码管理"> </span>

##### 新的链码管理

> 提出lifecycle指令对链码的部署进行管理

**多个组织必须审批链码通过后，才能进行链码提交到帐本中**

链码提交到账本的过程：打包、安装、审批、提交。（升级过程和安装启动过程一样）



**<a name="deployCC">链码部署</a>**

> 一键打包脚本 scripts/deployCC.sh

###### **打包**

```bash
peer lifecycle chaincode package fabcar.tar.gz --path ../../fabric-samples/chaincode/fabcar/go/ --lang golang --label fabcar_1 >&package.log
#peer lifecycle chaincode package fabcar.tar.gz --path ${CC_SRC_PATH} --lang ${CC_RUNTIME_LANGUAGE} --label fabcar_${VERSION} >&log.txt

# 链码打包，将合约打包在了xxx.tar.gz文件中。
```

> ```shell
> # 出failed to normalize chaincode path: 'go list' 错误的解决办法
> go env -w GO111MODULE=on
> go env -w GOPROXY=https://goproxy.cn,direct
> ```

###### **安装**

```bash
peer lifecycle chaincode install fabcar.tar.gz >&log.txt
# 链码安装
```

###### 审批

审批—配置链码定义

> 定义包括名字，版本，包背书策略，签名策略，隐私数据配置等。

```bash
peer lifecycle chaincode approveformyorg -o orderer.example.com:7050 --channelID $CHANNEL_NAME --name fabcar --version ${VERSION} --init-required --package-id ${PACKAGE_ID} --sequence ${VERSION} --waitForEvent >&log.txt
# 2.0的指令，用于某个组织定义链码审批机制：
--signature-policy "OR('Org1MSP.member','Org2MSP.member')" # 签名策略
--channel-config-policy "OR('Org1MSP.member','Org2MSP.member')"   # 链码背书策略

/*
export ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051

peer lifecycle chaincode approveformyorg -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --peerAddresses peer0.org1.example.com:7051 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/tlscacerts/tls-localhost-7054-ca-org1.pem --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/fabric-ca/org1/tls-cert.pem --channelID succhannel --name fabcartest1 --version 1 --init-required --package-id fabcar_1:aca5cf806d53b8860e3bc4234af8fe9a017e7082d98108e07ffc7ea36dbc2e07 --sequence 1 --signature-policy "OR('Org1MSP.member','Org2MSP.member')" 

peer lifecycle chaincode approveformyorg -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/fabric-ca/org1/tls-cert.pem --channelID succhannel --name fabcartest1 --version 1 --init-required --package-id fabcar_1:aca5cf806d53b8860e3bc4234af8fe9a017e7082d98108e07ffc7ea36dbc2e07 --sequence 1 --signature-policy "OR('Org1MSP.member','Org2MSP.member')" 
#（二选一）--channel-config-policy Channel/Application/Admins  >&log.txt


peer lifecycle chaincode approveformyorg -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem  --channelID succhannel --name fabcar --version 1 --init-required --package-id fabcar_1:aca5cf806d53b8860e3bc4234af8fe9a017e7082d98108e07ffc7ea36dbc2e07 --sequence 1 --channel-config-policy Channel/Application/Admins >&log.txt
*/


export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=peer0.org2.example.com:9051
```

审批—检查能否提交

```bash
peer lifecycle chaincode checkcommitreadiness --channelID succhannel --name fabcar --version 1 --sequence 1 --output json --init-required #>&log.txt
# 2.0的指令，检查指定的链码是否可以向通道提交。
```

###### **提交**

```bash
peer lifecycle chaincode commit -o localhost:7050 --channelID mychannel1 --name fabcar $PEER_CONN_PARMS --version 1.0 --sequence 1 --init-required #>&log.txt
# 2.0的指令，用于提交链码定义

peer lifecycle chaincode commit -o orderer.example.com:7050 --channelID mychannel1 --name fabcar --version 1.0 --sequence 1 --init-required --tls  --cafile $ORDERER_CA --peerAddresses peer0.org1.example.com:7051

peer lifecycle chaincode commit -o orderer.example.com:7050 --ordererTLSHostnameOverride orderer.example.com --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --channelID mychannel1 --name fabcar1 --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt --version 1.0 --sequence 1 --init-required

```

<br>
<br>

<span id="隐私数据"> </span>



##### 隐私数据

隐私数据集合定义：

>一个集合定义包含了一个或者多个集合，每个集合具有一个策略列出了在集合中的所有组织，还包括用来控制在背书阶段私有数据的传播所使用的属性，另外还有一个可选项来决定数据是否会被删除。

在2.0版本，隐私数据集合将由channel成员进行定义审批条件，**当链码定义被提交到通道时部署隐私数据集合。**

```bash
--collections-config 
# 在实例化，或者peer lifecycle chaincode approveformyorg 用这个标志来指定隐私数据集合配置文件路径
```

配置文件内容示意：

```json
[
 {
    "name": "collectionMarblePrivateDetails",
    "policy": "OR('Org1MSP.member')", // 私有数据集合分发策略
    "requiredPeerCount": 0, //在节点为背书签名并将提案响应返回给客户端前，每个背书节点必须将私有数据分发到的节点(在被授权的组织当中)的最小数量。0 表示不做分发要求
    "maxPeerCount": 3, //每个背书节点将会尝试将私有数据分发到的其他节点（在被授权的组织中) 的最大数量
    "blockToLive":3, //代表了数据应该以区块的形式在私有数据库中存在多久。
    "memberOnlyRead": true, //节点会自动强制只有属于这些集合的组织的客户端才可以读取私有数据。
    "memberOnlyWrite":true, //节点会自动强制只有属于这些集合的组织的客户端才可以写入私有数据。
    "endorsementPolicy": {//可选背书策略，用于覆盖链码级的背书策略
     "signaturePolicy": "OR('Org1MSP.member')"  
    }
 }
]
```

API中隐私数据的存取

```go
PutPrivateData(collection,key,value) 
GetPrivateData(collection,key)
```

