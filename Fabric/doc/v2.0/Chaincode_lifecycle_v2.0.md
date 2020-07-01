# Fabric chaincode lifecycle

## What is Chaincode?

Chaincode is a program, written in [Go](https://golang.org/), [Node.js](https://nodejs.org/), or [Java](https://java.com/en/) that implements a prescribed interface. Chaincode runs in a secured Docker container isolated from the endorsing peer process. Chaincode initializes and manages ledger state through transactions submitted by applications.

链码是使用Go, Node或者Java编写的可实现规定接口的程序。Chaincode在与认可对等进程相隔离的安全Docker容器中运行。Chaincode通过应用程序提交的事务来初始化和管理分类帐状态。

A chaincode typically handles business logic agreed to by members of the network, so it may be considered as a “smart contract”. Ledger updates created by a chaincode are scoped exclusively to that chaincode and can’t be accessed directly by another chaincode. However, within the same network, given the appropriate permission a chaincode may invoke another chaincode to access its state.

链码通常处理网络成员同意的业务逻辑，因此可以将其视为“智能合约”。由一个链码创建的分类账更新仅限于该链码的范围，并且不能被另一个链码直接访问。但是，在同一个网络中，在获得适当许可的情况下，链码可以调用另一个链码以访问其状态。

In this concept topic, we will explore chaincode through the eyes of a blockchain network operator rather than an application developer. Chaincode operators can use this topic as a guide to how to use the Fabric chainode lifecycle to deploy and manage chaincode on their network.

在这个主题中，我们将通过区块链网络运营人员而不是应用程序开发人员的视角来探索链代码。链码运营人员可以将此主题用作如何使用Fabric链码生命周期在其网络上部署和管理链码的指南。

## Deploying a chaincode

The Fabric chaincode lifecycle is a process that allows multiple organizations to agree on how a chaincode will be operated before it can be used on a channel. A network operator would use the Fabric lifecycle to perform the following tasks:

Fabric链码生命周期是一个过程，它允许多个组织在链码可以在通道上使用之前就如何操作链码达成一致。网络运营商将使用Fabric生命周期来执行以下任务：

- [Install and define a chaincode](https://hyperledger-fabric.readthedocs.io/en/release-2.0/chaincode_lifecycle.html#install-and-define-a-chaincode)
- [Upgrade a chaincode](https://hyperledger-fabric.readthedocs.io/en/release-2.0/chaincode_lifecycle.html#upgrade-a-chaincode)
- [Deployment Scenarios](https://hyperledger-fabric.readthedocs.io/en/release-2.0/chaincode_lifecycle.html#deployment-scenarios)
- [Migrate to the new Fabric lifecycle](https://hyperledger-fabric.readthedocs.io/en/release-2.0/chaincode_lifecycle.html#migrate-to-the-new-fabric-lifecycle)

You can use the Fabric chaincode lifecycle by creating a new channel and setting the channel capabilities to V2_0. You will not be able to use the old lifecycle to install, instantiate, or update a chaincode on channels with V2_0 capabilities enabled. However, you can still invoke chaincode installed using the previous lifecycle model after you enable V2_0 capabilities. If you are upgrading from a v1.4.x network and need to edit your channel configurations to enable the new lifecycle, check out [Enabling the new chaincode lifecycle](https://hyperledger-fabric.readthedocs.io/en/release-2.0/enable_cc_lifecycle.html).

您可以通过创建新通道并将通道功能设置为V2_0来使用Fabric链码生命周期。您将无法使用旧的生命周期在启用了V2_0功能的频道上安装、实例化或更新链码。但是，启用V2_0功能后，您仍然可以调用使用以前的生命周期模型安装的链码。如果要从v1.4.x网络升级，并且需要编辑通道配置以启用新的生命周期，请签出“ [启用新的链码生命周期”](https://hyperledger-fabric.readthedocs.io/en/release-2.0/enable_cc_lifecycle.html)。

## Install and define a chaincode

Fabric chaincode lifecycle requires that organizations agree to the parameters that define a chaincode, such as name, version, and the chaincode endorsement policy. Channel members come to agreement using the following four steps. Not every organization on a channel needs to complete each step.

Fabric链码生命周期要求组织同意定义链码的参数，例如名称，版本和链码认可策略。渠道成员通过以下四个步骤达成协议。并非渠道上的每个组织都需要完成每个步骤。

1. **Package the chaincode:** This step can be completed by one organization or by each organization.

   **打包链码：**可以由一个组织或每个组织完成此步骤。

2. **Install the chaincode on your peers:** Every organization that will use the chaincode to endorse a transaction or query the ledger needs to complete this step.

   **在您的peer节点上安装链码：**每个想要使用链码来背书交易或查询分类帐的组织都需要完成此步骤。

3. **Approve a chaincode definition for your organization:** Every organization that will use the chaincode needs to complete this step. The chaincode definition needs to be approved by a sufficient number of organizations to satisfy the channel’s LifecycleEndorsment policy (a majority, by default) before the chaincode can be started on the channel.

   **批准组织的链码定义：**每个使用链码的组织都需要完成此步骤。链码定义需要得到足够多的组织的批准，才能满足频道的LifecycleEndorsment策略（默认情况下，大多数是默认），然后才能在频道上启动链码。

4. **Commit the chaincode definition to the channel:** The commit transaction needs to be submitted by one organization once the required number of organizations on the channel have approved. The submitter first collects endorsements from enough peers of the organizations that have approved, and then submits the transaction to commit the chaincode definition.

   **将链码定义提交到通道：** 通道上所需数量的组织获得批准后，提交事务需要由一个组织提交。提交者首先从已经批准的组织的足够的同龄人那里收集认可，然后提交交易以提交链码定义。

This topic provides a detailed overview of the operations of the Fabric chaincode lifecycle rather than the specific commands. To learn more about how to use the Fabric lifecycle using the Peer CLI, see the [Deploying a smart contract to a channel tutorial](https://hyperledger-fabric.readthedocs.io/en/release-2.0/deploy_chaincode.html) or the [peer lifecycle command reference](https://hyperledger-fabric.readthedocs.io/en/release-2.0/commands/peerlifecycle.html).

本主题详细介绍了Fabric链码生命周期的操作，而不是特定的命令。要了解有关如何使用对等CLI使用Fabric生命周期的更多信息，请参阅将 [智能合约部署到通道教程](https://hyperledger-fabric.readthedocs.io/en/release-2.0/deploy_chaincode.html) 或对[等生命周期命令参考](https://hyperledger-fabric.readthedocs.io/en/release-2.0/commands/peerlifecycle.html)。

### Step One: Packaging the smart contract

Chaincode needs to be packaged in a tar file before it can be installed on your peers. You can package a chaincode using the Fabric peer binaries, the Node Fabric SDK, or a third party tool such as GNU tar. When you create a chaincode package, you need to provide a chaincode package label to create a succinct and human readable description of the package.

Chaincode必须先包装在tar文件中，然后才能安装在对等方上。您可以使用Fabric对等二进制文件，Node Fabric SDK或第三方工具（例如GNU tar）来打包链码。创建chaincode程序包时，需要提供一个chaincode程序包标签，以创建该程序包的简洁易读的描述。

If you use a third party tool to package the chaincode, the resulting file needs to be in the format below. The Fabric peer binaries and the Fabric SDKs will automatically create a file in this format.

如果您使用第三方工具打包链式代码，则生成的文件需要采用以下格式。Fabric对等二进制文件和Fabric SDK将自动创建此格式的文件。

- The chaincode needs to be packaged in a tar file, ending with a `.tar.gz` file extension.

  链码需要打包在tar文件中，并以`.tar.gz`文件扩展名结尾。

- The tar file needs to contain two files (no directory): a metadata file “Chaincode-Package-Metadata.json” and another tar containing the chaincode files.

  tar文件需要包含两个文件（无目录）：元数据文件“ Chaincode-Package-Metadata.json”和另一个包含链码文件的tar。

- “Chaincode-Package-Metadata.json” contains JSON that specifies the chaincode language, code path, and package label. You can see an example of a metadata file below:

  “ Chaincode-Package-Metadata.json”包含用于指定链码语言，代码路径和包标签的JSON。您可以在下面看到元数据文件的示例：

```shell
{"Path":"fabric-samples/chaincode/fabcar/go","Type":"golang","Label":"fabcarv1"}
```

![Lifecycle-package](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-package.png)

*The chaincode is packaged separately by Org1 and Org2. Both organizations use MYCC_1 as their package label in order to identify the package using the name and version. It is not necessary for organizations to use the same package label.*

*链码由Org1和Org2分别打包。两个组织都使用MYCC_1作为其包装标签，以便使用名称和版本来标识包装。组织不一定要使用相同的包装标签。*

### Step Two: Install the chaincode on your peers

You need to install the chaincode package on every peer that will execute and endorse transactions. Whether using the CLI or an SDK, you need to complete this step using your **Peer Administrator**. Your peer will build the chaincode after the chaincode is installed, and return a build error if there is a problem with your chaincode. It is recommended that organizations only package a chaincode once, and then install the same package on every peer that belongs to their org. If a channel wants to ensure that each organization is running the same chaincode, one organization can package a chaincode and send it to other channel members out of band.

您需要在将执行和背书交易的每个对等节点上安装chaincode软件包。无论使用CLI还是SDK，您都需要使用**Peer Administrator**完成此步骤。您的对等节点将在安装链码后构建链码，如果链码存在问题，则返回构建错误。建议组织仅打包一次链式代码，然后在属于其组织的每个对等方上安装相同的软件包。如果某个渠道想要确保每个组织都运行相同的链码，则一个组织可以打包一个链码并将其发送到带外其他渠道成员。

A successful install command will return a chaincode package identifier, which is the package label combined with a hash of the package. This package identifier is used to associate a chaincode package installed on your peers with a chaincode definition approved by your organization. **Save the identifier** for next step. You can also find the package identifier by querying the packages installed on your peer using the Peer CLI.

命令安装成功将返回一个链码软件包标识符，该标识符是软件包标签和软件包的哈希值。此软件包标识符用于将对等方安装的链码软件包与组织批准的链码定义相关联。**保存标识符** 以进行下一步。您还可以通过使用对等CLI查询对等方上安装的软件包来找到软件包标识符。

![Lifecycle-package](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-install.png)

*A peer administrator from Org1 and Org2 installs the chaincode package MYCC_1 on the peers joined to the channel. Installing the chaincode package builds the chaincode and creates a package identifier of MYCC_1:hash.*

*来自Org1和Org2的peer节点管理员将链码软件包MYCC_1安装在加入该通道的peer节点上。安装chaincode程序包将构建该chaincode，并创建MYCC_1：hash的程序包标识符。*

### Step Three: Approve a chaincode definition for your organization

The chaincode is governed by a **chaincode definition**. When channel members approve a chaincode definition, the approval acts as a vote by an organization on the chaincode parameters it accepts. These approved organization definitions allow channel members to agree on a chaincode before it can be used on a channel. The chaincode definition includes the following parameters, which need to be consistent across organizations:

链码受**链码定义约束**。渠道成员批准链码定义时，批准将作为组织对其接受的链码参数的投票。这些批准的组织定义允许渠道成员在可在渠道上使用链码之前达成共识。链码定义包含以下参数，这些参数在组织之间必须保持一致：

- **Name:** The name that applications will use when invoking the chaincode.

  **名称：**应用程序在调用链码时将使用的名称。

- **Version:** A version number or value associated with a given chaincodes package. If you upgrade the chaincode binaries, you need to change your chaincode version as well.

  **版本：**与给定chaincode程序包关联的版本号或数值。如果要升级链码二进制文件，同时还需要更改链码版本。

- **Sequence:** The number of times the chaincode has been defined. This value is an integer, and is used to keep track of chaincode upgrades. For example, when you first install and approve a chaincode definition, the sequence number will be 1. When you next upgrade the chaincode, the sequence number will be incremented to 2.

  **顺序：**定义链码的次数。此值是整数，用于跟踪链码升级。例如，当您第一次安装并批准链码定义时，序列号将为1。下次升级链码时，序列号将增加为2。

- **Endorsement Policy:** Which organizations need to execute and validate the transaction output. The endorsement policy can be expressed as a string passed to the CLI, or it can reference a policy in the channel config. By default, the endorsement policy is set to `Channel/Application/Endorsement`, which defaults to require that a majority of organizations in the channel endorse a transaction.

  **背书政策：**哪些组织需要执行和验证交易输出。背书策略可以表示为传递给CLI的字符串，也可以引用通道配置中的策略。默认情况下，背书策略设置为`Channel/Application/Endorsement`，默认情况下要求渠道中的大多数组织背书交易。

- **Collection Configuration:** The path to a private data collection definition file associated with your chaincode. For more information about private data collections, see the [Private Data architecture reference](https://hyperledger-fabric.readthedocs.io/en/master/private-data-arch.html).

- **集合配置：**与您的链码关联的私有数据集合定义文件的路径。有关私有数据收集的更多信息，请参阅[私有数据体系结构参考](https://hyperledger-fabric.readthedocs.io/en/master/private-data-arch.html)。

- **Initialization:** All chaincode need to contain an `Init` function that is used to initialize the chaincode. By default, this function is never executed. However, you can use the chaincode definition to request that the `Init` function be callable. If execution of `Init` is requested, fabric will ensure that `Init` is invoked before any other function and is only invoked once.

- **初始化：**所有链码都需要包含一个`Init`用于初始化链码的函数。默认情况下，此功能从不执行。但是，您可以使用链码定义来请求该`Init` 函数可调用。如果`Init`请求执行，fabric将确保先调用`Init`函数，再进行其他函数，并且`Init`仅调用一次。

- **ESCC/VSCC Plugins:** The name of a custom endorsement or validation plugin to be used by this chaincode.

- **ESCC / VSCC插件：**此链码将使用的自定义认可或验证插件的名称。

The chaincode definition also includes the **Package Identifier**. This is a required parameter for each organization that wants to use the chaincode. The package ID does not need to be the same for all organizations. An organization can approve a chaincode definition without installing a chaincode package or including the identifier in the definition.

链码定义还包括**Package Identifier**。这是每个要使用链码的组织的必需参数。包ID不必对于所有组织都相同。组织可以批准链码定义，而无需安装链码包或在定义中包括标识符。

Each channel member that wants to use the chaincode needs to approve a chaincode definition for their organization. This approval needs to be submitted to the ordering service, after which it is distributed to all peers. This approval needs to be submitted by your **Organization Administrator**. After the approval transaction has been successfully submitted, the approved definition is stored in a collection that is available to all the peers of your organization. As a result you only need to approve a chaincode for your organization once, even if you have multiple peers.

每个想要使用链码的渠道成员都需要为其组织批准链码定义。该批准需要提交给订购服务，然后再分发给所有peer节点。该批准需要由您的**组织管理员**提交。成功提交批准交易后，批准的定义将存储在一个集合中，该集合可供组织的所有peer节点使用。因此，即使您有多个peer节点，您也只需要为组织批准一次链码。

![Lifecycle-approve](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-approve.png)

*An organization administrator from Org1 and Org2 approve the chaincode definition of MYCC for their organization. The chaincode definition includes the chaincode name, version, and the endorsement policy, among other fields. Since both organizations will use the chaincode to endorse transactions, the approved definitions for both organizations need to include the packageID.*

*Org1和Org2的组织管理员为他们的组织批准MYCC的链码定义。链码定义包括链码名称，版本和认可策略以及其他字段。由于两个组织都将使用链码来认可交易，因此两个组织的批准定义都需要包含packageID。*

### Step Four: Commit the chaincode definition to the channel

Once a sufficient number of channel members have approved a chaincode definition, one organization can commit the definition to the channel. You can use the `checkcommitreadiness` command to check whether committing the chaincode definition should be successful based on which channel members have approved a definition before committing it to the channel using the peer CLI. The commit transaction proposal is first sent to the peers of channel members, who query the chaincode definition approved for their organizations and endorse the definition if their organization has approved it. The transaction is then submitted to the ordering service, which then commits the chaincode definition to the channel. The commit definition transaction needs to be submitted as the **Organization** **Administrator**.

一旦足够数量的渠道成员批准了链码定义，则一个组织可以将定义提交给渠道。您可以使用 `checkcommitreadiness`命令检查是否能成功提交链码定义，检查的方式基于**（所有、大多数？）**通道成员有没有批准，然后再使用peer节点的CLI将其提交给通道。首先将提交交易提案发送给通道成员的peer节点，这些节点查询其组织批准的链码定义，并在组织批准的前提下背书该定义。然后，将交易提交给排序服务，然后排序服务将链码定义提交给通道。提交定义事务需要以**组织管理员的**身份提交。

The number of organizations that need to approve a definition before it can be successfully committed to the channel is governed by the `Channel/Application/LifecycleEndorsement` policy. By default, this policy requires that a majority of organizations in the channel endorse the transaction. The LifecycleEndorsement policy is separate from the chaincode endorsement policy. For example, even if a chaincode endorsement policy only requires signatures from one or two organizations, a majority of channel members still need to approve the chaincode definition according to the default policy. When committing a channel definition, you need to target enough peer organizations in the channel to satisfy your LifecycleEndorsement policy. You can learn more about the Fabric chaincode lifecycle policies in the [Policies concept topic](https://hyperledger-fabric.readthedocs.io/en/release-2.0/policies/policies.html).

在将定义成功提交到通道之前，需要批准组织的数量由该 `Channel/Application/LifecycleEndorsement`策略控制。默认情况下，此策略要求通道中的大多数组织都背书该交易。LifecycleEndorsement策略与链码背书策略是分开的。例如，即使链码背书策略仅需要一个或两个组织的签名，大多数渠道成员仍需要根据默认策略（生命周期背书策略）批准链码定义。提交通道定义时，您需要在通道中找到足够多的peer节点组织，以满足您的LifecycleEndorsement策略。您可以在“ [策略”概念主题中](https://hyperledger-fabric.readthedocs.io/en/release-2.0/policies/policies.html)了解有关Fabric链码生命周期策略的更多信息。

You can also set the `Channel/Application/LifecycleEndorsement` policy to be a signature policy and explicitly specify the set of organizations on the channel that can approve a chaincode definition. This allows you to create a channel where a select number of organizations act as chaincode administrators and govern the business logic used by the channel. You can also use a signature policy if your channel has a large number Idemix organizations, which cannot approve chaincode definitions or endorse chaincode and may prevent the channel from reaching a majority as a result.

您还可以将`Channel/Application/LifecycleEndorsement`策略设置为签名策略，并在通道上显式指定可以批准链码定义的组织集。这使您可以创建一个通道，由一定数量的组织充当链码管理员，并管理该通道使用的业务逻辑。如果您的频道有大量的Idemix组织，它们不能批准链码定义或背书链码，并且可能阻止通道成员占多数，您也可以使用签名策略。

![Lifecycle-commit](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-commit.png)

*One organization administrator from Org1 or Org2 commits the chaincode definition to the channel. The definition on the channel does not include the packageID.*

*来自Org1或Org2的一位组织管理员将链码定义提交给通道。通道上的定义不包括packageID。*

An organization can approve a chaincode definition without installing the chaincode package. If an organization does not need to use the chaincode, they can approve a chaincode definition without a package identifier to ensure that the Lifecycle Endorsement policy is satisfied.

组织可以批准链码定义而不安装链码包。如果组织不需要使用链码，则可以批准不带包标识符的链码定义，以确保满足“生命周期认可”策略。

After the chaincode definition has been committed to the channel, the chaincode container will launch on all of the peers where the chaincode has been installed, allowing channel members to start using the chaincode. It may take a few minutes for the chaincode container to start. You can use the chaincode definition to require the invocation of the `Init` function to initialize the chaincode. If the invocation of the `Init` function is requested, the first invoke of the chaincode must be a call to the `Init` function. The invoke of the `Init` function is subject to the chaincode endorsement policy.

在将chaincode定义提交到通道后，链码容器将在已安装chaincode的所有peer节点上启动，从而允许通道成员开始使用chaincode。启动chaincode容器可能需要几分钟。您可以使用链码定义来要求调用`Init`函数来初始化链码。如果`Init`请求调用函数，则链码的第一次调用必须是对该`Init`函数的调用。该`Init` 功能的调用受链码背书政策的约束。

![Lifecycle-start](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-start.png)

*Once MYCC is defined on the channel, Org1 and Org2 can start using the chaincode. The first invoke of the chaincode on each peer starts the chaincode container on that peer.*

*一旦在通道上定义了MYCC，Org1和Org2就可以开始使用链码了。每个对等点上的链码的第一次调用都会在该对等点上启动链码容器。*

## Upgrade a chaincode

You can upgrade a chaincode using the same Fabric lifecycle process as you used to install and start the chainocode. You can upgrade the chaincode binaries, or only update the chaincode policies. Follow these steps to upgrade a chaincode:

您可以使用与安装和启动Chainocode相同的Fabric生命周期过程来升级Chaincode。您可以升级链码二进制文件，或仅更新链码策略。请按照以下步骤升级链码：

1. **Repackage the chaincode:** You only need to complete this step if you are upgrading the chaincode binaries.

   **重新打包链码：**仅在升级链码二进制文件时才需要完成此步骤。

![Lifecycle-upgrade-package](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-upgrade-package.png)

*Org1 and Org2 upgrade the chaincode binaries and repackage the chaincode. Both organizations use a different package label.*

*Org1和Org2升级链码二进制文件并重新打包链码。两家公司使用不同的包装标签。*

2. **Install the new chaincode package on your peers:** Once again, you only need to complete this step if you are upgrading the chaincode binaries. Installing the new chaincode package will generate a package ID, which you will need to pass to the new chaincode definition. You also need to change the chaincode version, which is used by the lifecycle process to track if the chaincode binaries have been upgraded. 

   **在对等方上安装新的chaincode软件包：**再次，如果要升级chaincode二进制文件，则仅需要完成此步骤。安装新的chaincode软件包将生成一个软件包ID，您需要将其传递给新的chaincode定义。您还需要更改链码版本，生命周期流程将使用它来跟踪链码二进制文件是否已升级。

![Lifecycle-upgrade-install](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-upgrade-install.png)

*Org1 and Org2 install the new package on their peers. The installation creates a new packageID.*

*Org1和Org2在同级上安装新软件包。安装将创建一个新的packageID。*

3. **Approve a new chaincode definition:** If you are upgrading the chaincode binaries, you need to update the chaincode version and the package ID in the chaincode definition. You can also update your chaincode endorsement policy without having to repackage your chaincode binaries. Channel members simply need to approve a definition with the new policy. The new definition needs to increment the **sequence** variable in the definition by one.

   **批准新的链码定义：**如果要升级链码二进制文件，则需要更新链码定义中的链码版本和程序包ID。您也可以更新链码认可策略，而不必重新打包链码二进制文件。通道成员只需要批准新政策的定义。新定义需要将定义中的**序列（sequence）**变量加1。

![Lifecycle-upgrade-approve](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-upgrade-approve.png)

*Organization administrators from Org1 and Org2 approve the new chaincode definition for their respective organizations. The new definition references the new packageID and changes the chaincode version. Since this is the first update of the chaincode, the sequence is incremented from one to two.*

*Org1和Org2的组织管理员为各自的组织批准新的链码定义。新定义引用了新的packageID并更改了链码版本。由于这是链码的第一次更新，因此序列从一递增到二。*

4. **Commit the definition to the channel:** When a sufficient number of channel members have approved the new chaincode definition, one organization can commit the new definition to upgrade the chaincode definition to the channel. There is no separate upgrade command as part of the lifecycle process.

   **将定义提交给渠道：**当足够数量的通道成员批准了新的链码定义时，一个组织可以提交新定义以将链码定义升级到通道。作为生命周期过程的一部分，没有单独的升级命令。

![Lifecycle-upgrade-commit](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-upgrade-commit.png)

​			*An organization administrator from Org1 or Org2 commits the new chaincode definition to the channel.*

​			*来自Org1或Org2的组织管理员将新的链码定义提交到通道。*

After you commit the chaincode definition, a new chaincode container will launch with the code from the upgraded chaincode binaries. If you requested the execution of the `Init` function in the chaincode definition, you need to initialize the upgraded chaincode by invoking the `Init` function again after the new definition is successfully committed. If you updated the chaincode definition without changing the chaincode version, the chaincode container will remain the same and you do not need to invoke `Init` function. 

提交链码定义后，将使用升级的链码二进制文件中的代码启动新的链码容器。如果您在链码定义中要求执行`Init`函数，则需要在成功提交新定义后再次调用`Init`函数来初始化升级的链码。如果在不更改链码版本的情况下更新了链码定义，则链码容器将保持不变，并且无需调用`Init`函数。

![Lifecycle-upgrade-start](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-upgrade-start.png)

*Once the new definition has been committed to the channel, each peer will automatically start the new chaincode container.*

*将新定义提交给通道后，每个peer节点将自动启动新的chaincode容器。*

The Fabric chaincode lifecycle uses the **sequence** in the chaincode definition to keep track of upgrades. All channel members need to increment the sequence number by one and approve a new definition to upgrade the chaincode. The version parameter is used to track the chaincode binaries, and needs to be changed only when you upgrade the chaincode binaries.

Fabric链码生命周期使用链码定义中的**序列**来跟踪升级。所有通道成员都需要将序列号增加一个，并批准新的定义以升级链码。版本参数用于跟踪链码二进制文件，仅在升级链码二进制文件时才需要更改。

## Deployment scenarios

The following examples illustrate how you can use the Fabric chaincode lifecycle to manage channels and chaincode.

以下示例说明了如何使用Fabric链码生命周期来管理通道和链码。

### Joining a channel

A new organization can join a channel with a chaincode already defined, and start using the chaincode after installing the chaincode package and approving the chaincode definition that has already been committed to the channel.

新的组织可以使用已定义的链码加入频道，并在安装链码包并批准已经提交给该频道的链码定义后开始使用链码。



![Lifecycle-join-approve](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-join-approve.png)

*Org3 joins the channel and approves the same chaincode definition that was previously committed to the channel by Org1 and Org2.*

*Org3加入频道并批准先前由Org1和Org2提交给频道的相同链码定义。*

After approving the chaincode definition, the new organization can start using the chaincode after the package has been installed on their peers. The definition does not need to be committed again. If the endorsement policy is set the default policy that requires endorsements from a majority of channel members, then the endorsement policy will be updated automatically to include the new organization.

批准链码定义后，新组织可以在将软件包安装到peer节点后开始使用链码。该定义不需要再次提交。如果将背书策略设置为默认策略（需要大多数渠道成员的背书），则背书策略将自动更新以包括新组织。

![Lifecycle-join-start](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-join-start.png)

*The chaincode container will start after the first invoke of the chaincode on the Org3 peer.*

*链码容器将在Org3对等体上首次调用链码后启动。*

### Updating an endorsement policy

You can use the chaincode definition to update an endorsement policy without having to repackage or re-install the chaincode. Channel members can approve a chaincode definition with a new endorsement policy and commit it to the channel.

您可以使用链码定义来更新背书策略，而不必重新打包或重新安装链码。通道成员可以批准带有新认可策略的链码定义，并将其提交给通道。

![Lifecycle-endorsement-approve](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-endorsement-approve.png)

*Org1, Org2, and Org3 approve a new endorsement policy requiring that all three organizations endorse a transaction. They increment the definition sequence from one to two, but do not need to update the chaincode version.*

*Org1，Org2和Org3批准了一项新的认可策略，要求所有三个组织都认可一项交易。它们将定义序列从一递增到两，但不需要更新链码版本。*

The new endorsement policy will take effect after the new definition is committed to the channel. Channel members do not have to restart the chaincode container by invoking the chaincode or executing the `Init` function in order to update the endorsement policy.

新的认可政策将在将新定义提交给渠道后生效。通道成员不必通过调用链码来重新启动链码容器或执行`Init`函数来更新背书策略。

![Lifecycle-endorsement-commit](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-endorsement-commit.png)

*One organization commits the new chaincode definition to the channel to update the endorsement policy.*

*一个组织将新的链码定义提交给渠道以更新认可策略。*

### Approving a definition without installing the chaincode

You can approve a chaincode definition without installing the chaincode package. This allows you to endorse a chaincode definition before it is committed to the channel, even if you do not want to use the chaincode to endorse transactions or query the ledger. You need to approve the same parameters as other members of the channel, but not need to include the packageID as part of the chaincode definition.

您可以批准链码定义而不安装链码包。这使您可以在将链码定义提交到通道之前对其进行背书，即使您不想使用该链码对交易进行背书或查询分类帐。您需要批准与通道的其他成员相同的参数，但不需要将packageID包含在链码定义中。

![Lifecycle-no-package](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-no-package.png)

*Org3 does not install the chaincode package. As a result, they do not need to provide a packageID as part of chaincode definition. However, Org3 can still endorse the definition of MYCC that has been committed to the channel.*

*Org3不会安装chaincode软件包。结果，他们不需要提供packageID作为链码定义的一部分。但是，Org3仍然可以背书已提交给该频道的MYCC的定义。*

### One organization disagrees on the chaincode definition

An organization that does not approve a chaincode definition that has been committed to the channel cannot use the chaincode. Organizations that have either not approved a chaincode definition, or approved a different chaincode definition will not be able to execute the chaincode on their peers.

不批准已提交给渠道的链码定义的组织不能使用链码。未批准链码定义或批准其他链码定义的组织将无法在其peer节点上执行链码。

![Lifecycle-one-disagrees](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-one-disagrees.png)

*Org3 approves a chaincode definition with a different endorsement policy than Org1 and Org2. As a result, Org3 cannot use the MYCC chaincode on the channel. However, Org1 or Org2 can still get enough endorsements to commit the definition to the channel and use the chaincode. Transactions from the chaincode will still be added to the ledger and stored on the Org3 peer. However, the Org3 will not be able to endorse transactions.*

*Org3批准的链码定义具有与Org1和Org2不同的认可策略。结果，Org3无法在通道上使用MYCC链码。但是，Org1或Org2仍然可以获得足够的认可，以将定义提交到通道并使用链码。链码中的交易仍将添加到分类帐中并存储在Org3的peer节点中。但是，Org3无法背书交易。*

An organization can approve a new chaincode definition with any sequence number or version. This allows you to approve the definition that has been committed to the channel and start using the chaincode. You can also approve a new chaincode definition in order to correct any mistakes made in the process of approving or packaging a chaincode.

组织可以批准具有任何序列号或版本的新链码定义。这使您可以批准已提交给通道的定义并开始使用链码。您也可以批准新的链码定义，以更正在批准或打包链码过程中犯的任何错误。

### The channel does not agree on a chaincode definition

If the organizations on a channel do not agree on a chaincode definition, the definition cannot be committed to the channel. None of the channel members will be able to use the chaincode.

如果通道上的组织不同意链码定义，则无法将该定义提交给通道。任何频道成员都将无法使用链码。

![Lifecycle-majority-disagree](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-majority-disagree.png)

*Org1, Org2, and Org3 all approve different chaincode definitions. As a result, no member of the channel can get enough endorsements to commit a chaincode definition to the channel. No channel member will be able to use the chaincode.*

*Org1，Org2和Org3都认可不同的链码定义。结果，该频道的任何成员都无法获得足够的背书以将链码定义提交给该频道。任何频道成员都无法使用链码。*

### Organizations install different chaincode packages

Each organization can use a different packageID when they approve a chaincode definition. This allows channel members to install different chaincode binaries that use the same endorsement policy and read and write to data in the same chaincode namespace.

每个组织在批准链码定义时都可以使用不同的packageID。这允许通道成员安装使用相同认可策略的不同链码二进制文件，并在同一链码名称空间中读取和写入数据。

Organizations can use this capability to install smart contracts that contain business logic that is specific to their organization. Each organization’s smart contract could contain additional validation that the organization requires before their peers endorse a transaction. Each organization can also write code that helps integrate the smart contract with data from their existing systems. 

组织可以使用此功能来安装包含特定于组织的业务逻辑的智能合约。每个组织的智能合约都可以包含组织在其peer节点认可交易之前所需的其他验证。每个组织还可以编写代码，以帮助将智能合约与其现有系统中的数据集成在一起。

![Lifecycle-binaries](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-binaries.png)

*Org1 and Org2 each install versions of the MYCC chaincode containing business logic that is specific to their organization.*

*Org1和Org2各自安装的MYCC链码的版本中都包含特定于其组织的业务逻辑。*

### Creating multiple chaincodes using one package

You can use one chaincode package to create multiple chaincode instances on a channel by approving and committing multiple chaincode definitions. Each definition needs to specify a different chaincode name. This allows you to run multiple instances of a smart contract on a channel, but have the contract be subject to different endorsement policies.

您可以通过批准并提交多个链码定义，实现使用一个链码包在一个通道上创建多个链码实例。每个定义都需要指定一个不同的链码名称。这使您可以在一个通道上运行智能合约的多个实例，但合约得遵循不同的背书策略。

![Lifecycle-multiple](.\Chaincode_lifecycle_v2.0.assets\Lifecycle-multiple.png)

*Org1 and Org2 use the MYCC_1 chaincode package to approve and commit two different chaincode definitions. As a result, both peers have two chaincode containers running on their peers. MYCC1 has an endorsement policy of 1 out of 2, while MYCC2 has an endorsement policy of 2 out of 2.*

*Org1和Org2使用MYCC_1链码包来批准和提交两个不同的链码定义。两个peer节点都有两个在其peer节点上运行的chaincode容器。MYCC1的背书政策为2个节点有一个背书即可通过，而MYCC2的背书政策为必须两个一起背书才能通过。*

## Migrate to the new Fabric lifecycle

For information about migrating to the new lifecycle, check out [Considerations for getting to v2.0](https://hyperledger-fabric.readthedocs.io/en/release-2.0/upgrade_to_newest_version.html#chaincode-lifecycle).

If you need to update your channel configurations to enable the new lifecycle, check out [Enabling the new chaincode lifecycle](https://hyperledger-fabric.readthedocs.io/en/release-2.0/enable_cc_lifecycle.html).

有关迁移到新生命周期的信息，请查看[有关升级到v2.0的注意事项](https://hyperledger-fabric.readthedocs.io/en/release-2.0/upgrade_to_newest_version.html#chaincode-lifecycle)。

如果您需要更新通道配置以启用新的生命周期，请查看“ [启用新的链码生命周期”](https://hyperledger-fabric.readthedocs.io/en/release-2.0/enable_cc_lifecycle.html)。

