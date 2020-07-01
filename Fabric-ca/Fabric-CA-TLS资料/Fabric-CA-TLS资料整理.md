>说明：
>
>本资料整理于两篇Fabric-CA官网的教程：<br>[Fabric CA Operations Guide](<https://hyperledger-fabric-ca.readthedocs.io/en/latest/operations_guide.html#setup-tls-ca>)<br>[CA Deployment steps](<https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/cadeploy.html>) 
>
>

# TLS资料整理

## From Fabric CA Operations Guide



### 部署TLS CA 服务

A TLS CA is used to issue TLS certificates. These certificates are required in order to secure the communication between various processes.

TLS CA 被用于签发TLS证书。这些证书将用于建立不同进程之间安全可靠的通讯。

In order to simplify this example, all organizations will use the same TLS CA and TLS mutual authentication is disabled.

出于简化例子的原因，所有组织都将使用同一个TLS CA，并且禁用TLS的互相认证功能。

> Note:
>
> In a production environment, you will probably use your organization’s CA to get TLS certificates. You will have to transfer out-of-band your CA’s certificate with organizations that will validate your TLS certificates. Thus, unlike this example, each organization would have its own TLS CA.
>
> 在生产环境，你可能会使用组织的CA来获得TLS证书。你必须将你的CA证书转移到将验证您的TLS证书组织之外，因此，与本例不同，**生产环境中每个组织中都将拥有自己的TLS CA。**

A docker service, such as the one below can be used to a launch a Fabric TLS CA container.

如下部署的docker服务可以被用于启动Fabric TLS CA

```yaml
ca-tls:
  container_name: ca-tls
  image: hyperledger/fabric-ca
  command: sh -c 'fabric-ca-server start -d -b tls-ca-admin:tls-ca-adminpw --port 7052'
  environment:
    - FABRIC_CA_SERVER_HOME=/tmp/hyperledger/fabric-ca/crypto
    - FABRIC_CA_SERVER_TLS_ENABLED=true
    - FABRIC_CA_SERVER_CSR_CN=ca-tls
    - FABRIC_CA_SERVER_CSR_HOSTS=0.0.0.0 # HOSTS，根CA启动不需要这个环境变量（Maple注）
    - FABRIC_CA_SERVER_DEBUG=true
  volumes:
    - /tmp/hyperledger/tls/ca:/tmp/hyperledger/fabric-ca
  networks:
    - fabric-ca
  ports:
    - 7052:7052
```

This container can be started using the following docker command.

```shell
docker-compose up ca-tls # 这个容器通过这个命令来启动
```

On a successful launch of the container, you will see the following line in the CA container’s log.

在成功启动的容器日志中你将看到如下一行日志。

```yam
[INFO] Listening on https://0.0.0.0:7052
```

At this point the TLS CA server is on a listening on a secure socket, and can start issuing TLS certificates.

这时，TLS CA 服务正在一个安全的socket上监听，并且可以开始签发TLS证书。



### Enroll TLS CA’s Admin / 登录TLS CA的管理身份

Before you can start using the CA client, you must acquire the signing certificate for the CA’s TLS certificate. This is a required step before you can connect using TLS.

在你使用CA 客户端之前，你必须获取到CA’s TLS证书的签名证书。如下是你在连接TLS之前需要完成的步骤。

In our example, you would need to acquire the file located at`/tmp/hyperledger/tls-ca/crypto/ca-cert.pem`on the machine running the TLS CA server and copy this file over to the host where you will be running the CA client binary. This certificate, also known as the TLS CA’s signing certificate is going to be used to validate the TLS certificate of the CA. Once the certificate has been copied over to the CA client’s host machine, you can start issuing commands using the CA.

在本例中，你需要在运行TLS CA服务的`/tmp/hyperledger/tls-ca/crypto/ca-cert.pem`(FABRIC_CA_SERVER_HOME)路径下获取证书，并将该文件拷贝到你需要运行CA客户端程序的机器当中。这个证书，也被叫做TLS CA的签名证书，将被用与CA的TLS证书。一旦该证书复制到CA客户端主机，就可以开始使用CA发出命令。

The TLS CA’s signing certificate will need to be available on each host that will run commands against the TLS CA.

这个TLS CA签名证书将需要在对TLS CA运行命令的每个主机上可用。

The TLS CA server was started with a bootstrap identity which has full admin privileges for the server. One of the key abilities of the admin is the ability to register new identities. The administrator for this CA will use the Fabric CA client to register four new identities with the CA, one for each peer and one for the orderer. These identities will be used to get TLS certificates for peers and orderers.

启动TLS CA服务器时使用的引导身份(boot 身份)具有该服务器的完全管理权限。管理员的关键能力之一是注册新身份的能力。此CA的管理员将使用Fabric CA客户端向CA注册四个新身份，一个用于每个peer对等点，一个用于Orderer节点。这些身份将用于为peer对等点和Orderer节点获取TLS证书。

You will issue the commands below to enroll the TLS CA admin and then register identities. We assume the trusted root certificate for the TLS CA has been copied to `/tmp/hyperledger/tls-ca/crypto/tls-ca-cert.pem` on all host machines that will communicate with this CA via the fabric-ca-client.

您将发出以下命令来注册TLS CA管理员，然后注册身份。我们假设已将TLS CA的可信根证书复制到所有将通过fabric-ca-client程序与该CA进行通信的主机的`/tmp/hyperledger/tls-ca/crypto/tls-ca-cert.pem` 位置。

```shell
export FABRIC_CA_CLIENT_TLS_CERTFILES=/tmp/hyperledger/tls-ca/crypto/tls-ca-cert.pem
export FABRIC_CA_CLIENT_HOME=/tmp/hyperledger/tls-ca/admin
fabric-ca-client enroll -d -u https://tls-ca-admin:tls-ca-adminpw@0.0.0.0:7052
fabric-ca-client register -d --id.name peer1-org1 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name peer2-org1 --id.secret peer2PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name peer1-org2 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name peer2-org2 --id.secret peer2PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name orderer1-org0 --id.secret ordererPW --id.type orderer -u https://0.0.0.0:7052
```

> Note:
>
> If the path of the environment variable FABRIC_CA_CLIENT_TLS_CERTFILES is not an absolute path, it will be parsed as relative to the client’s home directory.
>
> 如果环境变量中的`FABRIC_CA_CLIENT_TLS_CERTFILES `不是一个绝对路径，他将解析为相对于`FABRIC_CA_CLIENT_HOME`的相对路径。

With the identities registered on the TLS CA, we can move forward to setting up the each organization’s network. Anytime we need to get TLS certificates for a node in an organization, we will refer to this CA.

有了在TLS CA上注册的身份，我们可以继续设置每个组织的网络。当我们需要为组织中的某个节点获取TLS证书时，我们将引用这个CA。

> Maple-Chan备注：
>
> 原文后续的内容就是通过已有的TLS CA来注册、enroll身份、部署服务。
>
> 在用TLS CA 服务启动之后，通过enroll TLS CA的来生成TLS证书。之后需要用到`FABRIC_CA_CLIENT_TLS_CERTFILES`或者通过--tls.certfiles命令参数来进行指定TLS证书的位置，来进行注册和enroll一个身份获取证书。



----



## From CA Deployment steps

> 文章连接在本文件最前面

### CA部署概念

本段是**部署CA的大致流程** - 与TLS关联不大

Before deploying a Fabric CA server, you need to understand the role of the Fabric CA client. While you can use the Fabric SDKs to interact with your CA, it is recommended that you **use the Fabric CA client to register and enroll node admin identities**. The instructions provided in this topic assume a single Fabric CA client is being used. Registering an identity, or user, is the process by which the enroll id and secret is added to the CA database “user registry”. If you are using LDAP server for your user registry, then the register step is not required because the identities already exist in the LDAP database. After a user is registered you can use the Fabric CA client to “enroll” the identity which is the process that generates the certificates the identity needs to transact as part of the organization. When you submit an enrollment request, the private and public keys are first generated locally by the Fabric CA client, and then the public key is sent to the CA which returns an encoded “signed certificate”.

在部署Fabric CA服务器之前，您需要了解Fabric CA客户端的角色。虽然可以使用Fabric sdk与CA进行交互，但建议使用Fabric CA客户端注册和注册节点管理身份。本主题中提供的说明假设使用的是单个Fabric CA客户端。注册身份或用户是将注册id和secret添加到CA数据库用户注册中心的过程。如果您使用LDAP服务器作为用户注册中心，则不需要注册步骤，因为LDAP数据库中已经存在标识。在用户注册之后，您可以使用Fabric CA客户端来“enroll”身份，“enroll”身份就是是生成证书的过程，身份需要作为组织的一部分进行交易。当您提交注册请求时，Fabric CA客户端首先在本地生成私钥和公钥，然后将公钥发送给CA, CA将返回经过编码的签名证书。

Because you will use a single CA client to submit register and enrollment requests to multiple CAs, certificate management is critically important when using the CA client. A best practice therefore is to create sub-folders for each CA server that the CA client will interact with, to store the generated certificates.

由于您将使用单个CA客户端向多个CA提交注册和登记请求，因此在使用CA客户端时，证书管理非常重要。因此，最佳实践是在与CA客户端将交互的每个CA服务器中创建子该客户端的文件夹，以存储生成的证书。



**TLS相关内容。**

Because TLS communications are enabled on a production network, the TLS CA for the organization is responsible for generating certificates that secure communications between all nodes in the organization. Therefore, every time the Fabric CA client transacts with a CA server in that organization, it needs to provide the TLS CA “root certificate” to secure the client-server communication. For example, when the Fabric CA client issues a register or enroll request to the CA server, the client request includes that root certificate to perform an SSL handshake. The TLS CA root certificate, named `ca-cert.pem`, is generated on the TLS CA after TLS is enabled in the server config .yaml file. To enable TLS communications for your CA client, you need a `tls-root-cert` sub-folder to store the root certificate. Later in this topic, we will copy the root certificate into this folder.

由于在生产网络上启用了TLS通信，所以组织的TLS CA负责生成证书，以保护组织中所有节点之间的通信。因此，每当Fabric CA客户端与该组织中的CA服务器进行交易时，它都需要提供TLS CA根证书来保护客户端-服务器通信。例如，当Fabric CA客户端向CA服务器发出注册或注册请求时，客户端请求需要包括用于执行SSL握手的根证书。TLS CA根证书，命名为`ca-cert.pem`,在服务器配置.yaml文件中启用TLS之后，由TLS CA上生成。要为您的CA客户端启用TLS通信，您需要一个`tls -root-cert`子文件夹来存储这个根证书。在本主题的后面，我们将把根证书复制到这个文件夹中。

**Important:** If your Fabric CA client will transact with CAs from multiple organizations that are secured by different TLS servers, then you would need to either create different `tls-root-cert` folders to hold the TLS CA root certificate for each organization or simply name them differently inside the folder to differentiate them. Since our Fabric CA client will only be transacting with CA servers in the same organization, all of which are secured by the same TLS CA, we will only have a single root certificate in this folder.

**重要**:如果你的Fabric-CA客户中与之交互的CAs是，由不同的TLS服务保证安全的多个组织，那么你就需要创建不同的`tls-root-cert`文件夹来保存TLS CA 根证书通过不同的组织来划分，或者在文件夹中以简单的命名来区分他们。由于我们的Fabric CA客户端将只与同一组织中的CA服务器进行事务处理，所有这些服务器都由相同的TLS CA保护，因此在此文件夹中只有一个根证书。



一些可以定义的环境变量：

>+ `FABRIC_CA_CLIENT_HOME` - Specify the fully qualified path to where Fabric CA client binary resides.
>
>  指定Fabric-CA客户端程序所在的位置
>
>+ `FABRIC_CA_CLIENT_TLS_CERTFILES` - Specify the location and name of the TLS CA root certificate.
>
>  指定TLS CA 根证书所在的位置和名字。
>
>+ `FABRIC_CA_CLIENT_MSPDIR` - While you can use this environment variable to specify the name of the folder where the certificates are located, because the client communicates with multiple CAs, *a better option is to explicitly pass the --mspdir flag on the register and enroll commands to specify the location.* If not specified on the command, the location defaults to `$FABRIC_CA_CLIENT_HOME/msp` which will be problematic if the Fabric CA client transacts with multiple CA servers in the organization.
>
>  当你使用这个环境变量，可以指定证书生成的位置，但是最好通过`--mspdir`来进行显示的指定路径，因为可能会和多个CA进行通信。如果不指定，那么将会保存在 `$FABRIC_CA_CLIENT_HOME/msp` ，放在这个默认的位置在一个客户端与多个CA服务进行通讯时会出现问题。

> TIPS：
>
> The first time you issue an `enroll` command from the CA client, if the `fabric-ca-client-config.yaml` does not already exist in the`fabric-ca-client-config.yaml` it is generated. When you customize the values in this file, they are used automatically by the CA client and do not have to be passed on the command line on a subsequent `enroll` command.
>
> 第一次使用enroll命令进行生成证书的时候，如果`fabric-ca-client-config.yaml`不存在与`fabric-ca-client-config.yaml`目录，他将会自动生成。当你在这个文件中自定义一些值，他们会在CA client自动的被使用，不需要再执行一边enroll命令。【根据后面的实验，还需要执行start的命令】。



### What order should I deploy the CAs?

1. **Deploy the TLS CA**

   Because TLS communication is required in a Production network, TLS must be enabled on each CA, peer, and ordering node. While the example configuration in the CA Operations Guide shares a single TLS CA across all organizations, the recommended configuration for production is to deploy a TLS CA for each organization. The TLS CA issues the TLS certificates that secure communications between all the nodes on the network. Therefore, it needs to be deployed first to generate the TLS certificates for the TLS handshake that occurs between the nodes.

   由于生产网络中需要TLS通信，所以必须在每个CA、Peer节点和Orderer节点上启用TLS。虽然CA操作指南中的示例配置在所有组织中共享一个TLS CA，但生产环境的推荐配置是为每个组织部署一个TLS CA。TLS CA颁发TLS证书，以保护网络上所有节点之间的通信。因此，首先需要部署它来为节点之间发生的TLS握手生成TLS证书。

2. **Deploy the organization CA**

   This is the organization  identity enrollment CA and is used to register and enroll the identities that will participate in the network from this organization.

   这是组织的身份登记CA，用于注册和登记将从该组织参与网络的身份。

3. **Deploy the intermediate CA (Optional)**

   If you decide to include an intermediate CA in your network, the intermediate CA’s parent server (the associated root CA) must be deployed before any intermediate CAs.

   如果您决定在您的网络中包含一个中间CA，那么中间CA的父服务器(相关的根CA)必须在任何中间CA之前部署。



### Deploy the TLS CA

Regardless of whether you are setting up a TLS CA, an organization CA or an intermediate CA, the process follows the same overall steps. The differences will be in the modifications you make to the CA server configuration .yaml file. The following steps provide an overview of the process:

无论您是设置TLS CA、组织CA还是中间CA，整个过程都遵循相同的步骤。不同之处在于对CA服务器配置的.yaml文件的修改。以下步骤提供了该过程的概述：

- Step one: Initialize the CA server
- Step two: Modify the CA server configuration
- Step three: Delete the CA server certificates
- Step four: Start the CA server
- Step five: Enroll bootstrap user with TLS CA

When you deploy any node, you have three options for your TLS configuration:

在部署任何节点时，TLS配置有三个选项：

- No TLS. *Not recommended for a production network.*

  不用TLS，生产环境不推荐

- Server-side TLS.

  服务端的TLS

- Mutual TLS.

  双向的TLS

This process will configure a CA with server-side TLS enabled which is recommended for production networks. Mutual TLS is disabled by default. If you need to use mutual TLS, refer to the [TLS configuration settings](https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/ca-config.html#tls).

此过程将配置一个启用了服务器端TLS的CA，建议用于生产网络。默认情况下禁用互TLS。如果需要使用双向TLS，请参考[TLS配置设置](https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/ca-config.html#tls)。



#### Before you begin

You should have already downloaded and copied the Fabric CA server binary `fabric-ca-server` to a clean directory on your machine. For purposes of these instructions, we put the binary in its own folder named `fabric-ca-server-tls`.

您应该已经下载并将Fabric CA服务器二进制`fabric-ca-server`复制到您机器上的一个干净目录中。出于这些说明的目的，我们将二进制文件放在它自己的文件夹中，命名为fabric-ca-server-tls。

Copy the `fabric-ca-server` binary into this folder.

将fabric-ca-server二进制文件复制到此文件夹中。



#### Initialize the TLS CA server

The first step to deploy a CA server is to “initialize” it. Run the following CA server CLI command to initialize the server by specifying the admin user id and password for the CA:

部署CA服务器的第一步是初始化它。运行以下CA服务器CLI命令，通过指定CA的管理用户id和密码来初始化服务器。

```shell
./fabric-ca-server init -b <ADMIN_USER>:<ADMIN_PWD>
```

例如:

```shell
cd fabric-ca-server-tls
./fabric-ca-server init -b tls-admin:tls-adminpw
```

The `-b` (bootstrap identity) flag bootstraps the admin username and password to the CA server which effectively “registers” the CA admin user with the server for you, so an explicit Fabric CA client CLI `register` command is not required for the bootstrapped user. All CA users need to be “registered” and then “enrolled” with the CA, except for this CA admin identity which is implicitly registered by using the `-b` flag. The registration process inserts the user into the CA database. The `-b` option is not required for initialization when LDAP will be configured.

 -b (bootstrap identity)标志将管理用户名和密码引导到CA服务器，从而有效地为您向服务器注册CA管理用户，因此，引导用户不需要显式的使用`fabric-ca-client`的 register命令。所有CA用户都需要注册，然后在CA中登记，但这个CA管理员身份是通过使用-b标志隐式注册的。注册过程将用户插入CA数据库。在配置LDAP时，初始化不需要-b选项。

**Note: This example is for illustration purposes only. Obviously, in a production environment you would never use tls-admin and tls-adminpw as the bootstrap username and password.** Be sure that you record the admin id and password that you specify. They are required later when you issue register and enroll commands against the CA. It can help to use a meaningful id to differentiate which server you are transacting with and follow secure password practices.

注意:本例仅供演示使用。显然，在生产环境中，您永远不会使用tls-admin和tls-adminpw作为引导用户名和密码。确保您记录了所指定的管理员id和密码。稍后，当您对CA发出注册和注册命令时，它们是必需的。使用有意义的id来区分您正在处理的服务器并遵循安全的密码实践，将会很有帮助。

#### What does the CA server `init` command do?

The `init` command does not actually start the server but generates the required metadata if it does not already exist for the server:

init命令实际上并不启动服务器，但如果服务器不存在所需的元数据，则生成元数据。

- Sets the default the CA Home directory (referred to as `FABRIC_CA_HOME` in these instructions) to where the `fabric-ca-server init` command is run.

  将缺省CA Home 目录(在这些指令中称为`FABRIC_CA_HOME`)设置为运行FABRIC - CA -server init命令的位置。

- Generates the default configuration file `fabric-ca-server-config.yaml` that is used as a template for your server configuration in the `FABRIC_CA_HOME` directory. We refer to this file throughout these instructions as the “configuration .yaml” file.

  生成默认配置文件fabrica -ca-server-config.yaml。作为服务器配置模板的放在`FABRIC_CA_HOME`中。在这些指令中，我们将这个文件称为配置.yaml文件。

- Creates the TLS CA root signed certificate file `ca-cert.pem`, if it does not already exist in the CA Home directory. This is the **self-signed root certificate**, meaning it is generated and signed by the TLS CA itself and does not come from another source. This certificate is the public key that must be shared with all clients that want to transact with any node in the organization. When any client or node submits a transaction to another node, it must include this certificate as part of the transaction.

  如果TLS CA根签署的证书在CA主目录中还不存在，则创建 `ca-cert.pem`。这是自签名的根证书，这意味着它是由TLS CA本身生成并签名的，而不是来自其他CA。此证书是必须与希望与组织中的任何节点进行交易的所有客户端共享的公钥。当任何客户端或节点向另一个节点提交事务时，它必须包含此证书作为事务的一部分。

- Generates the CA server private key and stores it in the `FABRIC_CA_HOME` directory under `/msp/keystore`.

  生成CA服务的私钥，并将其存在 `FABRIC_CA_HOME` 目录的`/msp/keystore`的文件夹当中。

- Initializes a default SQLite database for the server although you can modify the database setting in the configuration .yaml file to use the supported database of your choice. Every time the server is started, it loads the data from this database. If you later switch to a different database such as PostgreSQL or MySQL, and the identities defined in the `registry.identites` section of the configuration .yaml file don’t exist in that database, they will be registered.

  初始化服务器的默认数据库是SQLite，不过您可以修改配置.yaml文件中的数据库设置，以您所选择的受支持的数据库。每次服务启动的时候，将从数据库加载数据。如果以后切换到不同的数据库，比如切换到PostgreSQL或MySQL后，定义在configuration .yaml文件中的`registry.identites`在数据库中不存在，那么它们将被注册。

- Bootstraps the CA server administrator, specified by the `-b` flag parameters `<ADMIN_USER>` and `<ADMIN_PWD>`, onto the server. When the CA server is subsequently started, the admin user is registered with the admin attributes provided in the configuration .yaml file `registry` section. If this CA will be used to register other users with any of those attributes, then the CA admin user needs to possess those attributes. In other words, the registrar must have the `hf.Registrar.Roles` attributes before it can register another identity with any of those attributes. Therefore, if this CA admin will be used to register the admin identity for an Intermediate CA, then this CA admin must have the `hf.IntermediateCA` set to `true` even though this may not be an intermediate CA server. The default settings already include these attributes.

  引导（自启动的）CA服务器管理员（由-b标志参数<ADMIN USER>和<ADMIN PWD>进行指定）到服务器上。在随后启动CA服务器时，管理用户将使用configuration .yaml文件的`registry`部分中提供的管理属性进行注册。如果此CA将用于注册其他具有这些属性的用户，则CA管理员用户需要拥有这些属性。换句话说，注册商必须有 `hf.Registrar.Roles`属性，然后才能用这些属性中的任何一个注册另一个身份。因此，如果此CA管理员将用于注册中间CA的管理员标识，则此CA管理员必须具有`hf.IntermediateCA`属性并设置为true，即使它可能不是一个中间CA服务器。默认设置已经包括这些属性。

**Important**: When you modify settings in the configuration .yaml file and restart the server, the **previously issued certificates are not replaced**. If you want the certificates to be regenerated when the server is started, you need to delete them and run the `fabric-ca-server start` command. For example, if you modify the `csr` values after you start the server, you need to delete the previously generated certificates, and then run the `fabric-ca-server start` command. Be aware though, that when you restart the CA server using the new signed certificate and private key, all previously issued certificates will no longer be able to authenticate with the CA.

重要提示:在修改配置.yaml文件中的设置并重新启动服务器时，不会替换以前颁发的证书。如果希望在服务器启动时重新生成证书，则需要删除证书并运行`fabric-ca-server start`命令。例如，如果在启动服务器后修改csr值，则需要删除以前生成的证书，然后运行fabrica -ca-server启动命令。但是要注意，当您使用新的签名证书和私钥重新启动CA服务器时，所有以前颁发的证书将不再能够使用CA进行身份验证。



#### Modify the TLS CA server configuration

Now that you have initialized your server, you can edit the generated `fabric-ca-server-config.yaml` file to modify the default configuration settings for your use case according to the [Checklist for a production CA server](https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/ca-config.html).

现在已经初始化了服务器，可以编辑生成的`fabric-ca-server-config.yaml`文件，根据[生产环境的CA服务器的参数列表](https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/ca-config.html)修改用例的默认配置设置。

At a minimum you should do the following:

你可能需要用到如下参数

- `port` - Enter the port that you want to use for this server. These instructions use `7054`, but you can choose your port.

  port属性，输入你想要为这个服务启动的端口，这些这里的指令使用的时7054，但是你可以根据你自己情况选择。

- `tls.enabled` - Recall that TLS is disabled in the default configuration file. Since this is a production server, enable it by setting this value to `true`. Setting this value to `true` causes the TLS signed certificate `tls-cert.pem` file to be generated when the server is started in the next step. The `tls-cert.pem` is the certificate the server will present to the client during a TLS handshake, which the client will then verify using the TLS CA’s `ca-cert.pem`.

  tls下的enabled属性，这个属性在默认的配置文件中时禁用的。在生产环境，需要把这个值设置为true。将此值设置为true将在下一步启动服务器时生成TLS签名的证书`tls-cert.pem`。`tls-cert.pem`是CA 服务需要在TLS握手期间呈现给客户端的证书。最后，客户端将使用TLS CA的`ca-cert.pem`证书进行验证。

- `ca.name` - Give the CA a name by editing the parameter, for example `tls-ca`.

  通过这个属性修改服务的名字。

- `csr.hosts` - Update this parameter to include this hostname and ip address where this server is running, if it is different than what is already in this file.

  更新此参数来指定此服务器正在运行的主机名和ip地址(如果它与此文件中已有的内容不同)。

- `signing.profiles.ca` - Since this is a TLS CA that will not issue CA certificates, the `ca` profiles section can be removed. The `signing.profiles` block should only contain `tls` profile.

  signing-profiles-ca的配置，由于这是一个TLS CA, 他将不会签发CA证书，ca这个描述可以删除。 `signing.profiles` 这个模块应该只包含`tls`信息。

- `operations.listenAddress:` - In the unlikely case that there is another node running on this host and port, then you need to update this parameter to use a different port.

  在此主机和端口上运行了另一个节点（虽然这个情况在生产环境不太可能），您需要更新此参数以使用不同的端口。



#### Delete the TLS CA server certificates

Before starting the server, if you modified any of the values in the `csr` block of the configuration .yaml file, you need to delete the `fabric-ca-server-tls/ca-cert.pem` file and the entire `fabric-ca-server-tls/msp` folder. These certificates will be re-generated when you start the CA server in the next step.

在启动服务之前，如果你修改了文件中的任何值，你需要删除 `fabric-ca-server-tls/ca-cert.pem`文件和整个 `fabric-ca-server-tls/msp` 文件夹。当你在下一步进行启动CA服务的时候，这些内容将会重新生成。



#### Start the TLS CA server

Run the following command to start the CA server:

运行如下命令来启动CA服务：

```shell
./fabric-ca-server start
```

在成功启动的容器日志中你将看到如下一行日志。

```yam
[INFO] Listening on https://0.0.0.0:7052
```

Because you have enabled TLS communications, notice that the TLS signed certificate `tls-cert.pem` file is generated under the `FABRIC_CA_HOME` location.

由于你启用了TLS通信，TLS签名的证书`tls-cert.pem`文件将在`FABRIC_CA_HOME` 文件夹下生成。

> **Tip:** The CA `ADMIN_USER` and `ADMIN_PWD` that were set on the `init` command cannot be overridden with the `-b` flag on this `start` command. When you need to modify the CA admin password, use the Fabric CA client [identity](https://hyperledger-fabric-ca.readthedocs.io/en/latest/clientcli.html#identity-command) command.
>
> 用init命令设置的CA的`ADMIN_USER` 和 `ADMIN_PWD`，不能通过start命令的-b标志覆盖。当你需修改CA admin的密码时，需要使用`fabric-ca-client`的identity命令。

**Optional flags**:

**可选标志参数：**

- `-d` - If you want to run the server in DEBUG mode which facilitates problem diagnosis, you can include the `-d` flag on the start command. However, in general it is not recommended to run a server with debug enabled as this will cause the server to perform slower.

  如果你想在服务器上运行DEBUG模式来加快问题的分析，你可以在start命令中通过设置-d来完成。然后通常情况下不建议使用DEBUG模式进行运行服务（会降低服务的性能，减慢速度）。

- `-p` - If you want the server to run on a port different than what is specified in the configuration .yaml file, you can override the existing port.

  如果你想运行一个不同于configuration .yaml文件中定义的端口，你可以使用 -p命令来指定CA服务的端口。





#### Enroll bootstrap user with TLS CA

用TLS CA来Enroll boot用户。

Now that your TLS CA is configured and before you can deploy any other nodes for your organization, you need to enroll the bootstrap (admin) user of the TLS CA. Since the CA server is up and running, instead of using the **Fabric CA server CLI commands** we now use the **Fabric CA client CLI commands** to submit an enrollment request to the server.

现在已经配置了TLS CA，在可以为组织部署任何其他节点之前，需要注册TLS CA的引导程序(管理员)用户。由于CA服务器已经启动并正在运行，所以我们现在使用Fabric CA客户端CLI命令（fabric-ca-client）向服务器提交注册请求，而不是使用Fabric CA服务器CLI命令。

Performed by using the Fabric CA client, the enrollment process is used to generate the certificate and private key pair which forms the node identity. You should have already setup the required folders in the [Fabric CA client](https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/cadeploy.html#fabric-ca-client) section.

通过使用Fabric CA客户端执行，登记（enroll）过程用于生成形成节点标识的证书和私钥对。

The folder structure that we are using for these Fabric CA client commands is:

用于客户端命令的文件结构如下所示：

```shell
fabric-ca-client
  └── tls-ca  # 存放由tls-ca签发的msp
  └── tls-root-cert # 存放tls证书，在进行客户端与服务端进行握手时用。
```

These folders are used by the Fabric CA client to:

Fabric CA客户端将使用这些文件夹作如下使用：

- Store the certificates that are issued when the Fabric CA client enroll command is run against the TLS CA server to enroll the TLS CA bootstrap identity. (**tls-ca** folder)

  (tls-ca文件夹) 存储Fabric CA客户端登记（enroll）命令在TLS CA服务器上运行时签发出的证书，以登记TLS CA引导身份。

- Know where the TLS CA root certificate resides that allows the Fabric CA client to communicate with the TLS CA server. (**tls-root-cert** folder)

  (tls-root-cert文件夹) 需要知道TLS CA根证书存放在何处，从而允许Fabric CA客户端与TLS CA服务器进行通信。



1. Copy the TLS CA root certificate file `fabric-ca-server-tls/ca-cert.pem`, that was generated when the TLS CA server was started, to the `fabric-ca-client/tls-root-cert/tls-ca-cert.pem` folder. Notice the file name is changed to `tls-ca-cert.pem` to make it clear this is the root certificate from the TLS CA. **Important:** This TLS CA root certificate will need to be available on each client system that will run commands against the TLS CA.

   拷贝TLS  CA 根证书文件`fabric-ca-server-tls/ca-cert.pem`（在TLS CA 服务启动的时候生成）到`fabric-ca-client/tls-root-cert/tls-ca-cert.pem` 文件夹。注意，为了标注清楚他是TLS CA的的证书，文件名由`ca-cert.pem`变成了`tls-ca-cert.pem`。重要提示，这个TLS CA 的根证书需要对所有需要向TLS CA运行命令的客户端系统保持有效。

   

2. The Fabric CA Client also needs to know where Fabric CA client binary is located. The `FABRIC_CA_CLIENT_HOME` environment variable is used to set the location.

   设置`FABRIC_CA_CLIENT_HOME` 来知道Fabric CA client二进制文件放在那儿。

   ```shell
   export FABRIC_CA_CLIENT_HOME=<FULLY-QUALIFIED-PATH-TO-FABRIC-CA-BINARY>
   ```

   For example, if you are in the `fabric-ca-client` folder you can use:

   ```shell
   export FABRIC_CA_CLIENT_HOME=$PWD
   ```

3. You are ready to use the Fabric CA client CLI to enroll the TLS CA admin user. Run the command:

   拷贝完证书，设置好环境变量你就可以进行使用Fabric CA client CLI来登记（enroll）TLS CA的管理员用户。

   ```shell
   ./fabric-ca-client enroll -d -u https://<ADMIN>:<ADMIN-PWD>@<CA-URL>:<PORT> --tls.certfiles <RELATIVE-PATH-TO-TLS-CERT> --enrollment.profile tls --csr.hosts '<CA_HOSTNAME>' --mspdir tls-ca/tlsadmin/msp
   ```



Replace: （命令中可以替换这些部分以适应与你自己的网络）

- `<ADMIN>` - with the TLS CA admin specified on the `init` command.

  在init命令中指定的TLS CA admin。

- `<ADMIN-PWD>` - with the TLS CA admin password specified on the `init` command.

  在init命令中指定的TLS CA admin的密码。

- `<CA-URL>` - with the hostname specified in the `csr` section of the TLS CA configuration .yaml file.

  在TLS CA配置文件的csr部分指定的hostname。

- `<PORT>` - with the port that the TLS CA is listening on.

  TLS CA 服务监听的端口。

- `<RELATIVE-PATH-TO-TLS-CERT>` - with the path and name of the root TLS certificate file that you copied from your TLS CA. This path is relative to `FABRIC_CA_CLIENT_HOME`. If you are following the folder structure in this tutorial it would be `tls-root-cert/tls-ca-cert.pem`.

  使用从TLS CA复制的根TLS证书文件的路径和名称。这是相对于`FABRIC_CA_CLIENT_HOME`的路径。如果您遵循本教程中的文件夹结构，那么它就是`tls-root-cert/tls-ca-cert.pem`.

- `<CA_HOSTNAME>` - with a comma-separated list of host names for which the certificate should be valid. If not specified, the default value from the `fabric-ca-client-config.yaml` is used. You can specify a wildcard for the domain. For example, when you include the flag `--csr.hosts 'host1,*.example.com'` it means that the hostname `host1` is recognized as well as any host from the `example.com` domain. These values are inserted into the generated certificate Subject Alternative Name (SAN) attribute. The value specified here corresponds to the `csr.hosts` parameter that you specified for the CA server.

  以逗号分隔的主机名列表，证书应该对这些主机名有效。如果未指定，则使用fabric-ca-client-config.yaml文件中的默认值。可以为域指定通配符。例如，当您包含标志`--csr.hosts 'host1，*.example.com'`时，这意味着主机名host1和来自example.com域的任何主机一样可以被识别。这个值被插入到生成的证书的Subject Alternative Name (SAN, 使用者备用名称) 属性中。这里指定的值对应于你在CA server指定的`csr.hosts`参数。

  

例如：

```shell
./fabric-ca-client enroll -d -u https://tls-admin:tls-adminpw@my-machine.example.com:7054 --tls.certfiles tls-root-cert/tls-ca-cert.pem --enrollment.profile tls --csr.hosts 'host1,*.example.com' --mspdir tls-ca/tlsadmin/msp
```



In this case, the `-d` parameter runs the client in DEBUG mode which is useful for debugging enrollment failures.

在这个实验中，通过`-d`参数运行client进入DEBUG模式，这样可以用于调试登记错误。

Notice the `--mspdir` flag is used on the command to designate where to store the TLS CA admin certificates that are generated by the enroll command.

注意，在命令中使用`--mspdir`标志来指定存储由登记(enroll)命令生成的TLS CA管理证书的位置。

##### TLS CA身份专有

The `--enrollment.profile tls` flag is specified because we are enrolling against the TLS CA. Use of this flag means that the enrollment is performed according to the `usage` and `expiry` settings of the TLS profile that is defined in the `signing` section of the configuration .yaml file. **Note:** If you removed the `signing.profiles.ca` block from the TLS CA configuration .yaml file, you could omit the `--enrollment.profile tls` flag.

指定这个`--enrollment.profile tls` 标志是因为我们要登记（enroll）的TLS CA身份。使用此标志意味着登记（enrollment）将根据配置的.yaml文件的`signing`部分中定义的`usage` and `expiry` 设置执行。如果你移除了`signing.profiles.ca`模块，那么你可以忽略这个 `--enrollment.profile tls` 标志参数。

When this command completes successfully, the `fabric-ca-client/tls-ca/tlsadmin/msp` folder is generated and contains the signed cert and private key for the TLS CA admin identity. If the enroll command fails for some reason, to avoid confusion later, you should remove the generated private key from the `fabric-ca-client/tls-ca/admin/msp/keystore` folder before reattempting the enroll command. We will reference this crypto material later when it is required to register other identities with the TLS CA.

当指令运行完成，`fabric-ca-client/tls-ca/tlsadmin/msp`文件夹下将会生成TLS CA 管理身份的签名证书和私钥。如果enroll命令运行失败，你需要在重新尝试enroll命令前删除这个文件夹`fabric-ca-client/tls-ca/admin/msp/keystore`下的私钥来避免冲突。我们将在使用TLS CA 注册其他身份的时候引用这个加密材料。

**Tip:** After you issue this first `enroll` command from the Fabric CA client, examine the contents of the generated `fabric-ca-client/fabric-ca-client-config.yaml` file to become familiar with the default settings that are used by the Fabric CA client. Because we are using a single Fabric CA client to interact with multiple CA servers, we need to use the `-u` flags on the client CLI commands to target the correct CA server. In conjunction, the `--mspdir` flag indicates the location of the cryptographic material to use on a `register` command or where to store the generated certificates on an `enroll` command.

提示：在你第一次使用`enroll`命令之后，检查生成的`fabric-ca-client/fabric-ca-client-config.yaml`文件的内容，熟悉Fabric CA客户端使用的默认设置。因为我们使用一个Fabric CA客户端与多个CA服务器进行交互，所以需要使用客户端CLI命令上的`-u`标志参数来定位正确的CA服务器。同时，`--mspdir`标志要在register命令中指定使用的加密材料的位置，或者在`enroll`命令中存储生成的证书的位置。



#### Register and enroll the organization CA bootstrap identity with the TLS CA

The TLS CA server was started with a bootstrap identity which has full admin privileges for the server. One of the key abilities of the admin is the ability to register new identities. Each node in the organization that transacts on the network needs to register with the TLS CA. Therefore, before we set up the organization CA, we need to use the TLS CA to register and enroll the organization CA bootstrap identity to get its TLS certificate and private key. The following command registers the organization CA bootstrap identity `rcaadmin` and `rcaadminpw` with the TLS CA.

启动TLS CA服务器时使用的引导标识具有该服务器的完全管理权限。管理员的关键能力之一是注册新身份的能力。组织中在网络上进行交易的每个节点都需要注册TLS CA。因此，在设置组织CA之前，我们需要使用TLS CA来注册和注册组织CA引导身份，以获得其TLS证书和私钥。下面的命令使用TLS CA注册组织CA引导身份

```shell
./fabric-ca-client register -d --id.name rcaadmin --id.secret rcaadminpw -u https://my-machine.example.com:7054  --tls.certfiles tls-root-cert/tls-ca-cert.pem --mspdir tls-ca/tlsadmin/msp
```

Notice that the `--mspdir` flag on the command points to the location of TLS CA admin msp certificates that we generated in the previous step. This crypto material is required to be able to register other users with the TLS CA.

注意，命令上的`--mspdir`标志指向我们在前面步骤中生成的TLS CA admin msp证书的位置。用TLS CA来注册用户的时候需要使用这个加密材料.

Next, we need to enroll the `rcaadmin` user to generate the TLS certificates for the identity. In this case, we use the `--mspdir` flag on the enroll command to designate where the generated organization CA TLS certificates should be stored for the `rcaadmin` user. Because these certificates are for a different identity, it is a best practice to put them in their own folder. Therefore, instead of generating them in the default `msp` folder, we will put them in a new folder named `rcaadmin` that resides along side the `tlsadmin` folder.

接下来，我们需要登记(enroll)`rcaadmin`用户，以生成身份的TLS证书。在本例中，我们在enroll命令上使用`--mspdir`标志来指定应该为rcaadmin用户存储生成的组织CA TLS证书的位置。因为这些证书用于不同的身份，所以最好将它们放在自己的文件夹中。因此，我们不会在默认的msp文件夹中生成它们，而是将它们放在一个名为rcaadmin的新文件夹中，该文件夹位于tlsadmin文件夹旁边。

```shell
./fabric-ca-client enroll -d -u https://rcaadmin:rcaadminpw@my-machine.example.com:7054 --tls.certfiles tls-root-cert/tls-ca-cert.pem --enrollment.profile tls --csr.hosts 'host1,*.example.com' --mspdir tls-ca/rcaadmin/msp
```

In this case, the `--mspdir` flag works a little differently. For the enroll command, the `--mspdir` flag indicates where to store the generated certificates for the `rcaadmin` identity.

上述命令的`--mspdir`标识参数,在运行enroll命令是使用来标识放置生成的msp证书的位置.



#### (Optional) Register and enroll the Intermediate CA admin with the TLS CA

```shell
./fabric-ca-client register -d --id.name icaadmin --id.secret icaadminpw -u https://my-machine.example.com:7054  --tls.certfiles tls-root-cert/tls-ca-cert.pem --mspdir tls-ca/tlsadmin/msp
```

```shell
./fabric-ca-client enroll -d -u https://icaadmin:icaadminpw@my-machine.example.com:7054 --tls.certfiles tls-root-cert/tls-ca-cert.pem --enrollment.profile tls --csr.hosts 'host1,*.example.com' --mspdir tls-ca/icaadmin/msp
```



### 部署组织CA

### 部署中间CA

上述两个操作都与部署TLS CA 类似,最大的区别就是他们各自的`fabric-ca-server-config.yaml`文件不同.(配置文件中需要设置TLS enable)

步骤就是，启动 CA 服务、通过enroll登记该CA的管理员用户。然后就可以进行注册用户，登记用户。



具体部署事宜可以参见官网文档[CA Deployment steps](<https://hyperledger-fabric-ca.readthedocs.io/en/latest/deployguide/cadeploy.html>) 。



