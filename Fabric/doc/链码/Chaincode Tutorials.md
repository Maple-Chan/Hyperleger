- [ ] BCCSP 加密库
- [ ] chaincode安装/绑定多个channel，会有几个实例
- [ ] chaincode调用另一个chaincode
- [ ] 实例化策略，多个身份打包签名，实例化签名
- [x] 背书策略

# Chaincode Tutorials



## 智能合约概述

​	智能合约是一种被广泛认可并使用的术语，在 `HyperLedger Fabric` 中被称为**链码**。
​	智能合约拥有自己的执行逻辑，在 `HyperLedger Fabric` 建立频道的特定网络中被采用为业务规则。这些业务主要是对数据进行**逻辑处理**，对数据的实际应用是各个组织的**自定义规则**，不建议将真实场景中的业务规则作为统一标准捆绑进智能合约。
​	智能合约（建议由 **Go** 编写）将会被一个授权的成员**安装并实例化到一台 Peer 节点服务上**，随后， **普通的业务人员**可以使用一个执行有 **Fabric-SDK 的客户端与 Peer 节点服务进行交互** ，从而实现对智能合约的**调用**。
​	智能合约在事物流程中进行运转，如果一旦被验证且验证的**结果集被发送至 Ordrer 排序服务**，那么其运行结果中的变化将被共享或**同步到 Fabric 网络中的所有 Peer 节点**，从而改变**World State**。
​	可以分别从智能合约**代码开发人员**和智能合约在 Fabric 网络中**实际操作人员**的角度来看待智能合约。通过**前者的视角**，**开发区块链应用程序**或解决方案名为智能合约，即如何通过 Go语言等进行编码从而实现整个智能合约编写的结果。通过**后者的视角**来看，区块链的智能合约是面向网络运维人员或运营商的，由一个负责管理整个区块链网络的人来执行相关操作，包括利用 `HyperLedger Fabric`的 API 来**安装、实例化和升级智能合约**，但它不会参与到智能合约的编码过程。

# Chaincode for Operators

## What is Chaincode?

​	`Chaincode` 智能合约是一个程序，它是使用 `Go、node.js、 Java` 等其他编程语言中**实现了指定的接口**。`Chaincode`运行在与`endorsing peer process` **背书节点**隔离的安全**Docker容器**中。 通过应用程序**提交事务**来**Chaincode初始化**和**管理分类帐状态**。

​	`Chaincode`通常处理**被网络成员同意**的业务逻辑，因此它被认为是一种**“智能合同“**。在提案交易中可以`invoke`智能合约来`update or query` 账本。在给定**适当的许可**的情况下，`Chaincode`链代码可以在**相同或不同的通道**中**调用另一个**`Chaincode`链代码来访问其状态。注意，如果被调`Chaincode`不和主调`Chaincode`在同一`channel`只能执行`query`。也就是说，`TODO`<u>不同通道上的被调用链代码只是一个`Query`，它在后续提交阶段不参与状态验证检查。</u>

​	在以下部分中，我们将通过应用程序运维人员**operator**的角度探索链代码。链码**operator通过**此教程学会在network中如何用 `Fabric chainode lifecycle` 去 `deploy and manage chaincode`。

## Chaincode lifecycle

​	**Hyperledger Fabric的 API** 支持与区块链网络中的各个节点进行交互——**peers、orderers和MSPs**——允许`endorsing peer` **背书节点**上**打包**、**安装**、**实例化**和**升级**chaincode 。尽管Hyperledger Fabric 可以用来管理智能合约的生命周期，但它还是提供了特定语言的**SDK**抽象了Hyperledger Fabric API 的细节，以促进应用程序的开发。另外，可以通过 **CLI** 直接访问Hyperledger Fabric API 。
​	官方提供了四个命令来管理一个智能合约的生命周期：`package、install、instantiate、upgrade`。在未来的版本中，官方也正在考虑添加 `stop`和`start`命令操作事务来禁用和重新启用智能合约，实现**不用真正地卸载智能合约**。在成功安装并实例一个智能合约之后，智能合约就处于活跃中（正在运行〉 并且可以通过`invoke transaction`**调用事务**处理事务。在**安装完毕后**，也可以在任何时间对智能合约进行**升级**。

### Packaging （包）

`chaincode package`由3部分组成：

- 智能合约由规范 `ChaincodeDeploymentSpec`或 **CDS** 定义。**CDS** 是根据**代码和其他属性**（如名称和版本）定义的智能合约包。
- 一个**可选的实例化策略**。与**背书策略**[Endorsement policies](https://hyperledger-fabric.readthedocs.io/en/release-1.4/endorsement-policies.html)语法一致。 
- 由**“拥有” chaincode** 的实体的一组**签名**。

签名用于以下目的：

- 为了建立智能合约的**所有权**

- 允许对包的内容进行**验证**：

- 允许检测包**是否篡改**

  一个 Channel智能合约实例化事务的**创建者**，是通过智能合约的**实例化策略来验证**。


### Creating the package

​	有两种打包**ChainCode**方法。1.**ChainCode**拥有多个所有者时，需多个身份**Identity**签名。工作流程：首先创建一个签名的**链代码包（SignedCDS）**，随后将其串行传递给**其他所有者**以进行**签名**。2.更简单的工作流程适用于部署，**仅具有发出安装事务的节点标识**的签名的`SignedCDS`。

​	首先，将处理**更复杂的情况**。但是，如果不需要考虑多个所有者情况，那么可以跳到下面的[Installing chaincode](https://hyperledger-fabric.readthedocs.io/en/release-1.4/chaincode4noah.html#install) 部分。
​	创建一个己签名的智能合约包：

```shell
peer chaincode package 
-n mycc 
-p github.com/hyperledger/fabric/examples/chaincode/go/chaincode_exampleθ2 
-v 0 
-s -S 
-i "AND('OrgA.admin')" ccpack.out
```

​	`-s` 参数是指可以创建一个由**多个所有者签署**的包，而不是简单地创建原始CDS。。当指定了 `-s` 时，如果其他所有者需要签名 ，也必须指定 `-s` 参数。否则，这个进程会创建一个除了CDS 实例化策略之外的己签署CDS。
​	`-S` 参数使用在 `core.yaml`中由 `LocalMspid` 属性值标志的 `MSP` 来指示该程序的签名。`-S` 参数是可选的。但是，如果一个包是在**没有签名的情况下创建**的，那么它就**不能由任何其他所有者**使用 `signpackage` 命令来签署。
​	`-i` 参数是可选的，即指定智能合约**实例化策略**。实例化策略与背书策略具有相同的格式，并指定哪些 ID 可以实例化智能合约。在上面的示例中，只允许使用 OrgA 的 admin 实例化链代码。如果没有提供策略，则使用**默认策略**，这将只允许 peer 中 **MSP 的 admin 身份**来实例化智能合约。

### Package signing

​	在创建时**被签名过**的 `chaincode package` 可以移交给**其他所有者**进行**检查和签名**。该工作流程支持对链代码包进行**带外签名**。

[	ChaincodeDeploymentSpec](https://github.com/hyperledger/fabric/blob/master/protos/peer/chaincode.proto#L78)可以选择由集体所有者签名，以创建[SignedChaincodeDeploymentSpec](https://github.com/hyperledger/fabric/blob/master/protos/peer/signed_cc_dep_spec.proto#L26)（或**SignedCDS**）。**SignedCDS** 包含了3个元素：

1. CDS 包含了智能合约的**源码、名称和版本**信息
2. 智能合约的**实例化策略**，即表示**背书策略**
3. 智能合约的**所有者列表**，以[Endorsement](https://github.com/hyperledger/fabric/blob/master/protos/peer/proposal_response.proto#L111)背书的方式定义

>
> 注意： 当智能合约在某些 Channel 上实例化时 ，此背书策略是由传输层协议使用的带外数据（ Out Of Band, OOB ）决定的 ，以提供适当 MSP 原则。**如果没有指定实例化策略，则默认策略是 Channel 的任何 MSP 的 admin**
>

​	每个所有者通过将 其与所有者身份（ 例如证书） 相结合 ，并签署结合后结果来为ChaincodeeDeploymentSpec 背书。
​	一个智能合约所有者可以使用下面的命令来签署一个以前创建的签名包：

```shell
peer chaincode signpackage ccpack.out signedccpack.out 
```

​	`ccpack.out` 和 `signedccpack.out` 分别是输入包和输出包。`signedccpack.out` 包含了使用**本地 MSP** 签名的包的附加签名。

### Installing chaincode

​	 `install transaction`将**chaincode**的源代码打包成一种指定的格式，称为 **ChaincodeDeploymentSpec或CDS**。将**chaincode**安装到 **Peer** 节点上，并运行**ChainCode**。

> 注意： 必须 在Channel 中的**每个背书节点**上安装智能合约，以运行智能合约。

​	<u>当安装 API 被简单地给出一个ChaincodeDeploymentSpec 时，它将默认实例化策略，并包含一个空的所有者列表。</u>

> 注意： chaincode只能在chaincode**拥有者**成员的**背书 Peer 节点**上安装，以保护网络中其他成员的智能合约逻辑的机密性。那些没有智能合约的成员，不能成为智能合约交易的背书人，也就是说，他们<u>不能执行智能合约。但是，他们仍然可以验证并将事务提交到账本。</u>

​	安装一个**chaincode**，需要将一个[SignedProposal](https://github.com/hyperledger/fabric/blob/master/protos/peer/proposal.proto#L104)发送到 `lifecycle system chaincode` (LSCC)  。例如，使用 **CLI** 安装简单资产智能合约中的 **sacc** 示例智能合约，该命令如下所示：

```shell
peer chaincode install -n asset_mgmt -v 1.0 -p sacc 
```

​	CLI 容器内执行，创建`SCD sacc`，并将其发送给**本地Peer** ，**本地Peer** 会调用 **LSCC** 上的`insatll`方法。 `-p` 选项指定**ChainCode路径**，它必须位于用户的 **GOPATH** 的源码树中，例如`$GOPATH/src/sacc` 。有关命令选项的完整描述，后面将会讲到。

​	注意，为了在Peer上安装，**SignedProposal**的**签名**必须是Peer本地 **MSP**的**管理员**之一。

### Instantiate

​	`instantiate transaction`调用 `lifecycle System Chaincode`(LSCC) 来**创建和初始化Channel**上的**ChainCode**。<u>这是一个 `Chaincode-Channel` 绑定过程：ChainCode可以绑定到**任意数量**的 Channel，并分别在每个Channel 上**独立操作**。</u>换句话说，不管智能合约安装和实例化了多少个其他 Channel，<u>状态都与提交事务的通道保持隔离。</u>
​	`instantiate transaction`的**创建者**必须满足在 **SignedCDS** 中包含的智能合约的**实例化策略**，也必须是 Channel 上的一个**写入者**，并且该创建者作为**创建该 Channel 配置信息**的一部分。这对于Channel 的安全性来说是非常重要的，它可以防止恶意实体部署智能合约或欺骗成员在一个未绑定的 Channel 上执行智能合约
​	例如，**默认**的实例化**策略**是任何 Channel 上的 **MSP 管理员**，因此一个智能合约实例化事务的**创建者**必须是 **Channel 管理员的成员**。当事务提案到达背书人（节点〉的时候，它将**验证创建者的签名与实例化策略**。井且在将其提交给账本之前，在事务验证期间再次执行此操作。

​	`instantiate transaction`还为 Channel 上的智能合约**设置了背书策略**。**背书策略**描述了交易结果的**认证要求**，被该 Channel 的所有成员所接受。
​	例如，使用 CLI 实例化 `sacc` 智能合约，井使用 `john 和 0` **初始化状态**，命令将如下所示

```shell
peer chaincode instantiate 
-n sacc -v 1.0 
-c '{"Args":["john","0"]' 
-P "AND ('Org1.member','Org2.member')"
```

> 注意： 背书策略（ CLI 使用波兰表示法），它需要来自Org1 和 Org2 的成员的支持，以支持所有的事务到 sacc。 也就是说，无论是 Org1 或 Org2 都必须签署在 sacc 上执行调用的结果，以使事务是有效的。

​	成功实例化后，**智能合约**在 **Channel** 上进入**活动状态**，并准备处理`ENDORSER_TRANSACTION`类型的任何**transaction proposals** 。当 **transactions** 到达了**背书 Peer**， **transactions** 被**并发处理**。

### Upgrade

​	**ChainCode**通过更改**Version 版本**来进行**升级**，这是 **SignedCDS** 的一部分。其他部分，例如`owners` **所有者**和`instantiation policy`**实例化策略**是**可选**的。但是，**名称**必须是相同的，否则它将被视为完全不同的智能合约。
​	在升级之前，**新版本**的智能合约在要求的背书 Peer 上需要**先安装**。升级是一个类似于**实例化事务**的事务，它将智能合约的新版本**绑定**到 Channel。**其他 Channels 所绑定的旧版本智能合约将会继续运行旧版本智能合约。**换句话说，升级事务只会**影响对应的 Channel** ，即提交事务的 Channel。

> 注意：由于智能合约的**多个版本**可能同时处于活跃状态 ，所以**升级不会删除旧版本**，因此用户必须暂时管理这个版本。

​	与实例化事务有一个微妙的区别：升级事务是根据当前的智能合约实例化策略检查的，而**不是新策略**（如果指定的话）。这是为了确保在当前的实例化策略中指定的现有成员可以升级智能合约。

> 注意：在升级过程中，调用 `chaincode Init` 函数来执行任何与数据相关的更新或重新初始化它，因此在**升级**智能合约时必须注意**避免重新设置状态**。

### Stop and Start

​	**停止和启动**智能合约的生命周期事务**还没有实现**。但是，可以通过从每个背书人中删除智能合约容器和 SignedCDS 包来手动停止智能合约。这是通过在背书 Peer 节点运行的每个主机或虚拟机上删除智能合约的容器来完成的，然后从每个背书 Peer 节点上删除 SignedCDS。

> 注意：官方 TODO-为了 从 Peer 节点删除 CDS 首先需要进入 Peer 节点的容器。暂时并没有提供一个能够执行此功能的实用程序脚本。

```shell
docker rm -f <container id>
rm /var/hyperledger/production/chaincodes/<ccname>:<ccversion>
```

Stop 在工作流程中是有用的，可以在控制方式上进行升级。在进行升级之前，可以在所有Peer 上停止一个智能合约。

## System chaincode

​	**系统智能合约**具有相同的编程模型，除了它在 **Peer 进程**中运行，而不是像普通的智能合约那样在一个**单独的容器**中运行 。 因此，系统智能合约被构建到 **Peer 的可执行文件**中，并且**不遵循**上面描述的相同的**生命周期** 。 特别是**安装、实例化和升级**并不适用于系统智能合约。
​	系统智能合约是为了在 **Peer 和 智能合约**之间减少 gRPC 的通信成本，并权衡管理的灵活性 。 例如，系统智能合约只能用 Peer 二进制进行升级。它还必须注册一个固定的参数集  [fixed set of parameters](https://github.com/hyperledger/fabric/blob/master/core/scc/importsysccs.go) ，并且没有背书策略或背书策略功能 。
​	系统智能合约用于 Hyperledger Fabric 以实现许多**系统行为**，使它们可以被系统集成商所取代或修改。

​	修改和删除系统合约时要特别注意。当前的系统智能合约列表 ：

- `LSCC (Lifecycle System Chaincode)`：处理上面描述的**生命周期请求** 。

- `CSCC (Configuration System Chaincode)`：在 Peer 端处理 **Channel配置** 。

- `QSCC (Query System Chaincode)` ： 提供了 **账本查询 API** ，例如获取块和事务。

  之前背书和验证链码已经被可插拔的背书和验证函数取代，可查看 [Pluggable transaction endorsement and validation](https://hyperledger-fabric.readthedocs.io/en/release-1.4/pluggable_endorsement_and_validation.html) 

-  `ESCC (Endorsement System Chaincode)`背书系统智能合约通过签署事务提案响应来处理支持。

- `VSCC (Validation System Chaincode)`：验证系统智能合约处理事务验证，包括检查

# Chaincode for Developers

## What is Chaincode?

​	`Chaincode` 智能合约是一个程序，它是使用 `Go、node.js、 Java` 等其他编程语言中**实现了指定的接口**。`Chaincode`运行在与`endorsing peer process` **背书节点**隔离的安全**Docker容器**中。 通过应用程序**提交事务**来**Chaincode初始化**和**管理分类帐状态**。

​	`Chaincode`通常处理**被网络成员同意**的业务逻辑，因此它被认为是一种**“智能合同“**。在提案交易中可以`invoke`智能合约来`update or query` 账本。在给定**适当的许可**的情况下，`Chaincode`链代码可以在**相同或不同的通道**中调用另一个`Chaincode`链代码来访问其状态。注意，如果被调`Chaincode`不和主调`Chaincode`在同一`channel`只能执行`query`。也就是说，`TODO`<u>不同通道上的被调用链代码只是一个`Query`，它在后续提交阶段不参与状态验证检查。</u>

​	在以下部分中，我们将通过应用程序开发人员**developer**的角度探索链代码。我们将提供一个简单的链代码示例**sample**应用程序，并介绍`Chaincode Shim API`中每个方法的用途。

## Chaincode API

​	每个chaincode程序都必须实现`Chaincode接口`，该接口的**方法被调用**来响应接收的**事务**。您可以在下面找到适用于不同语言的`Chaincode Shim API`的参考文档：

> - [Go](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#Chaincode)
> - [node.js](https://fabric-shim.github.io/ChaincodeInterface.html)
> - [Java](https://fabric-chaincode-java.github.io/org/hyperledger/fabric/shim/Chaincode.html)

​	在每种语言中，**Clients**调用`Invoke`方法来**提交事务提案**。此方法允许您使用`Chaincode`链代码在`channel ledger 通道分类帐`上**读取和写入**数据。

​	需要包含一个`Init`方法，该方法将用作链代码的初始化函数。将调用此方法，以便在`start or upgrade` 启动或升级时**初始化链代码**。默认情况下，`Init`方法永远不会执行。但是，<u>`TODO`您可以使用`chaincode definition`链代码定义来请求执行`Init`函数。如果请求执行`Init`，</u>则fabric将确保在任何**其他函数之前调用Init**，并且**仅调用一次**。此选项使您可以进一步**控制哪些用户**可以**初始化链码**以及将**初始数据**添加到分类帐的功能。如果使用`peer CLI`批准链代码定义，请使用`--init-required`标志来请求执行`Init`函数。然后使用`peer chaincode invoke`命令调用`Init`函数并传递`--isInit`标志。如果您使用`Fabric SDK for Node.js`，请访问如何安装和启动您的链代码。有关更多信息，请参阅[Chaincode for Operators](https://fabric-sdk-node.github.io/master/tutorial-chaincode-lifecycle.html)。
​	智能合约`shim API` 的其他接口是`ChaincodeStublnterface`。

> - [Go](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#ChaincodeStubInterface)
> - [node.js](https://fabric-shim.github.io/ChaincodeStub.html)
> - [Java](https://fabric-chaincode-java.github.io/org/hyperledger/fabric/shim/ChaincodeStub.html)

​	该接口方法用于**访问和修改网络账本**，并在智能合约之间进行调用。

​	在本教程中使用`Go chaincode`，我们将通过实现管理简单**“asset资产”**的简单链代码应用程序来演示这些API的使用。

## Simple Asset Chaincode

​	我们的应用程序是一个基本的示例链代码，用于在分类帐上创建**资产（键值对）**。

### Choosing a Location for the Code

​	如果还没有Go进行编程，您可能需要确保安装了 [Go Programming Language](https://hyperledger-fabric.readthedocs.io/en/release-1.4/prereqs.html#golang)并正确配置了系统。
​	现在，要为链代码应用程序创建一个目录，作为`$GOPATH/src/`的子目录。
​	使用以下命令：

```shell
mkdir -p $GOPATH/src/sacc && cd $GOPATH/src/sacc
```

​	创建代码填写的源文件：

```shell
touch sacc.go
```

### Housekeeping

​	首先需要准备好必备的 **Go 编程样板**，与所有的智能合约一样，它实现了**Chaincode**接口，特别是实现了**Init**和**Invoke**方法。因此，需要给智能合约添加 `import` 语句，以获得必要的**依赖项**。这里将导入 `chaincode shim`包和 `peer protobuf`。接下来，继续添加一个名为`SimpleAsset struct` 作为 `Chaincode shim` 方法的接收方。

```go
package main

import (
    "fmt"

    "github.com/hyperledger/fabric/core/chaincode/shim"
    "github.com/hyperledger/fabric/protos/peer"
)

// SimpleAsset implements a simple chaincode to manage an asset
type SimpleAsset struct {
}
```

### Initializing the Chaincode

​	接下来，将要继续**实现 Init** 方法。示例如下：

```go
//Init在智能合约实例化过程中被调用初始化任何数据。
//Init is called during chaincode instantiation to initialize any data.
func (t *SimpleAsset) Init(stub shim.ChaincodeStubInterface) peer.Response {

}
```

> 请注意，chaincode升级也会调用此函数。在编写将**升级现有链代码**的链代码时，请确保正确修改`Init`函数。*特别是，如果没有“迁移”或在升级过程中无需初始化，则提供空的**“Init”**方法。*
>
> In particular, provide an empty “Init” method if there’s no “migration” or nothing to be initialized as part of the upgrade.

​	接下来，将使用[ChaincodeStubInterface.GetStringArgs](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#ChaincodeStub.GetStringArgs) 方法检索`Init`所需要的**参数**，并核实其参数数量的有效性。在这个例子中，需要传入的是一个键值对，即两个参数。

​	接下来，既然我们**已经确定调用有效**，我们将把`initial state`初始状态存储在`ledger`。为此，我们将调用 [ChaincodeStubInterface.PutState](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#ChaincodeStub.PutState) ，并将键值对作为参数传入。假设一切顺利，返回一个`peer.Response`对象，表明初始化成功。

```go
// Init is called during chaincode instantiation to initialize any data. 
// Note that chaincode upgrade also calls this function to reset or to migrate data, so be careful to avoid a scenario where you inadvertently clobber your ledger's data!
// Init在 智能合约实例化过程中被调用来初始化任何数据。
// 注意，智能合约升级也调用这个方法来重置或迁移数据，所以要小心避免在无意中破坏了账本数据的情况！
func (t *SimpleAsset) Init(stub shim.ChaincodeStubInterface) peer.Response {
  // Get the args from the transaction proposal 从交易请求中获取请求参数数组
  args := stub.GetStringArgs()
  if len(args) != 2 {
    return shim.Error("Incorrect arguments. Expecting a key and a value")
  }

  // store the key and the value on the ledger 通过调用 stub.PutState()方法，将资产的value和相关联key存入账本
  err := stub.PutState(args[0], []byte(args[1]))
  if err != nil {
    return shim.Error(fmt.Sprintf("Failed to create asset: %s", args[0]))
  }
  return shim.Success(nil)
}
```

### Invoking the Chaincode

​	首先，让我们添加**Invoke**函数签名。

```go
// 在智能合约上的每笔交易都会调用Invoke。 每一个事务都是“get”或“set”来操作由Init方法创建的资产。
// “set”方法可以通过指定新的键-值对来创建新资产
func (t *SimpleAsset) Invoke(stub shim.ChaincodeStubInterface) peer.Response {

}
```

​	与上面的 `Init` 方法一样，这里需要从 `ChaincodeStubInterface` 接口中提取参数。 `Invoke` 方法的**参数**是将要调用的应用程序**方法的name**。在这个例子中，所编写的应用程序只有两个功能：`set 和 get` ，它允许设定一个资产的值，或者检索它的当前状态。首先调用[ChaincodeStubInterface.GetFunctionAndParameters](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#ChaincodeStub.GetFunctionAndParameters)方法，**提取方法名**和应用程序**功能的参数**。

​	接下来，将继续**验证方法名**是否为 `set 或 get` ，并调用那些`chaincode`应用程序的方法，通过`shim.Success`或`shim.Error`方法返回适当的**响应**，这些方法将响应**序列化**成 `gRPC protobuf`消息。

```go
// Invoke is called per transaction on the chaincode. Each transaction is either a 'get' or a 'set' on the asset created by Init function. 
// The Set method may create a new asset by specifying a new key-value pair.
// 在智能合约上的每笔交易都会调用Invoke。 每一个事务都是“get”或“set”来操作由Init方法创建的资产。
// “set”方法可以通过指定新的键-值对来创建新资产
func (t *SimpleAsset) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
   // 从交易请求中获取方法名和请求参数数组。
    fn, args := stub.GetFunctionAndParameters()

    var result string
    var err error
    if fn == "set" {
            result, err = set(stub, args)
    } else {
            result, err = get(stub, args)
    }
    if err != nil {
            return shim.Error(err.Error())
    }

   // 返回成功结果。
    return shim.Success([]byte(result))
}
```

### Implementing the Chaincode Application 

​	如前所述，**chaincode** 应用实现了两个功能，并可以通过 `Invoke方法`调用。现在让我们实现这些功能。注意正如前面提到的，为了**访问账本的状态**，将利用`shim API` 中的  [ChaincodeStubInterface.PutState](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#ChaincodeStub.PutState)和[ChaincodeStubInterface.GetState](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#ChaincodeStub.GetState) 方法。

```go
// 将资产（包括key和value ）存储在账本上。
// 如果key存在，它会用新key覆盖value
func set(stub shim.ChaincodeStubInterface, args []string) (string, error) {
    if len(args) != 2 {
            return "", fmt.Errorf("Incorrect arguments. Expecting a key and a value")
    }

    err := stub.PutState(args[0], []byte(args[1]))
    if err != nil {
            return "", fmt.Errorf("Failed to set asset: %s", args[0])
    }
    return args[1], nil
}

// 获取指定资产key的value
func get(stub shim.ChaincodeStubInterface, args []string) (string, error) {
    if len(args) != 1 {
            return "", fmt.Errorf("Incorrect arguments. Expecting a key")
    }

    value, err := stub.GetState(args[0])
    if err != nil {
            return "", fmt.Errorf("Failed to get asset: %s with error: %s", args[0], err)
    }
    if value == nil {
            return "", fmt.Errorf("Asset not found: %s", args[0])
    }
    return string(value), nil
}
```

### Pulling it All Together

​	最后，需要添加一个`main`方法，它将调用[shim.Start](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#Start)方法 这是整个智能合约应用程序的入口方法。具体源码示例如下：

```go
package main

import (
    "fmt"

    "github.com/hyperledger/fabric/core/chaincode/shim"
    "github.com/hyperledger/fabric/protos/peer"
)

// SimpleAsset 实现一个简单的智能合约来管理资产。
type SimpleAsset struct {
}

// Init在 智能合约实例化过程中被调用来初始化任何数据。
// 注意，智能合约升级也调用这个方法来重置或迁移数据，所以要小心避免在无意中破坏了账本数据的情况！
func (t *SimpleAsset) Init(stub shim.ChaincodeStubInterface) peer.Response {
    // 从交易请求中获取请求参数数组
    args := stub.GetStringArgs()
    if len(args) != 2 {
            return shim.Error("Incorrect arguments. Expecting a key and a value")
    }

    // 在这里通过调用stub.PutState () 方法来存入资产信息，将资产的value和相关联key存入账本。
    err := stub.PutState(args[0], []byte(args[1]))
    if err != nil {
            return shim.Error(fmt.Sprintf("Failed to create asset: %s", args[0]))
    }
    return shim.Success(nil)
}

// 在智能合约上的每笔交易都会调用Invoke。每个事务都是由Init方法创建的资产的“get”或“set”。
// “set”方法可以通过指定新的键-值对来创建新资产。
func (t *SimpleAsset) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
    // Extract the function and args from the transaction proposal
    fn, args := stub.GetFunctionAndParameters()

    var result string
    var err error
    if fn == "set" {
            result, err = set(stub, args)
    } else { // assume 'get' even if fn is nil
            result, err = get(stub, args)
    }
    if err != nil {
            return shim.Error(err.Error())
    }
    // 返回成功结果。
    return shim.Success([]byte(result))
}

// 将资产（包括key和value ）存储在账本上。 如果key存在，它会用新key覆盖value
func set(stub shim.ChaincodeStubInterface, args []string) (string, error) {
    if len(args) != 2 {
            return "", fmt.Errorf("Incorrect arguments. Expecting a key and a value")
    }

    err := stub.PutState(args[0], []byte(args[1]))
    if err != nil {
            return "", fmt.Errorf("Failed to set asset: %s", args[0])
    }
    return args[1], nil
}

// 获取指定资产key的value
func get(stub shim.ChaincodeStubInterface, args []string) (string, error) {
    if len(args) != 1 {
            return "", fmt.Errorf("Incorrect arguments. Expecting a key")
    }

    value, err := stub.GetState(args[0])
    if err != nil {
            return "", fmt.Errorf("Failed to get asset: %s with error: %s", args[0], err)
    }
    if value == nil {
            return "", fmt.Errorf("Asset not found: %s", args[0])
    }
    return string(value), nil
}

// main方法在实例化过程中启动容器中的智能合约。
func main() {
    if err := shim.Start(new(SimpleAsset)); err != nil {
            fmt.Printf("Error starting SimpleAsset chaincode: %s", err)
    }
}
```

### Building Chaincode

​	现在开始编译chaincode。

```go
go get -u github.com/hyperledger/fabric/core/chaincode/shim
go build
```

​	假定上述没有错误，可以继续下一步，**测试**已经编写好的智能合约。

### Testing Using dev mode

​	一般情况下，`chaincodes` 智能合约是由 `Peer` 节点**启动和维护**，但是在`“ dev 模式”`中 ，**chaincode** 是由**用户自己构建和启动**的。这种模式在 **chaincode** **开发阶段**周期中进行快速`code/build/run/debug` 非常有用。
​	接下来介绍如何开启`“dev 模式”`，利用预先生成的 `Orderer 和 channel artifacts` 进行示例开发网络。因此，用户可以立即跳转到编译智能合约和驱动调用的过程。

## Install Hyperledger Fabric Samples

​	如果你还没有这样安装的话，参考[Install Samples, Binaries and Docker Images](https://hyperledger-fabric.readthedocs.io/en/release-1.4/install.html).

​	进入到clone的`fabric-samples`的 `chaincode-docker-devmode`目录：

```shell
cd chaincode-docker-devmode
```

​	现在打开三个终端并导航到每个终端中的`chaincode-docker-devmode`目录。

### 终端一：Start the network

```shell
docker-compose -f docker-compose-simple.yaml up
```

​	上面启动的网络使用 `SingleSampleMSPSolo` 的 **orderer** 配置文件启动，并在“dev 模式”中启动了 Peer节点。它还会启动两个额外的容器：一个用于智能合约环境和一个与智能合约交互的 **CLI** 。**创建和联接 Channel** 的命令嵌入到 CLI 容器中，因此可以立即跳转到智能合约启动调用。

### 终端二：Build & start the chaincode

```shell
docker exec -it chaincode bash
```

​	可以看到类似如下命令入口：

```shell
root@d2629980e76b:/opt/gopath/src/chaincode#
```

l's	现在就可以编译智能合约了，执行如下命令：

```shell
cd sacc
go build
```

​	执行如下命令，开始运行智能合约：

```shell
CORE_PEER_ADDRESS=peer:7052 CORE_CHAINCODE_ID_NAME=mycc:0 ./sacc
```

​	当Peer 节点成功注册了该智能合约，则会显示智能合约相关的日志、信息。但在这个阶段，智能合约还没有与任何 **Channel** 有关联，需要在随后的智能合约 `instantiate` 实例化命令过后才开始关联。

### 终端三：Use the chaincode

​	即使目前处于 `--peer-chaincodedev` 模式，但仍然需要安装`chaincode` ，以便生命周期系统智能合约能够正常地通过它的检查。官方提示：在以后的版本中，这一步操作可能不再需要，毕竟是在开发模式中。

​	这里将利用 CLI 容器来驱动这些调用 ，执行如下命令进入 CLI 容器：

```shell
docker exec -it cli bash
```

​	继续执行如下命令，安装及实例化智能合约：

```shell
peer chaincode install -p chaincodedev/chaincode/sacc -n mycc -v 0
peer chaincode instantiate -n mycc -v 0 -c '{"Args":["a","10"]}' -C myc
```

​	接下来发出一个 invoke 来改变“a ”的值到“20 ”，执行如下命令：

```sh
peer chaincode invoke -n mycc -c '{"Args":["set", "a", "20"]}' -C myc
```

​	最后查询a 。应该看到值为 20 ，执行如下命令：

```shell
peer chaincode query -n mycc -c '{"Args":["query","a"]}' -C myc
```

## Testing new chaincode

​	在默认情况下，只安装并实例化了 **sacc** 。但是，可以轻松测试不同的智能合约**chaincodes** ，添加到 `chaincode` **子目录**并重新启动这个网。在这一点上，它们将在智能合约容器中访问。

### Chaincode access control

​	**Chaincode**可以通过调用`GetCreator()`函数获取**客户端（提交者）证书**，以此进行**访问控制决策**。此外，Go **shim**提供了扩展API，可从提交者的证书中提取客户端身份，该证书可用于访问控制决策，无论是基于**客户端身份本身**，还是**组织身份**，还是客户端**身份属性**。

​	例如，表示为键/值的资产可以包括**客户端的标识**作为值的一部分（例如，作为指示该资产所有者的**JSON属性**），并且只有该客户端可以被授权对密钥/值进行更新。可以在**chaincode** 中使用`client identity library` 的扩展API来检索此`submitter` 信息以做出此类访问控制决策。

​	有关更多详细信息，请参阅客户端标识（CID）[client identity (CID) library documentation](https://github.com/hyperledger/fabric/blob/master/core/chaincode/shim/ext/cid/README.md) 库文档。

​	要将 `client identity shim extension` 添加到`chaincode` 作为`dependency`依赖关系，请参阅[Managing external dependencies for chaincode written in Go](https://hyperledger-fabric.readthedocs.io/en/latest/chaincode4ade.html#vendoring).

### Chaincode encryption

In certain scenarios, it may be useful to encrypt values associated with a key in their entirety or simply in part.  For example, if a person’s social security number or address was being written to the ledger, then you likely would not want this data to appear in plaintext.  Chaincode encryption is achieved by leveraging the [entities extension](https://github.com/hyperledger/fabric/tree/master/core/chaincode/shim/ext/entities) which is a BCCSP wrapper with commodity factories and functions to perform cryptographic operations such as encryption and elliptic curve digital signatures.  For example, to encrypt, the invoker of a chaincode passes in a cryptographic key via the transient field.  The same key may then be used for subsequent query operations, allowing for proper decryption of the encrypted state values.

For more information and samples, see the [Encc Example](https://github.com/hyperledger/fabric/tree/master/examples/chaincode/go/enccc_example) within the `fabric/examples` directory.  Pay specific attention to the `utils.go` helper program.  This utility loads the chaincode shim APIs and Entities extension and builds a new class of functions (e.g. `encryptAndPutState` & `getStateAndDecrypt`) that the sample encryption chaincode then leverages.  As such, the chaincode can now marry the basic shim APIs of `Get` and `Put` with the added functionality of `Encrypt` and `Decrypt`.

To add the encryption entities extension to your chaincode as a dependency, see [Managing external dependencies for chaincode written in Go](https://hyperledger-fabric.readthedocs.io/en/latest/chaincode4ade.html#vendoring).

​	在某些情况下，key关联的值可以值完全或仅部分地加密。例如，如果某人的社会安全号码或地址被写入分类帐，那么您可能不希望此数据以明文形式出现。

### Managing external dependencies for chaincode written in Go

​	如果您的 **chaincode** 需要**Go标准库**外的包，则你的`ChainCode`需要包含这些**packages**。将**shim**和任何其他扩展库作为你**ChainCode**的依赖是一个很好的实践。

​	有 [many tools available](https://github.com/golang/go/wiki/PackageManagementTools) 用于管理这些依赖项。以下演示了如何使用`govendor`：

```shell
govendor init
govendor add +external  // Add all external package, or
govendor add github.com/external/pkg // Add specific external package
```

​	以上命令将**外部依赖项**导入**本地** `vendor` 目录。如果要供应`Fabric shim or shim` 扩展，请在执行`govendor`命令之前将**Fabric存储库**克隆到`$GOPATH/src/github.com/hyperledger`目录。

​	一旦依赖关系出现在您的chaincode目录中，`peer chaincode package` and `peer chaincode install` 操作将包含与链代码包中的**依赖关联的代码**。

# Read and Write Set 读写集



## Transaction模拟和读写集

​	在一个 `endorser`背书节点模拟`transaction`期间，为交易准备了一个读写集合`read-write set` 。`read set`包含在模拟期间transaction读取的唯一键`key`及其提交的版本`version`的列表。`write set`包含唯一键`key`的列表（尽管可能与读集中存在的键重叠）以及transaction写入的新值`new values`。如果transaction执行的更新是删除`delete key`，那么设置key的删除标记（代替新值）。A delete marker is set (in the place of new value) for the key if the update performed by the transaction is to delete the key.

​	此外，如果transaction为key多次写入值，则只保留最后写入的值`last written value`。另外，如果一个transaction为一个key读取一个值，即使transaction在发出读取之前更新了key的值，也会返回提交状态中的值。换句话说，不支持`Read-your-writing`语义。 Also, if a transaction reads a value for a key, the value in the committed state is returned even if the transaction has updated the value for the key before issuing the read. In another words, Read-your-writes semantics are not supported.

​	如前所述，`version of key` 键的版本仅在`read-set`读集中记录；`write-set` 写入集只包含唯一键`value`的列表及其由transaction设置的最新值`last value`。

​	可能有各种实现版本的方案。版本控制方案的最低要求是为给定的key生成不重复的标识符。例如，对于版本使用单调增加的数字可以是一种这样的方案。在当前的实现中，我们使用基于区块链高度的版本控制方案，其中提交transaction的高度被用作所有由transaction修改的key的最新版本。在该方案中，transaction的高度由tuple表示（txNumber是块内事务的高度）。该方案比增量数量方案具有许多优点 - 主要是它可以实现其他组件，如说明，交易模拟和验证，以进行有效的设计选择。

There could be various schemes for implementing versions. The minimal requirement for a versioning scheme is to produce non-repeating identifiers for a given key. For instance, using monotonically increasing numbers for versions can be one such scheme. In the current implementation, we use a blockchain height based versioning scheme in which the height of the committing transaction is used as the latest version for all the keys modified by the transaction. In this scheme, the height of a transaction is represented by a tuple (txNumber is the height of the transaction within the block). This scheme has many advantages over the incremental number scheme - primarily, it enables other components such as statedb, transaction simulation and validation for making efficient design choices.

​	以下是通过模拟假设交易准备的示例读写集的说明。为了简单起见，在插图中，我们使用增量数字来表示版本。

```xml
< TxReadWriteSet > 
  < NsReadWriteSet  name = “chaincode1” > 
    < read - set > 
      < read  key = “K1” ， version = “1” > 
      < read  key = “K2” ， version = “1” > 
    </ read - set > 
    < write - set > 
      < write  key = “K1” ， value = “V1” 
      < write  key = “K3” ， value = “V2” 
      < write  key = “K4” ， isDelete = “true” 
    </ write - set > 
  </ NsReadWriteSet > 
< TxReadWriteSet >
```

此外，如果transaction在模拟期间执行`range query`，则`range query`及其结果将被添加到读写集合中作为`query-info`。

## 使用读写集合进行transaction验证和更新世界状态

​	**一个`committer`使用读写集合的 `read set`读取部分来检查transaction的有效性，用`write set`写集部分来更新相应键的版本和值`key and value`。**

​	**在验证阶段，如果 `read set`读集中的每个key版本和`world state`世界状态中该`key and value` 相同，那么就认为这个transaction是`valid**` - 假设所有之前的`valid transaction`（包括同一块中的之前的transaction）被提交了（`committed-state`）。如果读写集合还包含一个或多个查询信息（`query-info`），则执行另外的验证。

​	附加验证应确保在`query-info`中没有key要被插入/删除/更新`inserted/deleted/updated`。换句话说，如果我们在对提交状态进行验证时重新执行任何range queries（在模拟期间执行的transaction），则应该和模拟期间执行的结果一样。此检查确保如果transaction在提交期间观察”phantom”项目，则该transaction应标记为无效。请注意，此phantom保护仅限于range query（即， GetStateByRange链码中的功能），还没有为其他查询（即，GetQueryResult链码中的功能）实现。其他查询有phantom的风险，因此只能用于只读的transaction，除非应用程序可以保证结果集在模拟、验证/提交期间是不变的。

This additional validation should ensure that no key has been inserted/deleted/updated in the super range (i.e., union of the ranges) of the results captured in the query-info(s). In other words, if we re-execute any of the range queries (that the transaction performed during simulation) during validation on the committed-state, it should yield the same results that were observed by the transaction at the time of simulation. This check ensures that if a transaction observes phantom items during commit, the transaction should be marked as invalid. Note that the this phantom protection is limited to range queries (i.e., `GetStateByRange` function in the chaincode) and not yet implemented for other queries (i.e., `GetQueryResult` function in the chaincode). Other queries are at risk of phantoms, and should therefore only be used in read-only transactions that are not submitted to ordering, unless the application can guarantee the stability of the result set between simulation and validation/commit time.

​	如果`transaction`通过有效性检查`validity check`，则`committer`**使用写集更新世界状态**。在更新阶段，对于写集中存在的每个键，将相同键的世界状态值设置为写集中指定的值。此外，世界状态的key版本已更改，以反映最新版本。

## 模拟和验证示例

​	本节通过示例场景帮助理解语义。出于本例的目的，`k`表示世界状态中的键，世界状态由元组表示`(k,ver,val)`，其中 `ver`是最新版本，`k`是键，`val`是对应的值。

​	现在考虑五个transaction`T1, T2, T3, T4, and T5`，全部模拟在世界状态的同一个快照上。以下代码段显示了模拟transaction的世界状态的快照，以及每个transaction执行的读取和写入活动的顺序。

```
World state: (k1,1,v1), (k2,1,v2), (k3,1,v3), (k4,1,v4), (k5,1,v5)
T1 -> Write(k1, v1'), Write(k2, v2')
T2 -> Read(k1), Write(k3, v3')
T3 -> Write(k2, v2'')
T4 -> Write(k2, v2'''), read(k2)
T5 -> Write(k6, v6'), read(k5)
```

​	现在，假设这些事务按照T1，..，T5的顺序排列（可以包含在单个块或不同的块中）

- `T1`通过验证，因为它不执行任何读取。此外，键`k1`和`k2`世界状态的元组被更新为`(k1,2,v1'), (k2,2,v2')`
- `T2`验证失败，因为它读取一个`k1`，这个键由前一个transaction`T1`修改。
- `T3`通过验证，因为它不执行读取。此外，键`k2`，在世界状态更新到`(k2,3,v2'')`
- `T4`验证失败，因为它读取`k2`，该键由先前transaction`T1`修改。
- `T5`通过验证，因为它读取一个键`k5`,该键未被任何之前的transaction修改。

