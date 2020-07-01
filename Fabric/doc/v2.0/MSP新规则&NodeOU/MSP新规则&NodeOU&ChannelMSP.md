## Admincerts的改动。

### admincerts文件夹

**admincerts (Deprecated from Fabric v1.4.3 and higher):** This folder contains a list of identities that define the actors who have the role of administrators for this organization. In general, there should be one or more X.509 certificates in this list.

这个文件夹中包含，一组在该组织中拥有管理远权限的身份，有至少一个X509证书在这之中。

**Note:** Prior to Fabric v1.4.3, admins were defined by explicitly putting certs in the `admincerts` folder in the local MSP directory of your peer. **With Fabric v1.4.3 or higher, certificates in this folder are no longer required.** Instead, it is recommended that when the user is registered with the CA, that the `admin` role is used to designate the node administrator. Then, the identity is recognized as an `admin` by the Node OU role value in their signcert. As a reminder, in order to leverage the admin role, the “identity classification” feature must be enabled in the config.yaml above by setting “Node OUs” to `Enable: true`. We’ll explore this more later.

在1.4.3之前的版本，管理员身份需要明确地通过将证书放置在peer节点的本地MSP目录中的admincerts文件夹来定义。但在1.4.3之后的版本，证书不再一定要放置在该文件当中。相反，必须在用CA注册用户的时候，指定`admin`角色为一个节点管理员角色。然后，通过在signcert中的**Node OU** 角色值来辨认管理员。（提示）为了使管理员角色生效，必须让身份分类功能开启。通过在config.yaml文件中设置**“Node OUs”** 属性 `Enable: true`完成。

And as a reminder, for Channel MSPs, just because an actor has the role of an administrator it doesn’t mean that they can administer particular resources. The actual power a given identity has with respect to administering the system is determined by the *policies* that manage system resources. For example, a channel policy might specify that `ORG1-MANUFACTURING` administrators have the rights to add new organizations to the channel, whereas the `ORG1-DISTRIBUTION` administrators have no such rights.

再次提醒，对于**Channel MSPs**而言，一个角色是管理员并不意味着他就能管理特定的资源。真正给予角色能够管理系统的权限是由管理系统资源的策略决定的。例如，一个通道策略可能指定`ORG1-MANUFACTURING`管理员有为通道增加组织的权利，而另一个管理员`ORG1-DISTRIBUTION`则没有这个权利。



> 概念词，Channel MSP，Node OU

## NodeOU

参考：[Node OU Roles and MSPs ](<https://hyperledger-fabric.readthedocs.io/en/release-2.1/membership/membership.html?highlight=MSP#node-ou-roles-and-msps>)

> 用于细分组织身份的粒度

### Node OU Roles and **MSP**s

Additionally, there is a special kind of OU, sometimes referred to as a `Node OU`, that can be used to confer a role onto an identity. These Node OU roles are defined in the `$FABRIC_CFG_PATH/msp/config.yaml` file and contain a list of organizational units whose members are considered to be part of the organization represented by this **MSP**. This is particularly useful when you want to restrict the members of an organization to the ones holding an identity (signed by one of MSP designated CAs) with a specific Node OU role in it. For example, with node OU’s you can implement a more granular endorsement policy that requires Org1 peers to endorse a transaction, rather than any member of Org1.

另外，也有一些特殊的组织单元（OU，Organization Unit）。有时描述他为`Node OU`，他能用于赋予某个角色身份。`Node OU`角色被定义在`$FABRIC_CFG_PATH/msp/config.yaml` 文件当中。`Node OU`角色包含一组组织单元，这些组织单元通过MSP来进行代表，可以被认为是组织的一员。这对你要限制组织中一个拥有具体的`Node OU`角色的成员（有CAs 分配的MSP进行签名的角色）非常有帮助。例如，有了`node OU`你就能实现通过Org1 的peer节点背书交易而不是Org1的所有节点都能用来背书交易，这样一种更细粒度的背书策略。

In order to use the Node OU roles, the “identity classification” feature must be enabled for the network. When using the folder-based **MSP** structure, this is accomplished by enabling “Node OUs” in the config.yaml file which resides in the root of the MSP folder:

要使用`Node OU`角色，必须使网络中的“身份分类”属性生效。这功能在使用基于文件夹的MSP结构时，通过设置MSP根目录中的`config.yaml`文件内**“Node OUs”** 属性 `Enable: true`来完成。文件内容如下所示：

```yaml
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.sampleorg-cert.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.sampleorg-cert.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.sampleorg-cert.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.sampleorg-cert.pem
    OrganizationalUnitIdentifier: orderer
```

In the example above, there are 4 possible Node OU `ROLES` for the **MSP**:

上述例子中有四种可能的MSP` Node OU `角色

- client
- peer
- admin
- orderer

This convention allows you to distinguish **MSP** roles by the OU present in the CommonName attribute of the X509 certificate. The example above says that any certificate issued by cacerts/ca.sampleorg-cert.pem in which OU=client will identified as a client, OU=peer as a peer, etc. Starting with Fabric v1.4.3, there is also an OU for the orderer and for admins. The new admins role means that you no longer have to explicitly place certs in the admincerts folder of the **MSP** directory. Rather, the `admin` role present in the user’s signcert qualifies the identity as an admin user.

这一协议允许你通过在X509证书中的`CommonName `属性表示的OU来区分**MSP**角色。上述例子中，任何被`cacerts/ca.sampleorg-cert.pem`提出的证书，被定义为OU=client将被身份认定为client，OU=peer 认定为 peer。从Fabric v1.4.3起，还能设置OU为`orderer`和`admins`。新的管理身份意味着，你不在必须显式的放置正式在**MSP**目录的`admincerts`文件夹下。Rather, the `admin` role present in the user’s signcert qualifies the identity as an admin user.



These Role and OU attributes are assigned to an identity when the Fabric CA or SDK is used to `register` a user with the CA. It is the subsequent `enroll` user command that generates the certificates in the users’ `/msp` folder.

这些角色和OU属性可在使用Fabric-CA或者通过SDK使用CA进行注册用户的时候被用于分配一个身份。随后使用enroll命令在该用户的`/msp`文件夹下生成其证书。

![MSP1c](./MSP新规则&NodeOU.assets/ca-msp-visualization.png)

The resulting ROLE and OU attributes are visible inside the X.509 signing certificate located in the `/signcerts` folder. The `ROLE` attribute is identified as `hf.Type` and refers to an actor’s role within its organization, (specifying, for example, that an actor is a `peer`). See the following snippet from a signing certificate shows how the Roles and OUs are represented in the certificate.

`ROLE`和`OU`属性能够在`/signcerts`文件夹下，通过X.509协议进行签名的证书中看到。

`ROLE`属性被标识为`hf.type`并且与其在组织中的角色相关，（例如，某个角色可以是peer）。可以通过签名证书中的片段看到`Roles`和`OUs`是如何在证书当中表示的。



![MSP1d](./MSP新规则&NodeOU.assets/signcert.png)

**Note:** For Channel **MSP**s, just because an actor has the role of an administrator it doesn’t mean that they can administer particular resources. The actual power a given identity has with respect to administering the system is determined by the *policies* that manage system resources. For example, a channel policy might specify that `ORG1-MANUFACTURING` administrators, meaning identities with a role of `admin` and a Node OU of `ORG1-MANUFACTURING`, have the rights to add new organizations to the channel, whereas the `ORG1-DISTRIBUTION` administrators have no such rights.

**提示**：对于Channel **MSP**s, 一个角色是管理员并不意味着他就能管理特定的资源。真正给予角色能够管理系统的权限是由管理系统资源的策略决定的。例如，例如，一个通道策略可能指定`ORG1-MANUFACTURING`管理员，这意味着它有着admin权限的身份和Node OU 中的 `ORG1-MANUFACTURING`身份，有为通道增加组织的权利，而另一个管理员`ORG1-DISTRIBUTION`则没有这个权利。

Finally, OUs could be used by different organizations in a consortium to distinguish each other. But in such cases, the different organizations have to use the same Root CAs and Intermediate CAs for their chain of trust, and assign the OU field to identify members of each organization. When every organization has the same CA or chain of trust, this makes the system more centralized than what might be desirable and therefore deserves careful consideration on a blockchain network.

最后，OUs可以被用于在一个联盟中的不同组织进行区分各自的身份。但在某些例子中，不同的组织必须使用相同的根CA和中间CA作为他们所在链的可信服务，并且分配各个组织中的成员OU域的身份。当每个组织拥有相同的CA或者链的可信服务时，系统就更加中心化（不是所期望的），并且也因此需要在区块链网络中进行更多周密的考虑。



## Channel MSP

> 定义哪些身份能够进行管理操作通道。

In contrast, **channel MSPs define administrative and participatory rights at the channel level**. Peers and ordering nodes on an application channel share the same view of channel **MSP**s, and will therefore be able to correctly authenticate the channel participants. This means that if an organization wishes to join the channel, an MSP incorporating the chain of trust for the organization’s members would need to be included in the channel configuration. Otherwise transactions originating from this organization’s identities will be rejected. Whereas local MSPs are represented as a folder structure on the file system, channel MSPs are described in a channel configuration.

`channel MSP`明确了在通道级别的管理和参与的权限。Peers和Ordering节点在应用通道上共享同一个`channel MSPs`, 并且能够正确的认证所在通道的参与者。这意味着，如果一个组织想要加入一个通道，一个MSP在链当中为组织成员注册的可信身份，需要被包含在channel的配置当中。否则，来自此组织身份的事务将被拒绝。本地MSPs在文件系统中以文件夹的结构存在，而channel MSPs则在通道配置中进行描述。

![ChannelMSP](.\MSP新规则&NodeOU.assets\ChannelMSP.png)

*Snippet from a channel config.json file that includes two organization MSPs.*

从通道config.json配置文件中的截取片段，该片段包括两个MSP组织。

**Channel MSPs identify who has authorities at a channel level**. The channel **MSP** defines the *relationship* between the identities of channel members (which themselves are MSPs) and the enforcement of channel level policies. Channel MSPs contain the MSPs of the organizations of the channel members.

**Channel MSPs 规定了那些人有通道级别的权限**。通道MSP定义了通道成员各个身份之间的关系和通道级别策略的执行。通道MSP包含通道成员组织的MSP。

**Every organization participating in a channel must have an MSP defined for it**. In fact, it is recommended that there is a one-to-one mapping between organizations and **MSP**s. The MSP defines which members are empowered to act on behalf of the organization. This includes configuration of the MSP itself as well as approving administrative tasks that the organization has role, such as adding new members to a channel. If all network members were part of a single organization or MSP, data privacy is sacrificed. Multiple organizations facilitate privacy by segregating ledger data to only channel members. If more granularity is required within an organization, the organization can be further divided into organizational units (OUs) which we describe in more detail later in this topic.

每个参与到通道的组织必须有一个MSP为其进行定义。事实上，组织和channel MSPs间要求是一对一的映射关系。这个MSP定义了那些成员能够代表组织进行操作。包括MSP本身的配置和认同组织成员的管理性的任务，例如，为通道添加一个组织。如果所有的网络成员都是同一个组织或者MSP之内的，数据的隐私将被牺牲。多组织通过给通道成员隔离账本数据来促进提高隐私性。如果一个组织中需要更多粒度，组织可以更进一步划分为组织单元（OUs）。

**The system channel MSP includes the MSPs of all the organizations that participate in an ordering service.** An ordering service will likely include ordering nodes from multiple organizations and collectively these organizations run the ordering service, most importantly managing the consortium of organizations and the default policies that are inherited by the application channels.

系统通道MSP包括所有加入到排序服务的组织的MSP。一个排序服务将包括从多个组织给出的排序节点并且这些组织共同运行排序服务，最终要的是管理这些组织的联盟，默认的策略隐含在应用通道当中。

**Local MSPs are only defined on the file system of the node or user** to which they apply. Therefore, physically and logically there is only one local **MSP** per node. However, as channel **MSP**s are available to all nodes in the channel, they are logically defined once in the channel configuration. However, **a channel MSP is also instantiated on the file system of every node in the channel and kept synchronized via consensus**. So while there is a copy of each channel MSP on the local file system of every node, logically a channel MSP resides on and is maintained by the channel or the network.

本地MSP仅在节点或用户的文件系统中定义。因此，物理和逻辑上每个节点都只有一个MSP。然而，由于通道MSP对通道中的所有节点都可用，他们逻辑上只在通道的配置文件中定义一次。然而，一个通道MSP也可以在每个通道中的每个节点的文件系统中实现，并且需要通过共识保持同步。虽然当在节点的本地文件系统中，但都有一个channel MSP的拷贝，逻辑上，一个MSP放置并保留在通道上或者网络中。

The following diagram illustrates how local and channel **MSP**s coexist on the network:

如下图是展示，本地和通道MSP是如何在网络中共同存在的。

![membership.diagram.2](.\MSP新规则&NodeOU.assets\membership.diagram.2.png)

*The MSPs for the peer and orderer are local, whereas the MSPs for a channel (including the network configuration channel, also known as the system channel) are global, shared across all participants of that channel. In this figure, the network system channel is administered by ORG1, but another application channel can be managed by ORG1 and ORG2. The peer is a member of and managed by ORG2, whereas ORG1 manages the orderer of the figure. ORG1 trusts identities from RCA1, whereas ORG2 trusts identities from RCA2. It is important to note that these are administration identities, reflecting who can administer these components. So while ORG1 administers the network, ORG2.MSP does exist in the network definition.*

peer和orderer的MSPs是放在本地的，然而通道（包括网络位置通道，也即是system 通道）的MSPs是全局的，在通道中所有的成员所共享的。在图中，该网络系统通道被ORG1所管理，然而另一个应用通道可以被ORG1和ORG2共同管理。图中的peer是ORG2的成员受ORG2所管理，而ORG1则管理Orderer。ORG1的可信身份来自RCA1，而ORG2的可信身份来自RCA2。需要重点注意到的是，这些是管理员身份，涉及到哪些成员能够管理这些组件。因此，虽然ORG1管理整个网络，同时ORG2的MSP也存在与网络的定义当中。











