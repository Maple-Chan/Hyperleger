> 官网链接[Service Discovery CLI](<https://hyperledger-fabric.readthedocs.io/en/latest/discovery-cli.html>)
>
> - [ ] [使节点能被服务发现给发现的配置](#Configuring external endpoints)：CORE_PEER_GOSSIP_EXTERNALENDPOINT=peer1.org1.example.com:8051
>
>   
>
> Signed-off-by <chenyf@sucsoft.com>

# Service Discovery CLI

The discovery service has its own Command Line Interface (CLI) which uses a YAML configuration file to persist properties such as certificate and private key paths, as well as MSP ID.

发现服务有自己的命令行界面（CLI），它用YAML配置文件来对包括证书和私钥路径以及成员服务提供者身份证（MSP ID）在内的属性进行维持。

The `discover` command has the following subcommands:

`discover` 命令有以下子命令：

- saveConfig
- peers
- config
- endorsers

And the usage of the command is shown below:

该命令的用法如下：

```powershell
usage: discover [<flags>] <command> [<args> ...]

Command line client for fabric discovery service

Flags:
  --help                   Show context-sensitive help (also try --help-long and --help-man).
  --configFile=CONFIGFILE  Specifies the config file to load the configuration from
  # ^ 指定要加载的配置文件的路径
  --peerTLSCA=PEERTLSCA    Sets the TLS CA certificate file path that verifies the TLS peer's certificate
  # ^ peer节点的tls ca 证书
  --tlsCert=TLSCERT        (Optional) Sets the client TLS certificate file path that is used when the peer enforces client authentication
  --tlsKey=TLSKEY          (Optional) Sets the client TLS key file path that is used when the peer enforces client authentication
  --userKey=USERKEY        Sets the user's key file path that is used to sign messages sent to the peer
  # ^ 设置用于对发送给Peer节点的消息进行签名的用户密钥文件路径Peer节点
  --userCert=USERCERT      Sets the user's certificate file path that is used to authenticate the messages sent to the peer
  # ^ 设置对发送给对等方的消息进行身份验证
  --MSP=MSP                Sets the MSP ID of the user, which represents the CA(s) that issued its user certificate
  # ^ 设置用户的MSPID

Commands:
  help [<command>...]
    Show help.

  peers [<flags>]
    Discover peers

  config [<flags>]
    Discover channel config

  endorsers [<flags>]
    Discover chaincode endorsers

  saveConfig
    Save the config passed by flags into the file specified by --configFile
```



## Configuring external endpoints

**配置外部端点**

Currently, to see peers in service discovery they need to have `EXTERNAL_ENDPOINT` to be configured for them. Otherwise, Fabric assumes the peer should not be disclosed.

当前，若想在服务发现中看到节点，需要在其上配置 `EXTERNAL_ENDPOINT`。否则，Fabric假定该节点不应被揭露。

To define these endpoints, you need to specify them in the `core.yaml` of the peer, replacing the sample endpoint below with the ones of your peer.

要定义这些端点，就得在peer的 `core.yaml`字段指明端点，把下面的样本端点换成你peer上的端点。

```
CORE_PEER_GOSSIP_EXTERNALENDPOINT=peer1.org1.example.com:8051
```

![1591172745580](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric-Service Discovery\Service Discovery CLI.assets\1591172745580.png)



## Persisting configuration

**持久化配置**

To persist the configuration, a config file name should be supplied via the flag `--configFile`, along with the command `saveConfig`:

要想保存配置，需要通过`—configFile` flag和 `saveConfig`命令来提供一个配置文件名：

```bash
discover --configFile conf.yaml --peerTLSCA tls/ca.crt --userKey msp/keystore/ea4f6a38ac7057b6fa9502c2f5f39f182e320f71f667749100fe7dd94c23ce43_sk --userCert msp/signcerts/User1\@org1.example.com-cert.pem  --MSP Org1MSP saveConfig
```

By executing the above command, configuration file would be created:

通过执行以上命令可创建配置文件：

```yaml
$ cat conf.yaml # 配置文件样例
version: 0
tlsconfig:
  certpath: ""
  keypath: ""
  peercacertpath: /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/User1@org1.example.com/tls/ca.crt
  timeout: 0s
signerconfig:
  mspid: Org1MSP
  identitypath: /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem
  keypath: /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore/ea4f6a38ac7057b6fa9502c2f5f39f182e320f71f667749100fe7dd94c23ce43_sk
```

When the peer runs with TLS enabled, the discovery service on the peer requires the client to connect to it with mutual TLS, which means it needs to supply a TLS certificate. The peer is configured by default to request (but not to verify) client TLS certificates, so supplying a TLS certificate isn’t needed (unless the peer’s `tls.clientAuthRequired` is set to `true`).

当TLS（安全传输层协议）启动时运行peer，peer上的发现服务需要客户端凭借相互的TLS证书来与之相连，这就意味着，发现服务需要提供一个TLS证书。peer的默认配置为请求（但不验证）客户端的TLS证书，因此并不需要提供TLS证书（除非peer的`tls.clientAuthRequired` 被设置为`true`）。

When the discovery CLI’s config file has a certificate path for `peercacertpath`, but the `certpath` and `keypath` aren’t configured as in the above - the discovery CLI generates a self-signed TLS certificate and uses this to connect to the peer.

当发现CLI的配置文件有 `peercacertpath`的证书路径，但是 `certpath`和`keypath` 没有按以上方式进行配置——`discovery CLI`生成一个自签名的TLS证书并用该证书与节点连接。

When the `peercacertpath` isn’t configured, the discovery CLI connects without TLS , and this is highly not recommended, as the information is sent over plaintext, un-encrypted.

当未配置`peercacertpath` ，`discoveryCLI `与节点连接没有用到TLS证书。但由于信息是以纯文本形式进行传送，未经加密，因此极不推荐这种操作。



## Querying the discovery service

**查询发现服务**

The discoveryCLI acts as a discovery client, and it needs to be executed against a peer. This is done via specifying the `--server` flag. In addition, the queries are channel-scoped, so the `--channel` flag must be used.

`discoveryCLI `作为一个发现客户端，需要依赖peer执行。此过程通过指明 `--server flag`来实现。除此之外，查询是在通道范围内进行的，所以必须使用 `--channel` flag。

The only query that doesn’t require a channel is the local membership peer query, which by default can only be used by administrators of the peer being queried.

唯一不需要通道的查询是本地成员节点查询，默认情况下，本地成员节点查询只能由被查询节点的管理员使用。

The discover CLI supports all server-side queries:

`discoveryCLI `支持所有服务器端的查询：

- Peer membership query

  节点成员查询

- Configuration query

  配置查询

- Endorsers query

  背书者查询

Let’s go over them and see how they should be invoked and parsed:

我们一起来看看这些查询，了解一下它们是如何被调用和解析的：



## Peer membership query:

**节点成员查询：**

```bash
$ discover --configFile conf.yaml peers --channel mychannel  --server peer0.org1.example.com:7051
[
    {
        "MSPID": "Org2MSP",
        "LedgerHeight": 5,
        "Endpoint": "peer0.org2.example.com:9051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICKTCCAc+gAwIBAgIRANK4WBck5gKuzTxVQIwhYMUwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzIuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzIuZXhhbXBsZS5jb20wHhcNMTgwNjE3MTM0NTIxWhcNMjgwNjE0MTM0NTIx\nWjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjAub3Jn\nMi5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABJa0gkMRqJCi\nzmx+L9xy/ecJNvdAV2zmSx5Sf2qospVAH1MYCHyudDEvkiRuBPgmCdOdwJsE0g+h\nz0nZdKq6/X+jTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\nIwQkMCKAIFZMuZfUtY6n2iyxaVr3rl+x5lU0CdG9x7KAeYydQGTMMAoGCCqGSM49\nBAMCA0gAMEUCIQC0M9/LJ7j3I9NEPQ/B1BpnJP+UNPnGO2peVrM/mJ1nVgIgS1ZA\nA1tsxuDyllaQuHx2P+P9NDFdjXx5T08lZhxuWYM=\n-----END CERTIFICATE-----\n",
        "Chaincodes": [
            "mycc"
        ]
    },
    {
        "MSPID": "Org2MSP",
        "LedgerHeight": 5,
        "Endpoint": "peer1.org2.example.com:10051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICKDCCAc+gAwIBAgIRALnNJzplCrYy4Y8CjZtqL7AwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzIuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzIuZXhhbXBsZS5jb20wHhcNMTgwNjE3MTM0NTIxWhcNMjgwNjE0MTM0NTIx\nWjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjEub3Jn\nMi5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABNDopAkHlDdu\nq10HEkdxvdpkbs7EJyqv1clvCt/YMn1hS6sM+bFDgkJKalG7s9Hg3URF0aGpy51R\nU+4F9Muo+XajTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\nIwQkMCKAIFZMuZfUtY6n2iyxaVr3rl+x5lU0CdG9x7KAeYydQGTMMAoGCCqGSM49\nBAMCA0cAMEQCIAR4fBmIBKW2jp0HbbabVepNtl1c7+6++riIrEBnoyIVAiBBvWmI\nyG02c5hu4wPAuVQMB7AU6tGSeYaWSAAo/ExunQ==\n-----END CERTIFICATE-----\n",
        "Chaincodes": [
            "mycc"
        ]
    },
    {
        "MSPID": "Org1MSP",
        "LedgerHeight": 5,
        "Endpoint": "peer0.org1.example.com:7051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICKDCCAc6gAwIBAgIQP18LeXtEXGoN8pTqzXTHZTAKBggqhkjOPQQDAjBzMQsw\nCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\nYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMTY2Eu\nb3JnMS5leGFtcGxlLmNvbTAeFw0xODA2MTcxMzQ1MjFaFw0yODA2MTQxMzQ1MjFa\nMGoxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1T\nYW4gRnJhbmNpc2NvMQ0wCwYDVQQLEwRwZWVyMR8wHQYDVQQDExZwZWVyMC5vcmcx\nLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKeC/1Rg/ynSk\nNNItaMlaCDZOaQvxJEl6o3fqx1PVFlfXE4NarY3OO1N3YZI41hWWoXksSwJu/35S\nM7wMEzw+3KNNMEswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwKwYDVR0j\nBCQwIoAgcecTOxTes6rfgyxHH6KIW7hsRAw2bhP9ikCHkvtv/RcwCgYIKoZIzj0E\nAwIDSAAwRQIhAKiJEv79XBmr8gGY6kHrGL0L3sq95E7IsCYzYdAQHj+DAiBPcBTg\nRuA0//Kq+3aHJ2T0KpKHqD3FfhZZolKDkcrkwQ==\n-----END CERTIFICATE-----\n",
        "Chaincodes": [
            "mycc"
        ]
    },
    {
        "MSPID": "Org1MSP",
        "LedgerHeight": 5,
        "Endpoint": "peer1.org1.example.com:8051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICJzCCAc6gAwIBAgIQO7zMEHlMfRhnP6Xt65jwtDAKBggqhkjOPQQDAjBzMQsw\nCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\nYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMTY2Eu\nb3JnMS5leGFtcGxlLmNvbTAeFw0xODA2MTcxMzQ1MjFaFw0yODA2MTQxMzQ1MjFa\nMGoxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1T\nYW4gRnJhbmNpc2NvMQ0wCwYDVQQLEwRwZWVyMR8wHQYDVQQDExZwZWVyMS5vcmcx\nLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEoII9k8db/Q2g\nRHw5rk3SYw+OMFw9jNbsJJyC5ttJRvc12Dn7lQ8ZR9hW1vLQ3NtqO/couccDJcHg\nt47iHBNadaNNMEswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwKwYDVR0j\nBCQwIoAgcecTOxTes6rfgyxHH6KIW7hsRAw2bhP9ikCHkvtv/RcwCgYIKoZIzj0E\nAwIDRwAwRAIgGHGtRVxcFVeMQr9yRlebs23OXEECNo6hNqd/4ChLwwoCIBFKFd6t\nlL5BVzVMGQyXWcZGrjFgl4+fDrwjmMe+jAfa\n-----END CERTIFICATE-----\n",
        "Chaincodes": null
    }
]
```

As seen, this command outputs a JSON containing membership information about all the peers in the channel that the peer queried possesses.

如上可见，该命令输出了一个JSON，其中包含了被查询节点所在通道上所有节点的成员信息。

The `Identity` that is returned is the enrollment certificate of the peer, and it can be parsed with a combination of `jq` and `openssl`:

被返回的 `Identity` 是节点的成员登记（enrollment）证书，可被`jq` 和 `openssl`的组合进行语法分析：

```bash
$ discover --configFile conf.yaml peers --channel mychannel  --server peer0.org1.example.com:7051  | jq .[0].Identity | sed "s/\\\n/\n/g" | sed "s/\"//g"  | openssl x509 -text -noout
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number:
            55:e9:3f:97:94:d5:74:db:e2:d6:99:3c:01:24:be:bf
    Signature Algorithm: ecdsa-with-SHA256
        Issuer: C=US, ST=California, L=San Francisco, O=org2.example.com, CN=ca.org2.example.com
        Validity
            Not Before: Jun  9 11:58:28 2018 GMT
            Not After : Jun  6 11:58:28 2028 GMT
        Subject: C=US, ST=California, L=San Francisco, OU=peer, CN=peer0.org2.example.com
        Subject Public Key Info:
            Public Key Algorithm: id-ecPublicKey
                Public-Key: (256 bit)
                pub:
                    04:f5:69:7a:11:65:d9:85:96:65:b7:b7:1b:08:77:
                    43:de:cb:ad:3a:79:ec:cc:2a:bc:d7:93:68:ae:92:
                    1c:4b:d8:32:47:d6:3d:72:32:f1:f1:fb:26:e4:69:
                    c2:eb:c9:45:69:99:78:d7:68:a9:77:09:88:c6:53:
                    01:2a:c1:f8:c0
                ASN1 OID: prime256v1
                NIST CURVE: P-256
        X509v3 extensions:
            X509v3 Key Usage: critical
                Digital Signature
            X509v3 Basic Constraints: critical
                CA:FALSE
            X509v3 Authority Key Identifier:
                keyid:8E:58:82:C9:0A:11:10:A9:0B:93:03:EE:A0:54:42:F4:A3:EF:11:4C:82:B6:F9:CE:10:A2:1E:24:AB:13:82:A0

    Signature Algorithm: ecdsa-with-SHA256
         30:44:02:20:29:3f:55:2b:9f:7b:99:b2:cb:06:ca:15:3f:93:
         a1:3d:65:5c:7b:79:a1:7a:d1:94:50:f0:cd:db:ea:61:81:7a:
         02:20:3b:40:5b:60:51:3c:f8:0f:9b:fc:ae:fc:21:fd:c8:36:
         a3:18:39:58:20:72:3d:1a:43:74:30:f3:56:01:aa:26
```



## Configuration query:

**配置查询**

The configuration query returns a mapping from MSP IDs to orderer endpoints, as well as the `FabricMSPConfig` which can be used to verify all peer and orderer nodes by the SDK:

配置查询返回了从MSP（成员服务提供者）ID到orderer端点的映射，还返回了`FabricMSPConfig` ，它可被用来通过SDK验证所有peer和orderer：

```bash
$ discover --configFile conf.yaml config --channel mychannel  --server peer0.org1.example.com:7051
{
    "msps": {
        "OrdererOrg": {
            "name": "OrdererMSP",
            "root_certs": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNMekNDQWRhZ0F3SUJBZ0lSQU1pWkxUb3RmMHR6VTRzNUdIdkQ0UjR3Q2dZSUtvWkl6ajBFQXdJd2FURUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhGREFTQmdOVkJBb1RDMlY0WVcxd2JHVXVZMjl0TVJjd0ZRWURWUVFERXc1allTNWxlR0Z0CmNHeGxMbU52YlRBZUZ3MHhPREEyTURreE1UVTRNamhhRncweU9EQTJNRFl4TVRVNE1qaGFNR2t4Q3pBSkJnTlYKQkFZVEFsVlRNUk13RVFZRFZRUUlFd3BEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIRXcxVFlXNGdSbkpoYm1OcApjMk52TVJRd0VnWURWUVFLRXd0bGVHRnRjR3hsTG1OdmJURVhNQlVHQTFVRUF4TU9ZMkV1WlhoaGJYQnNaUzVqCmIyMHdXVEFUQmdjcWhrak9QUUlCQmdncWhrak9QUU1CQndOQ0FBUW9ySjVSamFTQUZRci9yc2xoMWdobnNCWEQKeDVsR1lXTUtFS1pDYXJDdkZBekE0bHUwb2NQd0IzNWJmTVN5bFJPVmdVdHF1ZU9IcFBNc2ZLNEFrWjR5bzE4dwpYVEFPQmdOVkhROEJBZjhFQkFNQ0FhWXdEd1lEVlIwbEJBZ3dCZ1lFVlIwbEFEQVBCZ05WSFJNQkFmOEVCVEFECkFRSC9NQ2tHQTFVZERnUWlCQ0JnbmZJd0pzNlBaWUZCclpZVkRpU05vSjNGZWNFWHYvN2xHL3QxVUJDbVREQUsKQmdncWhrak9QUVFEQWdOSEFEQkVBaUE5NGFkc21UK0hLalpFVVpnM0VkaWdSM296L3pEQkNhWUY3TEJUVXpuQgpEZ0lnYS9RZFNPQnk1TUx2c0lSNTFDN0N4UnR2NUM5V05WRVlmWk5SaGdXRXpoOD0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo="
            ],
            "admins": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNDVENDQWJDZ0F3SUJBZ0lRR2wzTjhaSzRDekRRQmZqYVpwMVF5VEFLQmdncWhrak9QUVFEQWpCcE1Rc3cKQ1FZRFZRUUdFd0pWVXpFVE1CRUdBMVVFQ0JNS1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ4TU5VMkZ1SUVaeQpZVzVqYVhOamJ6RVVNQklHQTFVRUNoTUxaWGhoYlhCc1pTNWpiMjB4RnpBVkJnTlZCQU1URG1OaExtVjRZVzF3CmJHVXVZMjl0TUI0WERURTRNRFl3T1RFeE5UZ3lPRm9YRFRJNE1EWXdOakV4TlRneU9Gb3dWakVMTUFrR0ExVUUKQmhNQ1ZWTXhFekFSQmdOVkJBZ1RDa05oYkdsbWIzSnVhV0V4RmpBVUJnTlZCQWNURFZOaGJpQkdjbUZ1WTJsegpZMjh4R2pBWUJnTlZCQU1NRVVGa2JXbHVRR1Y0WVcxd2JHVXVZMjl0TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJCnpqMERBUWNEUWdBRWl2TXQybVdiQ2FHb1FZaWpka1BRM1NuTGFkMi8rV0FESEFYMnRGNWthMTBteG1OMEx3VysKdmE5U1dLMmJhRGY5RDQ2TVROZ2gycnRhUitNWXFWRm84Nk5OTUVzd0RnWURWUjBQQVFIL0JBUURBZ2VBTUF3RwpBMVVkRXdFQi93UUNNQUF3S3dZRFZSMGpCQ1F3SW9BZ1lKM3lNQ2JPajJXQlFhMldGUTRramFDZHhYbkJGNy8rCjVSdjdkVkFRcGt3d0NnWUlLb1pJemowRUF3SURSd0F3UkFJZ2RIc0pUcGM5T01DZ3JPVFRLTFNnU043UWk3MWIKSWpkdzE4MzJOeXFQZnJ3Q0lCOXBhSlRnL2R5ckNhWUx1ZndUbUtFSnZZMEtXVzcrRnJTeG5CTGdzZjJpCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
            ],
            "crypto_config": {
                "signature_hash_family": "SHA2",
                "identity_identifier_hash_function": "SHA256"
            },
            "tls_root_certs": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNORENDQWR1Z0F3SUJBZ0lRZDdodzFIaHNZTXI2a25ETWJrZThTakFLQmdncWhrak9QUVFEQWpCc01Rc3cKQ1FZRFZRUUdFd0pWVXpFVE1CRUdBMVVFQ0JNS1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ4TU5VMkZ1SUVaeQpZVzVqYVhOamJ6RVVNQklHQTFVRUNoTUxaWGhoYlhCc1pTNWpiMjB4R2pBWUJnTlZCQU1URVhSc2MyTmhMbVY0CllXMXdiR1V1WTI5dE1CNFhEVEU0TURZd09URXhOVGd5T0ZvWERUSTRNRFl3TmpFeE5UZ3lPRm93YkRFTE1Ba0cKQTFVRUJoTUNWVk14RXpBUkJnTlZCQWdUQ2tOaGJHbG1iM0p1YVdFeEZqQVVCZ05WQkFjVERWTmhiaUJHY21GdQpZMmx6WTI4eEZEQVNCZ05WQkFvVEMyVjRZVzF3YkdVdVkyOXRNUm93R0FZRFZRUURFeEYwYkhOallTNWxlR0Z0CmNHeGxMbU52YlRCWk1CTUdCeXFHU000OUFnRUdDQ3FHU000OUF3RUhBMElBQk9ZZGdpNm53a3pYcTBKQUF2cTIKZU5xNE5Ybi85L0VRaU13Tzc1dXdpTWJVbklYOGM1N2NYU2dQdy9NMUNVUGFwNmRyMldvTjA3RGhHb1B6ZXZaMwp1aFdqWHpCZE1BNEdBMVVkRHdFQi93UUVBd0lCcGpBUEJnTlZIU1VFQ0RBR0JnUlZIU1VBTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0tRWURWUjBPQkNJRUlCcW0xZW9aZy9qSW52Z1ZYR2cwbzVNamxrd2tSekRlalAzZkplbW8KU1hBek1Bb0dDQ3FHU000OUJBTUNBMGNBTUVRQ0lEUG9FRkF5bFVYcEJOMnh4VEo0MVplaS9ZQWFvN29aL0tEMwpvTVBpQ3RTOUFpQmFiU1dNS3UwR1l4eXdsZkFwdi9CWitxUEJNS0JMNk5EQ1haUnpZZmtENEE9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg=="
            ]
        },
        "Org1MSP": {
            "name": "Org1MSP",
            "root_certs": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNSRENDQWVxZ0F3SUJBZ0lSQU1nN2VETnhwS0t0ZGl0TDRVNDRZMUl3Q2dZSUtvWkl6ajBFQXdJd2N6RUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpFdVpYaGhiWEJzWlM1amIyMHhIREFhQmdOVkJBTVRFMk5oCkxtOXlaekV1WlhoaGJYQnNaUzVqYjIwd0hoY05NVGd3TmpBNU1URTFPREk0V2hjTk1qZ3dOakEyTVRFMU9ESTQKV2pCek1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQk1LUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnhNTgpVMkZ1SUVaeVlXNWphWE5qYnpFWk1CY0dBMVVFQ2hNUWIzSm5NUzVsZUdGdGNHeGxMbU52YlRFY01Cb0dBMVVFCkF4TVRZMkV1YjNKbk1TNWxlR0Z0Y0d4bExtTnZiVEJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUEKQk41d040THpVNGRpcUZSWnB6d3FSVm9JbWw1MVh0YWkzbWgzUXo0UEZxWkhXTW9lZ0ovUWRNKzF4L3RobERPcwpnbmVRcndGd216WGpvSSszaHJUSmRuU2pYekJkTUE0R0ExVWREd0VCL3dRRUF3SUJwakFQQmdOVkhTVUVDREFHCkJnUlZIU1VBTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3S1FZRFZSME9CQ0lFSU9CZFFMRitjTVdhNmUxcDJDcE8KRXg3U0hVaW56VnZkNTVoTG03dzZ2NzJvTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDQyt6T1lHcll0ZTB4SgpSbDVYdUxjUWJySW9UeHpsRnJLZWFNWnJXMnVaSkFJZ0NVVGU5MEl4aW55dk4wUkh4UFhoVGNJTFdEZzdLUEJOCmVrNW5TRlh3Y0lZPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg=="
            ],
            "admins": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNLakNDQWRDZ0F3SUJBZ0lRRTRFK0tqSHgwdTlzRSsxZUgrL1dOakFLQmdncWhrak9QUVFEQWpCek1Rc3cKQ1FZRFZRUUdFd0pWVXpFVE1CRUdBMVVFQ0JNS1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ4TU5VMkZ1SUVaeQpZVzVqYVhOamJ6RVpNQmNHQTFVRUNoTVFiM0puTVM1bGVHRnRjR3hsTG1OdmJURWNNQm9HQTFVRUF4TVRZMkV1CmIzSm5NUzVsZUdGdGNHeGxMbU52YlRBZUZ3MHhPREEyTURreE1UVTRNamhhRncweU9EQTJNRFl4TVRVNE1qaGEKTUd3eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUlFd3BEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIRXcxVApZVzRnUm5KaGJtTnBjMk52TVE4d0RRWURWUVFMRXdaamJHbGxiblF4SHpBZEJnTlZCQU1NRmtGa2JXbHVRRzl5Clp6RXVaWGhoYlhCc1pTNWpiMjB3V1RBVEJnY3Foa2pPUFFJQkJnZ3Foa2pPUFFNQkJ3TkNBQVFqK01MZk1ESnUKQ2FlWjV5TDR2TnczaWp4ZUxjd2YwSHo1blFrbXVpSnFETjRhQ0ZwVitNTTVablFEQmx1dWRyUS80UFA1Sk1WeQpreWZsQ3pJa2NCNjdvMDB3U3pBT0JnTlZIUThCQWY4RUJBTUNCNEF3REFZRFZSMFRBUUgvQkFJd0FEQXJCZ05WCkhTTUVKREFpZ0NEZ1hVQ3hmbkRGbXVudGFkZ3FUaE1lMGgxSXA4MWIzZWVZUzV1OE9yKzlxREFLQmdncWhrak8KUFFRREFnTklBREJGQWlFQXlJV21QcjlQakdpSk1QM1pVd05MRENnNnVwMlVQVXNJSzd2L2h3RVRra01DSUE0cQo3cHhQZy9VVldiamZYeE0wUCsvcTEzbXFFaFlYaVpTTXpoUENFNkNmCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
            ],
            "crypto_config": {
                "signature_hash_family": "SHA2",
                "identity_identifier_hash_function": "SHA256"
            },
            "tls_root_certs": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNTVENDQWUrZ0F3SUJBZ0lRZlRWTE9iTENVUjdxVEY3Z283UXgvakFLQmdncWhrak9QUVFEQWpCMk1Rc3cKQ1FZRFZRUUdFd0pWVXpFVE1CRUdBMVVFQ0JNS1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ4TU5VMkZ1SUVaeQpZVzVqYVhOamJ6RVpNQmNHQTFVRUNoTVFiM0puTVM1bGVHRnRjR3hsTG1OdmJURWZNQjBHQTFVRUF4TVdkR3h6ClkyRXViM0puTVM1bGVHRnRjR3hsTG1OdmJUQWVGdzB4T0RBMk1Ea3hNVFU0TWpoYUZ3MHlPREEyTURZeE1UVTQKTWpoYU1IWXhDekFKQmdOVkJBWVRBbFZUTVJNd0VRWURWUVFJRXdwRFlXeHBabTl5Ym1saE1SWXdGQVlEVlFRSApFdzFUWVc0Z1JuSmhibU5wYzJOdk1Sa3dGd1lEVlFRS0V4QnZjbWN4TG1WNFlXMXdiR1V1WTI5dE1SOHdIUVlEClZRUURFeFowYkhOallTNXZjbWN4TG1WNFlXMXdiR1V1WTI5dE1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMEQKQVFjRFFnQUVZbnp4bmMzVUpHS0ZLWDNUNmR0VGpkZnhJTVYybGhTVzNab0lWSW9mb04rWnNsWWp0d0g2ZXZXYgptTkZGQmRaYWExTjluaXRpbmxxbVVzTU1NQ2JieXFOZk1GMHdEZ1lEVlIwUEFRSC9CQVFEQWdHbU1BOEdBMVVkCkpRUUlNQVlHQkZVZEpRQXdEd1lEVlIwVEFRSC9CQVV3QXdFQi96QXBCZ05WSFE0RUlnUWdlVTAwNlNaUllUNDIKN1Uxb2YwL3RGdHUvRFVtazVLY3hnajFCaklJakduZ3dDZ1lJS29aSXpqMEVBd0lEU0FBd1JRSWhBSWpvcldJTwpRNVNjYjNoZDluRi9UamxWcmk1UHdTaDNVNmJaMFdYWEsxYzVBaUFlMmM5QmkyNFE1WjQ0aXQ1MkI5cm1hU1NpCkttM2NZVlY0cWJ6RFhMOHZYUT09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
            ],
            "fabric_node_ous": {
                "enable": true,
                "client_ou_identifier": {
                    "certificate": "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNSRENDQWVxZ0F3SUJBZ0lSQU1nN2VETnhwS0t0ZGl0TDRVNDRZMUl3Q2dZSUtvWkl6ajBFQXdJd2N6RUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpFdVpYaGhiWEJzWlM1amIyMHhIREFhQmdOVkJBTVRFMk5oCkxtOXlaekV1WlhoaGJYQnNaUzVqYjIwd0hoY05NVGd3TmpBNU1URTFPREk0V2hjTk1qZ3dOakEyTVRFMU9ESTQKV2pCek1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQk1LUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnhNTgpVMkZ1SUVaeVlXNWphWE5qYnpFWk1CY0dBMVVFQ2hNUWIzSm5NUzVsZUdGdGNHeGxMbU52YlRFY01Cb0dBMVVFCkF4TVRZMkV1YjNKbk1TNWxlR0Z0Y0d4bExtTnZiVEJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUEKQk41d040THpVNGRpcUZSWnB6d3FSVm9JbWw1MVh0YWkzbWgzUXo0UEZxWkhXTW9lZ0ovUWRNKzF4L3RobERPcwpnbmVRcndGd216WGpvSSszaHJUSmRuU2pYekJkTUE0R0ExVWREd0VCL3dRRUF3SUJwakFQQmdOVkhTVUVDREFHCkJnUlZIU1VBTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3S1FZRFZSME9CQ0lFSU9CZFFMRitjTVdhNmUxcDJDcE8KRXg3U0hVaW56VnZkNTVoTG03dzZ2NzJvTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDQyt6T1lHcll0ZTB4SgpSbDVYdUxjUWJySW9UeHpsRnJLZWFNWnJXMnVaSkFJZ0NVVGU5MEl4aW55dk4wUkh4UFhoVGNJTFdEZzdLUEJOCmVrNW5TRlh3Y0lZPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==",
                    "organizational_unit_identifier": "client"
                },
                "peer_ou_identifier": {
                    "certificate": "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNSRENDQWVxZ0F3SUJBZ0lSQU1nN2VETnhwS0t0ZGl0TDRVNDRZMUl3Q2dZSUtvWkl6ajBFQXdJd2N6RUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpFdVpYaGhiWEJzWlM1amIyMHhIREFhQmdOVkJBTVRFMk5oCkxtOXlaekV1WlhoaGJYQnNaUzVqYjIwd0hoY05NVGd3TmpBNU1URTFPREk0V2hjTk1qZ3dOakEyTVRFMU9ESTQKV2pCek1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQk1LUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnhNTgpVMkZ1SUVaeVlXNWphWE5qYnpFWk1CY0dBMVVFQ2hNUWIzSm5NUzVsZUdGdGNHeGxMbU52YlRFY01Cb0dBMVVFCkF4TVRZMkV1YjNKbk1TNWxlR0Z0Y0d4bExtTnZiVEJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUEKQk41d040THpVNGRpcUZSWnB6d3FSVm9JbWw1MVh0YWkzbWgzUXo0UEZxWkhXTW9lZ0ovUWRNKzF4L3RobERPcwpnbmVRcndGd216WGpvSSszaHJUSmRuU2pYekJkTUE0R0ExVWREd0VCL3dRRUF3SUJwakFQQmdOVkhTVUVDREFHCkJnUlZIU1VBTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3S1FZRFZSME9CQ0lFSU9CZFFMRitjTVdhNmUxcDJDcE8KRXg3U0hVaW56VnZkNTVoTG03dzZ2NzJvTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDQyt6T1lHcll0ZTB4SgpSbDVYdUxjUWJySW9UeHpsRnJLZWFNWnJXMnVaSkFJZ0NVVGU5MEl4aW55dk4wUkh4UFhoVGNJTFdEZzdLUEJOCmVrNW5TRlh3Y0lZPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==",
                    "organizational_unit_identifier": "peer"
                }
            }
        },
        "Org2MSP": {
            "name": "Org2MSP",
            "root_certs": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNSRENDQWVxZ0F3SUJBZ0lSQUx2SWV2KzE4Vm9LZFR2V1RLNCtaZ2d3Q2dZSUtvWkl6ajBFQXdJd2N6RUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpJdVpYaGhiWEJzWlM1amIyMHhIREFhQmdOVkJBTVRFMk5oCkxtOXlaekl1WlhoaGJYQnNaUzVqYjIwd0hoY05NVGd3TmpBNU1URTFPREk0V2hjTk1qZ3dOakEyTVRFMU9ESTQKV2pCek1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQk1LUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnhNTgpVMkZ1SUVaeVlXNWphWE5qYnpFWk1CY0dBMVVFQ2hNUWIzSm5NaTVsZUdGdGNHeGxMbU52YlRFY01Cb0dBMVVFCkF4TVRZMkV1YjNKbk1pNWxlR0Z0Y0d4bExtTnZiVEJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUEKQkhUS01aall0TDdnSXZ0ekN4Y2pMQit4NlZNdENzVW0wbExIcGtIeDFQaW5LUU1ybzFJWWNIMEpGVmdFempvSQpCcUdMYURyQmhWQkpoS1kwS21kMUJJZWpYekJkTUE0R0ExVWREd0VCL3dRRUF3SUJwakFQQmdOVkhTVUVDREFHCkJnUlZIU1VBTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3S1FZRFZSME9CQ0lFSUk1WWdza0tFUkNwQzVNRDdxQlUKUXZTajd4Rk1ncmI1emhDaUhpU3JFNEtnTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDWnNSUjVBVU5KUjdJbwpQQzgzUCt1UlF1RmpUYS94eitzVkpZYnBsNEh1Z1FJZ0QzUlhuQWFqaGlPMU1EL1JzSC9JN2FPL1RuWUxkQUl6Cnd4VlNJenhQbWd3PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg=="
            ],
            "admins": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNLVENDQWRDZ0F3SUJBZ0lRU1lpeE1vdmpoM1N2c25WMmFUOXl1REFLQmdncWhrak9QUVFEQWpCek1Rc3cKQ1FZRFZRUUdFd0pWVXpFVE1CRUdBMVVFQ0JNS1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ4TU5VMkZ1SUVaeQpZVzVqYVhOamJ6RVpNQmNHQTFVRUNoTVFiM0puTWk1bGVHRnRjR3hsTG1OdmJURWNNQm9HQTFVRUF4TVRZMkV1CmIzSm5NaTVsZUdGdGNHeGxMbU52YlRBZUZ3MHhPREEyTURreE1UVTRNamhhRncweU9EQTJNRFl4TVRVNE1qaGEKTUd3eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUlFd3BEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIRXcxVApZVzRnUm5KaGJtTnBjMk52TVE4d0RRWURWUVFMRXdaamJHbGxiblF4SHpBZEJnTlZCQU1NRmtGa2JXbHVRRzl5Clp6SXVaWGhoYlhCc1pTNWpiMjB3V1RBVEJnY3Foa2pPUFFJQkJnZ3Foa2pPUFFNQkJ3TkNBQVJFdStKc3l3QlQKdkFYUUdwT2FuS3ZkOVhCNlMxVGU4NTJ2L0xRODVWM1Rld0hlYXZXeGUydUszYTBvRHA5WDV5SlJ4YXN2b2hCcwpOMGJIRWErV1ZFQjdvMDB3U3pBT0JnTlZIUThCQWY4RUJBTUNCNEF3REFZRFZSMFRBUUgvQkFJd0FEQXJCZ05WCkhTTUVKREFpZ0NDT1dJTEpDaEVRcVF1VEErNmdWRUwwbys4UlRJSzIrYzRRb2g0a3F4T0NvREFLQmdncWhrak8KUFFRREFnTkhBREJFQWlCVUFsRStvbFBjMTZBMitmNVBRSmdTZFp0SjNPeXBieG9JVlhOdi90VUJ2QUlnVGFNcgo1K2k2TUxpaU9FZ0wzcWZSWmdkcG1yVm1SbHlIdVdabWE0NXdnaE09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
            ],
            "crypto_config": {
                "signature_hash_family": "SHA2",
                "identity_identifier_hash_function": "SHA256"
            },
            "tls_root_certs": [
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNTakNDQWZDZ0F3SUJBZ0lSQUtoUFFxUGZSYnVpSktqL0JRanQ3RXN3Q2dZSUtvWkl6ajBFQXdJd2RqRUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpJdVpYaGhiWEJzWlM1amIyMHhIekFkQmdOVkJBTVRGblJzCmMyTmhMbTl5WnpJdVpYaGhiWEJzWlM1amIyMHdIaGNOTVRnd05qQTVNVEUxT0RJNFdoY05Namd3TmpBMk1URTEKT0RJNFdqQjJNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUdBMVVFQ0JNS1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRQpCeE1OVTJGdUlFWnlZVzVqYVhOamJ6RVpNQmNHQTFVRUNoTVFiM0puTWk1bGVHRnRjR3hsTG1OdmJURWZNQjBHCkExVUVBeE1XZEd4elkyRXViM0puTWk1bGVHRnRjR3hsTG1OdmJUQlpNQk1HQnlxR1NNNDlBZ0VHQ0NxR1NNNDkKQXdFSEEwSUFCRVIrMnREOWdkME9NTlk5Y20rbllZR2NUeWszRStCMnBsWWxDL2ZVdGdUU0QyZUVyY2kyWmltdQo5N25YeUIrM0NwNFJwVjFIVHdaR0JMbmNnbVIyb1J5alh6QmRNQTRHQTFVZER3RUIvd1FFQXdJQnBqQVBCZ05WCkhTVUVDREFHQmdSVkhTVUFNQThHQTFVZEV3RUIvd1FGTUFNQkFmOHdLUVlEVlIwT0JDSUVJUEN0V01JRFRtWC8KcGxseS8wNDI4eFRXZHlhazQybU9tbVNJSENCcnAyN0tNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUNtN2xmVQpjbG91VHJrS2Z1YjhmdmdJTTU3QS85bW5IdzhpQnAycURtamZhUUlnSjkwcnRUV204YzVBbE93bFpyYkd0NWZMCjF6WXg5QW5DMTJBNnhOZDIzTG89Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
            ],
            "fabric_node_ous": {
                "enable": true,
                "client_ou_identifier": {
                    "certificate": "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNSRENDQWVxZ0F3SUJBZ0lSQUx2SWV2KzE4Vm9LZFR2V1RLNCtaZ2d3Q2dZSUtvWkl6ajBFQXdJd2N6RUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpJdVpYaGhiWEJzWlM1amIyMHhIREFhQmdOVkJBTVRFMk5oCkxtOXlaekl1WlhoaGJYQnNaUzVqYjIwd0hoY05NVGd3TmpBNU1URTFPREk0V2hjTk1qZ3dOakEyTVRFMU9ESTQKV2pCek1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQk1LUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnhNTgpVMkZ1SUVaeVlXNWphWE5qYnpFWk1CY0dBMVVFQ2hNUWIzSm5NaTVsZUdGdGNHeGxMbU52YlRFY01Cb0dBMVVFCkF4TVRZMkV1YjNKbk1pNWxlR0Z0Y0d4bExtTnZiVEJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUEKQkhUS01aall0TDdnSXZ0ekN4Y2pMQit4NlZNdENzVW0wbExIcGtIeDFQaW5LUU1ybzFJWWNIMEpGVmdFempvSQpCcUdMYURyQmhWQkpoS1kwS21kMUJJZWpYekJkTUE0R0ExVWREd0VCL3dRRUF3SUJwakFQQmdOVkhTVUVDREFHCkJnUlZIU1VBTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3S1FZRFZSME9CQ0lFSUk1WWdza0tFUkNwQzVNRDdxQlUKUXZTajd4Rk1ncmI1emhDaUhpU3JFNEtnTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDWnNSUjVBVU5KUjdJbwpQQzgzUCt1UlF1RmpUYS94eitzVkpZYnBsNEh1Z1FJZ0QzUlhuQWFqaGlPMU1EL1JzSC9JN2FPL1RuWUxkQUl6Cnd4VlNJenhQbWd3PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==",
                    "organizational_unit_identifier": "client"
                },
                "peer_ou_identifier": {
                    "certificate": "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNSRENDQWVxZ0F3SUJBZ0lSQUx2SWV2KzE4Vm9LZFR2V1RLNCtaZ2d3Q2dZSUtvWkl6ajBFQXdJd2N6RUwKTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY1REVk5oYmlCRwpjbUZ1WTJselkyOHhHVEFYQmdOVkJBb1RFRzl5WnpJdVpYaGhiWEJzWlM1amIyMHhIREFhQmdOVkJBTVRFMk5oCkxtOXlaekl1WlhoaGJYQnNaUzVqYjIwd0hoY05NVGd3TmpBNU1URTFPREk0V2hjTk1qZ3dOakEyTVRFMU9ESTQKV2pCek1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQk1LUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnhNTgpVMkZ1SUVaeVlXNWphWE5qYnpFWk1CY0dBMVVFQ2hNUWIzSm5NaTVsZUdGdGNHeGxMbU52YlRFY01Cb0dBMVVFCkF4TVRZMkV1YjNKbk1pNWxlR0Z0Y0d4bExtTnZiVEJaTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEEwSUEKQkhUS01aall0TDdnSXZ0ekN4Y2pMQit4NlZNdENzVW0wbExIcGtIeDFQaW5LUU1ybzFJWWNIMEpGVmdFempvSQpCcUdMYURyQmhWQkpoS1kwS21kMUJJZWpYekJkTUE0R0ExVWREd0VCL3dRRUF3SUJwakFQQmdOVkhTVUVDREFHCkJnUlZIU1VBTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3S1FZRFZSME9CQ0lFSUk1WWdza0tFUkNwQzVNRDdxQlUKUXZTajd4Rk1ncmI1emhDaUhpU3JFNEtnTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDWnNSUjVBVU5KUjdJbwpQQzgzUCt1UlF1RmpUYS94eitzVkpZYnBsNEh1Z1FJZ0QzUlhuQWFqaGlPMU1EL1JzSC9JN2FPL1RuWUxkQUl6Cnd4VlNJenhQbWd3PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==",
                    "organizational_unit_identifier": "peer"
                }
            }
        },
        "Org3MSP": {
            "name": "Org3MSP",
            "root_certs": [
                "CgJPVQoEUm9sZQoMRW5yb2xsbWVudElEChBSZXZvY2F0aW9uSGFuZGxlEkQKIKoEXcq/psdYnMKCiT79N+dS1hM8k+SuzU1blOgTuN++EiBe2m3E+FjWLuQGMNRGRrEVTMqTvC4A/5jvCLv2ja1sZxpECiDBbI0kwetxAwFzHwb1hi8TlkGW3OofvuVzfFt9VlewcRIgyvsxG5/THdWyKJTdNx8Gle2hoCbVF0Y1/DQESBjGOGciRAog25fMyWps+FLOjzj1vIsGUyO457ri3YMvmUcycIH2FvQSICTtzaFvSPUiDtNtAVz+uetuB9kfmjUdUSQxjyXULOm2IkQKIO8FKzwoWwu8Mo77GNqnKFGCZaJL9tlrkdTuEMu9ujzbEiA4xtzo8oo8oEhFVsl6010mNoj1VuI0Wmz4tvUgXolCIiJECiDZcZPuwk/uaJMuVph7Dy/icgnAtVYHShET41O0Eh3Q5BIgy5q9VMQrch9VW5yajhY8dH1uA593gKd5kBqGdLfiXzAiRAogAnUYq/kwKzFfmIm/W4nZxi1kjG2C8NRjsYYBkeAOQ6wSIGyX5GGmwgvxgXXehNWBfijyNIJALGRVhO8YtBqr+vnrKogBCiDHR1XQsDbpcBoZFJ09V97zsIKNVTxjUow7/wwC+tq3oBIgSWT/peiO2BI0DecypKfgMpVR8DWXl8ZHSrPISsL3Mc8aINem9+BOezLwFKCbtVH1KAHIRLyyiNP+TkIKW6x9RkThIiAbIJCYU6O02EB8uX6rqLU/1lHxV0vtWdIsKCTLx2EZmDJECiCPXeyUyFzPS3iFv8CQUOLCPZxf6buZS5JlM6EE/gCRaxIgmF9GKPLLmEoA77+AU3J8Iwnu9pBxnaHtUlyf/F9p30c6RAogG7ENKWlOZ4aF0HprqXAjl++Iao7/iE8xeVcKRlmfq1ASIGtmmavDAVS2bw3zClQd4ZBD2DrqCBO9NPOcLNB0IWeIQiCjxTdbmcuBNINZYWe+5fWyI1oY9LavKzDVkdh+miu26EogY2uJtJGfKrQQjy+pgf9FdPMUk+8PNUBtH9LCD4bos7JSIPl6m5lEP/PRAmBaeTQLXdbMxIthxM2gw+Zkc5+IJEWX"
            ],
            "intermediate_certs": [
                "CtgCCkQKIP0UVivtH8NlnRNrZuuu6jpaj2ZbEB4/secGS57MfbINEiDSJweLUMIQSW12jugBQG81lIQflJWvi7vi925u+PU/+xJECiDgOGdNbAiGSoHmTjKhT22fqUqYLIVh+JBHetm4kF4skhIg9XTWRkUqtsfYKENzPgm7ZUSmCHNF8xH7Vnhuc1EpAUgaINwSnJKofiMoyDRZwUBhgfwMH9DJzMccvRVW7IvLMe/cIiCnlRj+mfNVAJGKthLgQBB/JKM14NbUeutyJtTgrmDDiCogme25qGvxJfgQNnzldMMicVyiI6YMfnoThAUyqsTzyXkqIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKiCZ7bmoa/El+BA2fOV0wyJxXKIjpgx+ehOEBTKqxPPJeSogAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAESIFYUenRvjbmEh+37YHJrvFJt4lGq9ShtJ4kEBrfHArPjGgNPVTEqA09VMTL0ARKIAQog/gwzULTJbCAoVg9XfCiROs4cU5oSv4Q80iYWtonAnvsSIE6mYFdzisBU21rhxjfYE7kk3Xjih9A1idJp7TSjfmorGiBwIEbnxUKjs3Z3DXUSTj5R78skdY1hWEjpCbSBvtwn/yIgBVTjvNOIwpBC7qZJKX6yn4tMvoCCGpiz4BKBEUqtBJsaZzBlAjBwZ4WXYOttkhsNA2r94gBfLUdx/4VhW4hwUImcztlau1T14UlNzJolCNkdiLc9CqsCMQD6OBkgDWGq9UlhkK9dJBzU+RElcZdSfVV1hDbbqt+lFRWOzzEkZ+BXCR1k3xybz+o="
            ],
            "admins": [
                "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUhZd0VBWUhLb1pJemowQ0FRWUZLNEVFQUNJRFlnQUVUYk13SEZteEpEMWR3SjE2K0hnVnRDZkpVRzdKK2FTYgorbkVvVmVkREVHYmtTc1owa1lraEpyYkx5SHlYZm15ZWV0ejFIUk1rWjRvMjdxRlMzTlVFb1J2QlM3RHJPWDJjCnZLaDRnbWhHTmlPbzRiWjFOVG9ZL2o3QnpqMFlMSXNlCi0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQo="
            ]
        }
    },
    "orderers": {
        "OrdererOrg": {
            "endpoint": [
                {
                    "host": "orderer.example.com",
                    "port": 7050
                }
            ]
        }
    }
}
```

It’s important to note that the certificates here are base64 encoded, and thus should decoded in a manner similar to the following:

值得注意的是，这里的**证书是base64**编码的，所以应该用类似以下的方法对其进行解码。

```bash
$ discover --configFile conf.yaml config --channel mychannel  --server peer0.org1.example.com:7051 | jq .msps.OrdererOrg.root_certs[0] | sed "s/\"//g" | base64 --decode | openssl x509 -text -noout
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number:
            c8:99:2d:3a:2d:7f:4b:73:53:8b:39:18:7b:c3:e1:1e
    Signature Algorithm: ecdsa-with-SHA256
        Issuer: C=US, ST=California, L=San Francisco, O=example.com, CN=ca.example.com
        Validity
            Not Before: Jun  9 11:58:28 2018 GMT
            Not After : Jun  6 11:58:28 2028 GMT
        Subject: C=US, ST=California, L=San Francisco, O=example.com, CN=ca.example.com
        Subject Public Key Info:
            Public Key Algorithm: id-ecPublicKey
                Public-Key: (256 bit)
                pub:
                    04:28:ac:9e:51:8d:a4:80:15:0a:ff:ae:c9:61:d6:
                    08:67:b0:15:c3:c7:99:46:61:63:0a:10:a6:42:6a:
                    b0:af:14:0c:c0:e2:5b:b4:a1:c3:f0:07:7e:5b:7c:
                    c4:b2:95:13:95:81:4b:6a:b9:e3:87:a4:f3:2c:7c:
                    ae:00:91:9e:32
                ASN1 OID: prime256v1
                NIST CURVE: P-256
        X509v3 extensions:
            X509v3 Key Usage: critical
                Digital Signature, Key Encipherment, Certificate Sign, CRL Sign
            X509v3 Extended Key Usage:
                Any Extended Key Usage
            X509v3 Basic Constraints: critical
                CA:TRUE
            X509v3 Subject Key Identifier:
                60:9D:F2:30:26:CE:8F:65:81:41:AD:96:15:0E:24:8D:A0:9D:C5:79:C1:17:BF:FE:E5:1B:FB:75:50:10:A6:4C
    Signature Algorithm: ecdsa-with-SHA256
         30:44:02:20:3d:e1:a7:6c:99:3f:87:2a:36:44:51:98:37:11:
         d8:a0:47:7a:33:ff:30:c1:09:a6:05:ec:b0:53:53:39:c1:0e:
         02:20:6b:f4:1d:48:e0:72:e4:c2:ef:b0:84:79:d4:2e:c2:c5:
         1b:6f:e4:2f:56:35:51:18:7d:93:51:86:05:84:ce:1f
```



## Endorsers query:

**背书者查询:**

To query for the endorsers of a chaincode call, additional flags need to be supplied:

要想查询一个链码调用的背书者，必须提供额外的flag：

- The `--chaincode` flag is mandatory and it provides the chaincode name(s). To query for a chaincode-to-chaincode invocation, one needs to repeat the `--chaincode` flag with all the chaincodes.

  `--chaincode`flag是必需的，它提供了链码名。要查询多链码的调用，必须对所有相关链码重复提供`–chaincode`flag。

  

- The `--collection` is used to specify private data collections that are expected to used by the chaincode(s). To map from thechaincodes passed via `--chaincode` to the collections, the following syntax should be used: `collection=CC:Collection1,Collection2,...`.

  `--collection`被用来指明链码预计将使用的私有数据集合。若要把 `--chaincode`通过的链码映射到数据集合中，应使用以下语法：`collection=CC:Collection1,Collection2,...`。

  

- The `--noPrivateReads` is used to indicate that the transaction is not expected to read private data for a certain chaincode. This is useful for private data “blind writes”, among other things.

  `--noPrivateReads` 被用于表示此次交易对于某个特定的链码不能读取隐私数据。这对隐私数据的盲写入等方面有用。

For example, to query for a chaincode invocation that results in both cc1 and cc2 to be invoked, as well as writes to private data collection col1 by cc2, one needs to specify: `--chaincsode=cc1 --chaincode=cc2 --collection=cc2:col1`

例如，某项链码调用导致了cc1和cc2被调用，同时cc2将该链码调用写入私有数据集合cc1，要想查询该项链码调用，必须指明：`--chaincode=cc1 --chaincode=cc2 --collection=cc2:col1`



If chaincode cc2 is not expected to read from collection `col1` then `--noPrivateReads=cc2` should be used.

如果不想cc2链码读取col1的数据，使用`--noPrivateReads=cc2` 来标识。

Below is the output of an endorsers query for chaincode **mycc** when the endorsement policy is `AND('Org1.peer', 'Org2.peer')`:

以下显示的是当背书策略为 `AND('Org1.peer', 'Org2.peer')`时，链码**mycc**的背书者查询的输出：

```bash
$ discover --configFile conf.yaml endorsers --channel mychannel  --server peer0.org1.example.com:7051 --chaincode mycc
[
    {
        "Chaincode": "mycc",
        "EndorsersByGroups": {
            "G0": [
                {
                    "MSPID": "Org1MSP",
                    "LedgerHeight": 5,
                    "Endpoint": "peer0.org1.example.com:7051",
                    "Identity": "-----BEGIN CERTIFICATE-----\nMIICKDCCAc+gAwIBAgIRANTiKfUVHVGnrYVzEy1ZSKIwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMTgwNjA5MTE1ODI4WhcNMjgwNjA2MTE1ODI4\nWjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjAub3Jn\nMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABD8jGz1l5Rrw\n5UWqAYnc4JrR46mCYwHhHFgwydccuytb00ouD4rECiBsCaeZFr5tODAK70jFOP/k\n/CtORCDPQ02jTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\nIwQkMCKAIOBdQLF+cMWa6e1p2CpOEx7SHUinzVvd55hLm7w6v72oMAoGCCqGSM49\nBAMCA0cAMEQCIC3bacbDYphXfHrNULxpV/zwD08t7hJxNe8MwgP8/48fAiBiC0cr\nu99oLsRNCFB7R3egyKg1YYao0KWTrr1T+rK9Bg==\n-----END CERTIFICATE-----\n"
                }
            ],
            "G1": [
                {
                    "MSPID": "Org2MSP",
                    "LedgerHeight": 5,
                    "Endpoint": "peer1.org2.example.com:10051",
                    "Identity": "-----BEGIN CERTIFICATE-----\nMIICKDCCAc+gAwIBAgIRAIs6fFxk4Y5cJxSwTjyJ9A8wCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzIuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzIuZXhhbXBsZS5jb20wHhcNMTgwNjA5MTE1ODI4WhcNMjgwNjA2MTE1ODI4\nWjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjEub3Jn\nMi5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABOVFyWVmKZ25\nxDYV3xZBDX4gKQ7rAZfYgOu1djD9EHccZhJVPsdwSjbRsvrfs9Z8mMuwEeSWq/cq\n0cGrMKR93vKjTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\nIwQkMCKAII5YgskKERCpC5MD7qBUQvSj7xFMgrb5zhCiHiSrE4KgMAoGCCqGSM49\nBAMCA0cAMEQCIDJmxseFul1GZ26djKa6jZ6zYYf6hchNF5xxMRWXpCnuAiBMf6JZ\njZjVM9F/OidQ2SBR7OZyMAzgXc5nAabWZpdkuQ==\n-----END CERTIFICATE-----\n"
                },
                {
                    "MSPID": "Org2MSP",
                    "LedgerHeight": 5,
                    "Endpoint": "peer0.org2.example.com:9051",
                    "Identity": "-----BEGIN CERTIFICATE-----\nMIICJzCCAc6gAwIBAgIQVek/l5TVdNvi1pk8ASS+vzAKBggqhkjOPQQDAjBzMQsw\nCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\nYW5jaXNjbzEZMBcGA1UEChMQb3JnMi5leGFtcGxlLmNvbTEcMBoGA1UEAxMTY2Eu\nb3JnMi5leGFtcGxlLmNvbTAeFw0xODA2MDkxMTU4MjhaFw0yODA2MDYxMTU4Mjha\nMGoxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1T\nYW4gRnJhbmNpc2NvMQ0wCwYDVQQLEwRwZWVyMR8wHQYDVQQDExZwZWVyMC5vcmcy\nLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE9Wl6EWXZhZZl\nt7cbCHdD3sutOnnszCq815NorpIcS9gyR9Y9cjLx8fsm5GnC68lFaZl412ipdwmI\nxlMBKsH4wKNNMEswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwKwYDVR0j\nBCQwIoAgjliCyQoREKkLkwPuoFRC9KPvEUyCtvnOEKIeJKsTgqAwCgYIKoZIzj0E\nAwIDRwAwRAIgKT9VK597mbLLBsoVP5OhPWVce3mhetGUUPDN2+phgXoCIDtAW2BR\nPPgPm/yu/CH9yDajGDlYIHI9GkN0MPNWAaom\n-----END CERTIFICATE-----\n"
                }
            ]
        },
        "Layouts": [
            {
                "quantities_by_group": {
                    "G0": 1,
                    "G1": 1
                }
            }
        ]
    }
]
```



## Not using a configuration file

**未使用配置文件**

It is possible to execute the discovery CLI without having a configuration file, and just passing all needed configuration as commandline flags. The following is an example of a local peer membership query which loads administrator credentials:

在没有配置文件的情况下也可以执行发现CLI，仅将所有需要的配置通过为命令行flag。以下是一个有关载入了管理员证书的本地节点成员查询的例子：

```bash
$ discover --peerTLSCA tls/ca.crt --userKey msp/keystore/cf31339d09e8311ac9ca5ed4e27a104a7f82f1e5904b3296a170ba4725ffde0d_sk --userCert msp/signcerts/Admin\@org1.example.com-cert.pem --MSP Org1MSP --tlsCert tls/client.crt --tlsKey tls/client.key peers --server peer0.org1.example.com:7051
[
    {
        "MSPID": "Org1MSP",
        "Endpoint": "peer1.org1.example.com:8051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICJzCCAc6gAwIBAgIQO7zMEHlMfRhnP6Xt65jwtDAKBggqhkjOPQQDAjBzMQsw\nCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\nYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMTY2Eu\nb3JnMS5leGFtcGxlLmNvbTAeFw0xODA2MTcxMzQ1MjFaFw0yODA2MTQxMzQ1MjFa\nMGoxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1T\nYW4gRnJhbmNpc2NvMQ0wCwYDVQQLEwRwZWVyMR8wHQYDVQQDExZwZWVyMS5vcmcx\nLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEoII9k8db/Q2g\nRHw5rk3SYw+OMFw9jNbsJJyC5ttJRvc12Dn7lQ8ZR9hW1vLQ3NtqO/couccDJcHg\nt47iHBNadaNNMEswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwKwYDVR0j\nBCQwIoAgcecTOxTes6rfgyxHH6KIW7hsRAw2bhP9ikCHkvtv/RcwCgYIKoZIzj0E\nAwIDRwAwRAIgGHGtRVxcFVeMQr9yRlebs23OXEECNo6hNqd/4ChLwwoCIBFKFd6t\nlL5BVzVMGQyXWcZGrjFgl4+fDrwjmMe+jAfa\n-----END CERTIFICATE-----\n",
    },
    {
        "MSPID": "Org1MSP",
        "Endpoint": "peer0.org1.example.com:7051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICKDCCAc6gAwIBAgIQP18LeXtEXGoN8pTqzXTHZTAKBggqhkjOPQQDAjBzMQsw\nCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\nYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UEAxMTY2Eu\nb3JnMS5leGFtcGxlLmNvbTAeFw0xODA2MTcxMzQ1MjFaFw0yODA2MTQxMzQ1MjFa\nMGoxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1T\nYW4gRnJhbmNpc2NvMQ0wCwYDVQQLEwRwZWVyMR8wHQYDVQQDExZwZWVyMC5vcmcx\nLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKeC/1Rg/ynSk\nNNItaMlaCDZOaQvxJEl6o3fqx1PVFlfXE4NarY3OO1N3YZI41hWWoXksSwJu/35S\nM7wMEzw+3KNNMEswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwKwYDVR0j\nBCQwIoAgcecTOxTes6rfgyxHH6KIW7hsRAw2bhP9ikCHkvtv/RcwCgYIKoZIzj0E\nAwIDSAAwRQIhAKiJEv79XBmr8gGY6kHrGL0L3sq95E7IsCYzYdAQHj+DAiBPcBTg\nRuA0//Kq+3aHJ2T0KpKHqD3FfhZZolKDkcrkwQ==\n-----END CERTIFICATE-----\n",
    },
    {
        "MSPID": "Org2MSP",
        "Endpoint": "peer0.org2.example.com:9051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICKTCCAc+gAwIBAgIRANK4WBck5gKuzTxVQIwhYMUwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzIuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzIuZXhhbXBsZS5jb20wHhcNMTgwNjE3MTM0NTIxWhcNMjgwNjE0MTM0NTIx\nWjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjAub3Jn\nMi5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABJa0gkMRqJCi\nzmx+L9xy/ecJNvdAV2zmSx5Sf2qospVAH1MYCHyudDEvkiRuBPgmCdOdwJsE0g+h\nz0nZdKq6/X+jTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\nIwQkMCKAIFZMuZfUtY6n2iyxaVr3rl+x5lU0CdG9x7KAeYydQGTMMAoGCCqGSM49\nBAMCA0gAMEUCIQC0M9/LJ7j3I9NEPQ/B1BpnJP+UNPnGO2peVrM/mJ1nVgIgS1ZA\nA1tsxuDyllaQuHx2P+P9NDFdjXx5T08lZhxuWYM=\n-----END CERTIFICATE-----\n",
    },
    {
        "MSPID": "Org2MSP",
        "Endpoint": "peer1.org2.example.com:10051",
        "Identity": "-----BEGIN CERTIFICATE-----\nMIICKDCCAc+gAwIBAgIRALnNJzplCrYy4Y8CjZtqL7AwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzIuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzIuZXhhbXBsZS5jb20wHhcNMTgwNjE3MTM0NTIxWhcNMjgwNjE0MTM0NTIx\nWjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjEub3Jn\nMi5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABNDopAkHlDdu\nq10HEkdxvdpkbs7EJyqv1clvCt/YMn1hS6sM+bFDgkJKalG7s9Hg3URF0aGpy51R\nU+4F9Muo+XajTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\nIwQkMCKAIFZMuZfUtY6n2iyxaVr3rl+x5lU0CdG9x7KAeYydQGTMMAoGCCqGSM49\nBAMCA0cAMEQCIAR4fBmIBKW2jp0HbbabVepNtl1c7+6++riIrEBnoyIVAiBBvWmI\nyG02c5hu4wPAuVQMB7AU6tGSeYaWSAAo/ExunQ==\n-----END CERTIFICATE-----\n",
    }
]
```



# 实验记录

## 生成配置信息

> 在执行discover的时候需要一个身份，这个文件保存这个身份的信息，包括证书，私钥

![1591320918435](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric-Service Discovery\Service Discovery CLI.assets\1591320918435.png)



## 查询节点成员

> 查询的节点成员对应于通道

![1591320950077](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric-Service Discovery\Service Discovery CLI.assets\1591320950077.png)



> 查看证书信息

![1591321368239](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric-Service Discovery\Service Discovery CLI.assets\1591321368239.png)



## 配置查询

### 查询配置

> 列举了从MSP（成员服务提供者）ID到orderer端点的映射，还返回了`FabricMSPConfig` （我理解是包括了Orderer节点和组织的MSP、TLS证书信息），它可被用来通过SDK验证所有peer和orderer。这里的**证书是base64**编码的。

```bash
root@3e1d896695c5:/etc/hyperledger/fabric/discover# discover --configFile conf.yaml config --channel foochannel --server peer0.morg1.com:9001
2020-06-05 09:46:04.267 CST [bccsp] initBCCSP -> DEBU 001 Initialize BCCSP [SW]
2020-06-05 09:46:04.305 CST [grpc] DialContext -> DEBU 002 parsed scheme: ""
2020-06-05 09:46:04.305 CST [grpc] DialContext -> DEBU 003 scheme "" not registered, fallback to default scheme
2020-06-05 09:46:04.305 CST [grpc] watcher -> DEBU 004 ccResolverWrapper: sending new addresses to cc: [{peer0.morg1.com:9001 0  <nil>}]
2020-06-05 09:46:04.305 CST [grpc] switchBalancer -> DEBU 005 ClientConn switching balancer to "pick_first"
2020-06-05 09:46:04.305 CST [grpc] HandleSubConnStateChange -> DEBU 006 pickfirstBalancer: HandleSubConnStateChange: 0xc0002415a0, CONNECTING
2020-06-05 09:46:04.314 CST [grpc] HandleSubConnStateChange -> DEBU 007 pickfirstBalancer: HandleSubConnStateChange: 0xc0002415a0, READY
{
	"msps": {
		"MSP-orderer0.org1": {
			"name": "MSP-orderer0.org1",
			"root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNGekNDQWIyZ0F3SUJBZ0lVSmxhZEluQ1NsWkJZOWtGVEh2c0drNVUrRFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTRNakV3TUZvWERUTTFNRFV5TXpBNE1qRXdNRm93YURFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdSbUZpY21sak1Sa3dGd1lEVlFRREV4Qm1ZV0p5YVdNdFkyRXRjMlZ5CmRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVseFhTaExBMEJvZHl0TXlFaE9XVXlBaWgKZTVwZUdVQTZBa2ltY0ZmTjloT01KQmtKWjg5eHRHTUw0RzlzWmxjYXBXR0tpTTMxNGlZRSs1K2VPOFR4SjZORgpNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUl3SFFZRFZSME9CQllFCkZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlZNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURQQVVoQVRIK3IKb3BMMGRUemtFQm9jSWFmUjNWSU14REFHMGhTSm9rVkJmUUlnS3hEdmRyei9FZEFiWnFBSEdoVm94Qy9ZTUpPRgorWjZsRjFjTjhWcEo1Qkk9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"intermediate_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNNVENDQWRpZ0F3SUJBZ0lVY0NXbEpRRmVyRkY1UUt0ZW5xbDVBZi90Q1Zzd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTROVGd3TUZvWERUSTFNRFV5TlRBNU1ETXdNRm93WWpFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdZMnhwWlc1ME1STXdFUVlEVlFRREV3cHliMjkwTFdGa2JXbHVNRmt3CkV3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFamdUeWRMK2JXdUpzWk42UXc4NThWTG1jR0FmdnQyMlgKZjUyK2lRL0ZKZGphVk9SUGxsbWxKbi9NcFl3V1J5OHk2SGRmQlFKc1RielZQd2JjU0xUcVlhTm1NR1F3RGdZRApWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFNd0hRWURWUjBPQkJZRUZLYnM5M2lUCkVnVEVIRjE0S0lhbEpSbGVnanRuTUI4R0ExVWRJd1FZTUJhQUZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlYKTUFvR0NDcUdTTTQ5QkFNQ0EwY0FNRVFDSUhIa2kxTm9jT1RQREp1SzZNU0hOWUdHQ0hPNGNCdUpkeXlETjBpTgplUFM5QWlCUzVtdlpHNlRRM09Rd2E4WEJ0MEJmUEs2cGp6RDRJOFBIL25YM2NxQkFRQT09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"admins": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNRakNDQWVpZ0F3SUJBZ0lVR2JSUHpibHdPVGR2bVRNVmJHakM2ZzlTRjJnd0NnWUlLb1pJemowRUF3SXcKWWpFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1kyeHBaVzUwTVJNd0VRWURWUVFERXdweWIyOTBMV0ZrCmJXbHVNQjRYRFRJd01EVXlOakE0TlRnd01Gb1hEVEl4TURVeU5qQTVNRE13TUZvd1l6RUxNQWtHQTFVRUJoTUMKVlZNeEZ6QVZCZ05WQkFnVERrNXZjblJvSUVOaGNtOXNhVzVoTVJRd0VnWURWUVFLRXd0SWVYQmxjbXhsWkdkbApjakVQTUEwR0ExVUVDeE1HWTJ4cFpXNTBNUlF3RWdZRFZRUURFd3R0YjNKbk1TMWhaRzFwYmpCWk1CTUdCeXFHClNNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJPaVhWS1QyUHF0M01TTUN5SUt6ck41MHZJTUkvK1d3QU53UFdyemsKTHRwV29XdnBzVEh1WUNyQ3ZqUldRRjdvdGUwTFdzWEczTUkyaktiMW95VFJnclNqZXpCNU1BNEdBMVVkRHdFQgovd1FFQXdJSGdEQU1CZ05WSFJNQkFmOEVBakFBTUIwR0ExVWREZ1FXQkJTM0hhcHUzeERqUHhndjJKTEU1ZnpjCmxLT09EREFmQmdOVkhTTUVHREFXZ0JTbTdQZDRreElFeEJ4ZGVDaUdwU1VaWG9JN1p6QVpCZ05WSFJFRUVqQVEKZ2c1d1pXVnlNQzV2Y21jeExtTnZiVEFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBd2UzZGE0WFp2NHRtbDhsMQpPNmVBbTYyVURSampFMWtZUGVpcHZZZUMyc2dDSUFZTzlpVmI0TklDaEhCVDBNVEdTWkF5TVZGT0pBWXZZelp6CnFISTJ2YldRCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"crypto_config": {
				"signature_hash_family": "SHA2",
				"identity_identifier_hash_function": "SHA256"
			},
			"tls_root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUIvekNDQWFXZ0F3SUJBZ0lVQ0pDdGRPbWRyQUR2a0pFSmpRQXd6UmdlRktjd0NnWUlLb1pJemowRUF3SXcKWERFTE1Ba0dBMVVFQmhNQ1drZ3hFVEFQQmdOVkJBZ1RDRnBvWldwcFlXNW5NUkF3RGdZRFZRUUtFd2RUZFdOegpiMlowTVFzd0NRWURWUVFMRXdKSlZERWJNQmtHQTFVRUF4TVNjbTl2ZEMxMGJITXRZMkV0YzJWeWRtVnlNQjRYCkRUSXdNRFV5TmpBNE5UQXdNRm9YRFRNMU1EVXlNekE0TlRBd01Gb3dYREVMTUFrR0ExVUVCaE1DV2tneEVUQVAKQmdOVkJBZ1RDRnBvWldwcFlXNW5NUkF3RGdZRFZRUUtFd2RUZFdOemIyWjBNUXN3Q1FZRFZRUUxFd0pKVkRFYgpNQmtHQTFVRUF4TVNjbTl2ZEMxMGJITXRZMkV0YzJWeWRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBECkFRY0RRZ0FFWEh2QWlkWGtzbjlqbkIxZlZOSG14MkRQcjR6bjFNSlpzL2JjdHdCU2dNOXh5ZDlvemtGVUhjN2cKVUR2ZzJJdE50elVYb3VzenpUeW90Z25SNkQzTE02TkZNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZApFd0VCL3dRSU1BWUJBZjhDQVFJd0hRWURWUjBPQkJZRUZKOUR5ckZ1UmdPS1hkTTFKUXB2RkZNNXBEQUhNQW9HCkNDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFET0hTYXhsblNiVmpNemVPZU5XdXpvTU5vQUNRdWh1dkVEUWo5VnNTZWQKVFFJZ1ZkZ1E2cXo5QVNzcGg4UkdsNWRpM2VwMUZ2RDUxOVd5UW5FY3lhcm92UnM9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			]
		},
		"MSP-orderer0.org2": {
			"name": "MSP-orderer0.org2",
			"root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNGekNDQWIyZ0F3SUJBZ0lVSmxhZEluQ1NsWkJZOWtGVEh2c0drNVUrRFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTRNakV3TUZvWERUTTFNRFV5TXpBNE1qRXdNRm93YURFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdSbUZpY21sak1Sa3dGd1lEVlFRREV4Qm1ZV0p5YVdNdFkyRXRjMlZ5CmRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVseFhTaExBMEJvZHl0TXlFaE9XVXlBaWgKZTVwZUdVQTZBa2ltY0ZmTjloT01KQmtKWjg5eHRHTUw0RzlzWmxjYXBXR0tpTTMxNGlZRSs1K2VPOFR4SjZORgpNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUl3SFFZRFZSME9CQllFCkZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlZNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURQQVVoQVRIK3IKb3BMMGRUemtFQm9jSWFmUjNWSU14REFHMGhTSm9rVkJmUUlnS3hEdmRyei9FZEFiWnFBSEdoVm94Qy9ZTUpPRgorWjZsRjFjTjhWcEo1Qkk9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"intermediate_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNNakNDQWRpZ0F3SUJBZ0lVS0M0aW9oa2YxbjhncDF0bSt5Vk5qZTNNaFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTVNREl3TUZvWERUSTFNRFV5TlRBNU1EY3dNRm93WWpFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdZMnhwWlc1ME1STXdFUVlEVlFRREV3cHliMjkwTFdGa2JXbHVNRmt3CkV3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFeDZOV2NEbDBGVTIzUEI3V0wydTN4clpDelNEQ04vdWsKL21TL3RRcnY2cHk0VklyNjZtM1A2RDdoZElYMjdxNU1ab21TRE1SZi9rcGlBRHRRNVpmZFQ2Tm1NR1F3RGdZRApWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFNd0hRWURWUjBPQkJZRUZQMWNwNThMCnY2ajJHbDhZaHZ3S09LNVFDLzkzTUI4R0ExVWRJd1FZTUJhQUZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlYKTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDa3ArUzNKamx4WkNLSHN0a2lUZUpNdWtuWUozaU5XemVOMUxHMgpOV2VFOGdJZ1RlWG9xd3MvTVNPUUJoZ3BWL28yRGFDTnQvU3ZmWU9JVmFpbEJwRnZwcTA9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"admins": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNRakNDQWVpZ0F3SUJBZ0lVYlYxazBkUG5jeHUrQXRCSDZYSlU3K0M5N1l3d0NnWUlLb1pJemowRUF3SXcKWWpFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1kyeHBaVzUwTVJNd0VRWURWUVFERXdweWIyOTBMV0ZrCmJXbHVNQjRYRFRJd01EVXlOakE1TURNd01Gb1hEVEl4TURVeU5qQTVNRGd3TUZvd1l6RUxNQWtHQTFVRUJoTUMKVlZNeEZ6QVZCZ05WQkFnVERrNXZjblJvSUVOaGNtOXNhVzVoTVJRd0VnWURWUVFLRXd0SWVYQmxjbXhsWkdkbApjakVQTUEwR0ExVUVDeE1HWTJ4cFpXNTBNUlF3RWdZRFZRUURFd3R0YjNKbk1pMWhaRzFwYmpCWk1CTUdCeXFHClNNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJKdnBpS1pCZFFlT0doTW9reXdlMmJNUkt4WFlSN0N0S3kvTkVZQWcKaEFBcXJwenZqYUpNSlEvVlByaTlKTjhIdENmQ2puK00ydkQwWHN1SEJPMEgvWWFqZXpCNU1BNEdBMVVkRHdFQgovd1FFQXdJSGdEQU1CZ05WSFJNQkFmOEVBakFBTUIwR0ExVWREZ1FXQkJSRlNocXFSL1ViN283ZHlnWGtwemxoCnpDNENNVEFmQmdOVkhTTUVHREFXZ0JUOVhLZWZDNytvOWhwZkdJYjhDaml1VUF2L2R6QVpCZ05WSFJFRUVqQVEKZ2c1d1pXVnlNQzV2Y21jeUxtTnZiVEFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBdE5wdWZoQUk3UWJiS0M4cApnOWVwNytqSndSZnhBSlc2Ymo2eDhrZlhvQ1FDSUYwbEw3VUgyRWw3VFIvVWdOUHBkTGkxVFRuZFp1SENVcC9YCjQ5OFVXTjJyCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"crypto_config": {
				"signature_hash_family": "SHA2",
				"identity_identifier_hash_function": "SHA256"
			},
			"tls_root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUIvekNDQWFXZ0F3SUJBZ0lVQ0pDdGRPbWRyQUR2a0pFSmpRQXd6UmdlRktjd0NnWUlLb1pJemowRUF3SXcKWERFTE1Ba0dBMVVFQmhNQ1drZ3hFVEFQQmdOVkJBZ1RDRnBvWldwcFlXNW5NUkF3RGdZRFZRUUtFd2RUZFdOegpiMlowTVFzd0NRWURWUVFMRXdKSlZERWJNQmtHQTFVRUF4TVNjbTl2ZEMxMGJITXRZMkV0YzJWeWRtVnlNQjRYCkRUSXdNRFV5TmpBNE5UQXdNRm9YRFRNMU1EVXlNekE0TlRBd01Gb3dYREVMTUFrR0ExVUVCaE1DV2tneEVUQVAKQmdOVkJBZ1RDRnBvWldwcFlXNW5NUkF3RGdZRFZRUUtFd2RUZFdOemIyWjBNUXN3Q1FZRFZRUUxFd0pKVkRFYgpNQmtHQTFVRUF4TVNjbTl2ZEMxMGJITXRZMkV0YzJWeWRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBECkFRY0RRZ0FFWEh2QWlkWGtzbjlqbkIxZlZOSG14MkRQcjR6bjFNSlpzL2JjdHdCU2dNOXh5ZDlvemtGVUhjN2cKVUR2ZzJJdE50elVYb3VzenpUeW90Z25SNkQzTE02TkZNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZApFd0VCL3dRSU1BWUJBZjhDQVFJd0hRWURWUjBPQkJZRUZKOUR5ckZ1UmdPS1hkTTFKUXB2RkZNNXBEQUhNQW9HCkNDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFET0hTYXhsblNiVmpNemVPZU5XdXpvTU5vQUNRdWh1dkVEUWo5VnNTZWQKVFFJZ1ZkZ1E2cXo5QVNzcGg4UkdsNWRpM2VwMUZ2RDUxOVd5UW5FY3lhcm92UnM9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			]
		},
		"MSP-orderer0.org3": {
			"name": "MSP-orderer0.org3",
			"root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNGekNDQWIyZ0F3SUJBZ0lVSmxhZEluQ1NsWkJZOWtGVEh2c0drNVUrRFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTRNakV3TUZvWERUTTFNRFV5TXpBNE1qRXdNRm93YURFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdSbUZpY21sak1Sa3dGd1lEVlFRREV4Qm1ZV0p5YVdNdFkyRXRjMlZ5CmRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVseFhTaExBMEJvZHl0TXlFaE9XVXlBaWgKZTVwZUdVQTZBa2ltY0ZmTjloT01KQmtKWjg5eHRHTUw0RzlzWmxjYXBXR0tpTTMxNGlZRSs1K2VPOFR4SjZORgpNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUl3SFFZRFZSME9CQllFCkZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlZNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURQQVVoQVRIK3IKb3BMMGRUemtFQm9jSWFmUjNWSU14REFHMGhTSm9rVkJmUUlnS3hEdmRyei9FZEFiWnFBSEdoVm94Qy9ZTUpPRgorWjZsRjFjTjhWcEo1Qkk9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"intermediate_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNNVENDQWRpZ0F3SUJBZ0lVRVVXYkFSZ0NNWkNOMzZqQW5VUXpvUXkxTVpnd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTVNakF3TUZvWERUSTFNRFV5TlRBNU1qVXdNRm93WWpFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdZMnhwWlc1ME1STXdFUVlEVlFRREV3cHliMjkwTFdGa2JXbHVNRmt3CkV3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFWmlpQ3F3dXNBdzkzZFpucjkwVUt4ZFZKY3RURnFDT0QKdVZwNFJOb0JWOEFleEN2aThDVy9qMXZKMSt6UExQZGhFcTRVT3Q3YWIxTEJEZmdJdHVoV0pxTm1NR1F3RGdZRApWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFNd0hRWURWUjBPQkJZRUZMK2JwMElEClRGamljL2YxL04zVDdGenVqYnRTTUI4R0ExVWRJd1FZTUJhQUZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlYKTUFvR0NDcUdTTTQ5QkFNQ0EwY0FNRVFDSUdhWjlzYnJldzZ5UEZNK21XWFkyRExSOS9nNWNUOEc2Yy9xK29XZAp2TDR4QWlCZ0lpdzJVSEM3ZGM0YUtkRnBVSlZKT1o5QWQ5eUxVTlBKajhuZ1l5NEVBdz09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"admins": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNTekNDQWZHZ0F3SUJBZ0lVQkMwS0Nqc1ZVYk9aSGdBODhBNjVaSmxENnNzd0NnWUlLb1pJemowRUF3SXcKWWpFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1kyeHBaVzUwTVJNd0VRWURWUVFERXdweWIyOTBMV0ZrCmJXbHVNQjRYRFRJd01EVXlOakE1TWpFd01Gb1hEVEl4TURVeU5qQTVNall3TUZvd1l6RUxNQWtHQTFVRUJoTUMKVlZNeEZ6QVZCZ05WQkFnVERrNXZjblJvSUVOaGNtOXNhVzVoTVJRd0VnWURWUVFLRXd0SWVYQmxjbXhsWkdkbApjakVQTUEwR0ExVUVDeE1HWTJ4cFpXNTBNUlF3RWdZRFZRUURFd3R0YjNKbk15MWhaRzFwYmpCWk1CTUdCeXFHClNNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJGbXJVM2hOekVpTGRDeGRrallHODZ0R0dtT1NEUm9DdnlqS1A2aSsKR0tYWHVITkliZERXUVpVYXU5bW1UMzFCbDNpUHhsZGZoSFl2cEZadVF0dE9RWEdqZ1lNd2dZQXdEZ1lEVlIwUApBUUgvQkFRREFnZUFNQXdHQTFVZEV3RUIvd1FDTUFBd0hRWURWUjBPQkJZRUZGS08yTzZkd01BV29XZ2tSVkt6Cm9pZTJ4em9pTUI4R0ExVWRJd1FZTUJhQUZMK2JwMElEVEZqaWMvZjEvTjNUN0Z6dWpidFNNQ0FHQTFVZEVRUVoKTUJlQ0ZXeHZZMkZzYUc5emRDNXNiMk5oYkdSdmJXRnBiakFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBMUdhRQpoYjdVU0lDeXYxaTM5VzF6Q2d1L2k4bTl1Zll2WEN0TFNLYmE0Tm9DSUVUUHhOUzBFSE9CbzkyTVZySlhrbkdZCjFYM3lUZzBmUkNKT2pySTViVXF5Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"crypto_config": {
				"signature_hash_family": "SHA2",
				"identity_identifier_hash_function": "SHA256"
			},
			"tls_root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUIvekNDQWFXZ0F3SUJBZ0lVQ0pDdGRPbWRyQUR2a0pFSmpRQXd6UmdlRktjd0NnWUlLb1pJemowRUF3SXcKWERFTE1Ba0dBMVVFQmhNQ1drZ3hFVEFQQmdOVkJBZ1RDRnBvWldwcFlXNW5NUkF3RGdZRFZRUUtFd2RUZFdOegpiMlowTVFzd0NRWURWUVFMRXdKSlZERWJNQmtHQTFVRUF4TVNjbTl2ZEMxMGJITXRZMkV0YzJWeWRtVnlNQjRYCkRUSXdNRFV5TmpBNE5UQXdNRm9YRFRNMU1EVXlNekE0TlRBd01Gb3dYREVMTUFrR0ExVUVCaE1DV2tneEVUQVAKQmdOVkJBZ1RDRnBvWldwcFlXNW5NUkF3RGdZRFZRUUtFd2RUZFdOemIyWjBNUXN3Q1FZRFZRUUxFd0pKVkRFYgpNQmtHQTFVRUF4TVNjbTl2ZEMxMGJITXRZMkV0YzJWeWRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBECkFRY0RRZ0FFWEh2QWlkWGtzbjlqbkIxZlZOSG14MkRQcjR6bjFNSlpzL2JjdHdCU2dNOXh5ZDlvemtGVUhjN2cKVUR2ZzJJdE50elVYb3VzenpUeW90Z25SNkQzTE02TkZNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZApFd0VCL3dRSU1BWUJBZjhDQVFJd0hRWURWUjBPQkJZRUZKOUR5ckZ1UmdPS1hkTTFKUXB2RkZNNXBEQUhNQW9HCkNDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFET0hTYXhsblNiVmpNemVPZU5XdXpvTU5vQUNRdWh1dkVEUWo5VnNTZWQKVFFJZ1ZkZ1E2cXo5QVNzcGg4UkdsNWRpM2VwMUZ2RDUxOVd5UW5FY3lhcm92UnM9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			]
		},
		"MSP-org1": {
			"name": "MSP-org1",
			"root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNGekNDQWIyZ0F3SUJBZ0lVSmxhZEluQ1NsWkJZOWtGVEh2c0drNVUrRFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTRNakV3TUZvWERUTTFNRFV5TXpBNE1qRXdNRm93YURFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdSbUZpY21sak1Sa3dGd1lEVlFRREV4Qm1ZV0p5YVdNdFkyRXRjMlZ5CmRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVseFhTaExBMEJvZHl0TXlFaE9XVXlBaWgKZTVwZUdVQTZBa2ltY0ZmTjloT01KQmtKWjg5eHRHTUw0RzlzWmxjYXBXR0tpTTMxNGlZRSs1K2VPOFR4SjZORgpNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUl3SFFZRFZSME9CQllFCkZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlZNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURQQVVoQVRIK3IKb3BMMGRUemtFQm9jSWFmUjNWSU14REFHMGhTSm9rVkJmUUlnS3hEdmRyei9FZEFiWnFBSEdoVm94Qy9ZTUpPRgorWjZsRjFjTjhWcEo1Qkk9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"intermediate_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNNVENDQWRpZ0F3SUJBZ0lVY0NXbEpRRmVyRkY1UUt0ZW5xbDVBZi90Q1Zzd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTROVGd3TUZvWERUSTFNRFV5TlRBNU1ETXdNRm93WWpFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdZMnhwWlc1ME1STXdFUVlEVlFRREV3cHliMjkwTFdGa2JXbHVNRmt3CkV3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFamdUeWRMK2JXdUpzWk42UXc4NThWTG1jR0FmdnQyMlgKZjUyK2lRL0ZKZGphVk9SUGxsbWxKbi9NcFl3V1J5OHk2SGRmQlFKc1RielZQd2JjU0xUcVlhTm1NR1F3RGdZRApWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFNd0hRWURWUjBPQkJZRUZLYnM5M2lUCkVnVEVIRjE0S0lhbEpSbGVnanRuTUI4R0ExVWRJd1FZTUJhQUZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlYKTUFvR0NDcUdTTTQ5QkFNQ0EwY0FNRVFDSUhIa2kxTm9jT1RQREp1SzZNU0hOWUdHQ0hPNGNCdUpkeXlETjBpTgplUFM5QWlCUzVtdlpHNlRRM09Rd2E4WEJ0MEJmUEs2cGp6RDRJOFBIL25YM2NxQkFRQT09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"admins": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNRakNDQWVpZ0F3SUJBZ0lVR2JSUHpibHdPVGR2bVRNVmJHakM2ZzlTRjJnd0NnWUlLb1pJemowRUF3SXcKWWpFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1kyeHBaVzUwTVJNd0VRWURWUVFERXdweWIyOTBMV0ZrCmJXbHVNQjRYRFRJd01EVXlOakE0TlRnd01Gb1hEVEl4TURVeU5qQTVNRE13TUZvd1l6RUxNQWtHQTFVRUJoTUMKVlZNeEZ6QVZCZ05WQkFnVERrNXZjblJvSUVOaGNtOXNhVzVoTVJRd0VnWURWUVFLRXd0SWVYQmxjbXhsWkdkbApjakVQTUEwR0ExVUVDeE1HWTJ4cFpXNTBNUlF3RWdZRFZRUURFd3R0YjNKbk1TMWhaRzFwYmpCWk1CTUdCeXFHClNNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJPaVhWS1QyUHF0M01TTUN5SUt6ck41MHZJTUkvK1d3QU53UFdyemsKTHRwV29XdnBzVEh1WUNyQ3ZqUldRRjdvdGUwTFdzWEczTUkyaktiMW95VFJnclNqZXpCNU1BNEdBMVVkRHdFQgovd1FFQXdJSGdEQU1CZ05WSFJNQkFmOEVBakFBTUIwR0ExVWREZ1FXQkJTM0hhcHUzeERqUHhndjJKTEU1ZnpjCmxLT09EREFmQmdOVkhTTUVHREFXZ0JTbTdQZDRreElFeEJ4ZGVDaUdwU1VaWG9JN1p6QVpCZ05WSFJFRUVqQVEKZ2c1d1pXVnlNQzV2Y21jeExtTnZiVEFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBd2UzZGE0WFp2NHRtbDhsMQpPNmVBbTYyVURSampFMWtZUGVpcHZZZUMyc2dDSUFZTzlpVmI0TklDaEhCVDBNVEdTWkF5TVZGT0pBWXZZelp6CnFISTJ2YldRCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"crypto_config": {
				"signature_hash_family": "SHA2",
				"identity_identifier_hash_function": "SHA256"
			}
		},
		"MSP-org2": {
			"name": "MSP-org2",
			"root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNGekNDQWIyZ0F3SUJBZ0lVSmxhZEluQ1NsWkJZOWtGVEh2c0drNVUrRFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTRNakV3TUZvWERUTTFNRFV5TXpBNE1qRXdNRm93YURFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdSbUZpY21sak1Sa3dGd1lEVlFRREV4Qm1ZV0p5YVdNdFkyRXRjMlZ5CmRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVseFhTaExBMEJvZHl0TXlFaE9XVXlBaWgKZTVwZUdVQTZBa2ltY0ZmTjloT01KQmtKWjg5eHRHTUw0RzlzWmxjYXBXR0tpTTMxNGlZRSs1K2VPOFR4SjZORgpNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUl3SFFZRFZSME9CQllFCkZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlZNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURQQVVoQVRIK3IKb3BMMGRUemtFQm9jSWFmUjNWSU14REFHMGhTSm9rVkJmUUlnS3hEdmRyei9FZEFiWnFBSEdoVm94Qy9ZTUpPRgorWjZsRjFjTjhWcEo1Qkk9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"intermediate_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNNakNDQWRpZ0F3SUJBZ0lVS0M0aW9oa2YxbjhncDF0bSt5Vk5qZTNNaFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTVNREl3TUZvWERUSTFNRFV5TlRBNU1EY3dNRm93WWpFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdZMnhwWlc1ME1STXdFUVlEVlFRREV3cHliMjkwTFdGa2JXbHVNRmt3CkV3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFeDZOV2NEbDBGVTIzUEI3V0wydTN4clpDelNEQ04vdWsKL21TL3RRcnY2cHk0VklyNjZtM1A2RDdoZElYMjdxNU1ab21TRE1SZi9rcGlBRHRRNVpmZFQ2Tm1NR1F3RGdZRApWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFNd0hRWURWUjBPQkJZRUZQMWNwNThMCnY2ajJHbDhZaHZ3S09LNVFDLzkzTUI4R0ExVWRJd1FZTUJhQUZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlYKTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDa3ArUzNKamx4WkNLSHN0a2lUZUpNdWtuWUozaU5XemVOMUxHMgpOV2VFOGdJZ1RlWG9xd3MvTVNPUUJoZ3BWL28yRGFDTnQvU3ZmWU9JVmFpbEJwRnZwcTA9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"admins": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNRakNDQWVpZ0F3SUJBZ0lVYlYxazBkUG5jeHUrQXRCSDZYSlU3K0M5N1l3d0NnWUlLb1pJemowRUF3SXcKWWpFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1kyeHBaVzUwTVJNd0VRWURWUVFERXdweWIyOTBMV0ZrCmJXbHVNQjRYRFRJd01EVXlOakE1TURNd01Gb1hEVEl4TURVeU5qQTVNRGd3TUZvd1l6RUxNQWtHQTFVRUJoTUMKVlZNeEZ6QVZCZ05WQkFnVERrNXZjblJvSUVOaGNtOXNhVzVoTVJRd0VnWURWUVFLRXd0SWVYQmxjbXhsWkdkbApjakVQTUEwR0ExVUVDeE1HWTJ4cFpXNTBNUlF3RWdZRFZRUURFd3R0YjNKbk1pMWhaRzFwYmpCWk1CTUdCeXFHClNNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJKdnBpS1pCZFFlT0doTW9reXdlMmJNUkt4WFlSN0N0S3kvTkVZQWcKaEFBcXJwenZqYUpNSlEvVlByaTlKTjhIdENmQ2puK00ydkQwWHN1SEJPMEgvWWFqZXpCNU1BNEdBMVVkRHdFQgovd1FFQXdJSGdEQU1CZ05WSFJNQkFmOEVBakFBTUIwR0ExVWREZ1FXQkJSRlNocXFSL1ViN283ZHlnWGtwemxoCnpDNENNVEFmQmdOVkhTTUVHREFXZ0JUOVhLZWZDNytvOWhwZkdJYjhDaml1VUF2L2R6QVpCZ05WSFJFRUVqQVEKZ2c1d1pXVnlNQzV2Y21jeUxtTnZiVEFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBdE5wdWZoQUk3UWJiS0M4cApnOWVwNytqSndSZnhBSlc2Ymo2eDhrZlhvQ1FDSUYwbEw3VUgyRWw3VFIvVWdOUHBkTGkxVFRuZFp1SENVcC9YCjQ5OFVXTjJyCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"crypto_config": {
				"signature_hash_family": "SHA2",
				"identity_identifier_hash_function": "SHA256"
			}
		},
		"MSP-org3": {
			"name": "MSP-org3",
			"root_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNGekNDQWIyZ0F3SUJBZ0lVSmxhZEluQ1NsWkJZOWtGVEh2c0drNVUrRFdrd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTRNakV3TUZvWERUTTFNRFV5TXpBNE1qRXdNRm93YURFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdSbUZpY21sak1Sa3dGd1lEVlFRREV4Qm1ZV0p5YVdNdFkyRXRjMlZ5CmRtVnlNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVseFhTaExBMEJvZHl0TXlFaE9XVXlBaWgKZTVwZUdVQTZBa2ltY0ZmTjloT01KQmtKWjg5eHRHTUw0RzlzWmxjYXBXR0tpTTMxNGlZRSs1K2VPOFR4SjZORgpNRU13RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUl3SFFZRFZSME9CQllFCkZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlZNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUURQQVVoQVRIK3IKb3BMMGRUemtFQm9jSWFmUjNWSU14REFHMGhTSm9rVkJmUUlnS3hEdmRyei9FZEFiWnFBSEdoVm94Qy9ZTUpPRgorWjZsRjFjTjhWcEo1Qkk9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"intermediate_certs": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNNVENDQWRpZ0F3SUJBZ0lVRVVXYkFSZ0NNWkNOMzZqQW5VUXpvUXkxTVpnd0NnWUlLb1pJemowRUF3SXcKYURFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1JtRmljbWxqTVJrd0Z3WURWUVFERXhCbVlXSnlhV010ClkyRXRjMlZ5ZG1WeU1CNFhEVEl3TURVeU5qQTVNakF3TUZvWERUSTFNRFV5TlRBNU1qVXdNRm93WWpFTE1Ba0cKQTFVRUJoTUNWVk14RnpBVkJnTlZCQWdURGs1dmNuUm9JRU5oY205c2FXNWhNUlF3RWdZRFZRUUtFd3RJZVhCbApjbXhsWkdkbGNqRVBNQTBHQTFVRUN4TUdZMnhwWlc1ME1STXdFUVlEVlFRREV3cHliMjkwTFdGa2JXbHVNRmt3CkV3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFWmlpQ3F3dXNBdzkzZFpucjkwVUt4ZFZKY3RURnFDT0QKdVZwNFJOb0JWOEFleEN2aThDVy9qMXZKMSt6UExQZGhFcTRVT3Q3YWIxTEJEZmdJdHVoV0pxTm1NR1F3RGdZRApWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFNd0hRWURWUjBPQkJZRUZMK2JwMElEClRGamljL2YxL04zVDdGenVqYnRTTUI4R0ExVWRJd1FZTUJhQUZOVnJCWWxRYitsM21zcnc3T1lyay92WUlhTlYKTUFvR0NDcUdTTTQ5QkFNQ0EwY0FNRVFDSUdhWjlzYnJldzZ5UEZNK21XWFkyRExSOS9nNWNUOEc2Yy9xK29XZAp2TDR4QWlCZ0lpdzJVSEM3ZGM0YUtkRnBVSlZKT1o5QWQ5eUxVTlBKajhuZ1l5NEVBdz09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"admins": [
				"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNTekNDQWZHZ0F3SUJBZ0lVQkMwS0Nqc1ZVYk9aSGdBODhBNjVaSmxENnNzd0NnWUlLb1pJemowRUF3SXcKWWpFTE1Ba0dBMVVFQmhNQ1ZWTXhGekFWQmdOVkJBZ1REazV2Y25Sb0lFTmhjbTlzYVc1aE1SUXdFZ1lEVlFRSwpFd3RJZVhCbGNteGxaR2RsY2pFUE1BMEdBMVVFQ3hNR1kyeHBaVzUwTVJNd0VRWURWUVFERXdweWIyOTBMV0ZrCmJXbHVNQjRYRFRJd01EVXlOakE1TWpFd01Gb1hEVEl4TURVeU5qQTVNall3TUZvd1l6RUxNQWtHQTFVRUJoTUMKVlZNeEZ6QVZCZ05WQkFnVERrNXZjblJvSUVOaGNtOXNhVzVoTVJRd0VnWURWUVFLRXd0SWVYQmxjbXhsWkdkbApjakVQTUEwR0ExVUVDeE1HWTJ4cFpXNTBNUlF3RWdZRFZRUURFd3R0YjNKbk15MWhaRzFwYmpCWk1CTUdCeXFHClNNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJGbXJVM2hOekVpTGRDeGRrallHODZ0R0dtT1NEUm9DdnlqS1A2aSsKR0tYWHVITkliZERXUVpVYXU5bW1UMzFCbDNpUHhsZGZoSFl2cEZadVF0dE9RWEdqZ1lNd2dZQXdEZ1lEVlIwUApBUUgvQkFRREFnZUFNQXdHQTFVZEV3RUIvd1FDTUFBd0hRWURWUjBPQkJZRUZGS08yTzZkd01BV29XZ2tSVkt6Cm9pZTJ4em9pTUI4R0ExVWRJd1FZTUJhQUZMK2JwMElEVEZqaWMvZjEvTjNUN0Z6dWpidFNNQ0FHQTFVZEVRUVoKTUJlQ0ZXeHZZMkZzYUc5emRDNXNiMk5oYkdSdmJXRnBiakFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBMUdhRQpoYjdVU0lDeXYxaTM5VzF6Q2d1L2k4bTl1Zll2WEN0TFNLYmE0Tm9DSUVUUHhOUzBFSE9CbzkyTVZySlhrbkdZCjFYM3lUZzBmUkNKT2pySTViVXF5Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
			],
			"crypto_config": {
				"signature_hash_family": "SHA2",
				"identity_identifier_hash_function": "SHA256"
			}
		}
	},
	"orderers": {
		"MSP-orderer0.org1": {
			"endpoint": [
				{
					"host": "orderer0.morg1.com",
					"port": 8000
				},
				{
					"host": "orderer0.morg2.com",
					"port": 8010
				},
				{
					"host": "orderer0.morg3.com",
					"port": 8020
				}
			]
		},
		"MSP-orderer0.org2": {
			"endpoint": [
				{
					"host": "orderer0.morg1.com",
					"port": 8000
				},
				{
					"host": "orderer0.morg2.com",
					"port": 8010
				},
				{
					"host": "orderer0.morg3.com",
					"port": 8020
				}
			]
		},
		"MSP-orderer0.org3": {
			"endpoint": [
				{
					"host": "orderer0.morg1.com",
					"port": 8000
				},
				{
					"host": "orderer0.morg2.com",
					"port": 8010
				},
				{
					"host": "orderer0.morg3.com",
					"port": 8020
				}
			]
		}
	}
}

```



### 查看base64编码证书信息

```bash
$ discover --configFile conf.yaml config --channel mychannel  --server peer0.org1.example.com:7051 | jq .msps.OrdererOrg.root_certs[0] | sed "s/\"//g" | base64 --decode | openssl x509 -text -noout
```

> 用jq base64解码，在通过openssl命令进行查看
>
> 这里得出json的key中最好不要有“.”，有点会导致jq解析不了（或者可以通过一种方式定位json的层级）。目前只能拷出来解析证书。

![1591322777209](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric-Service Discovery\Service Discovery CLI.assets\1591322777209.png)



## 背书者查询

> 上述文档中有 --chaincode --collection --noPrivateReads等参数，需要注意。
>
> 这里只用了--chaincode来指定查询那个链码的背书

![1591323344721](F:\Maple\Ztudents\GraduateS\SucSoftware\BlockChainProject\Mission\Fabric-Service Discovery\Service Discovery CLI.assets\1591323344721.png)

该输出的具体内容可以看另一篇文档，Service Discovery。



## 不采用配置文件进行查询例子

```bash
$ discover --peerTLSCA tls/ca.crt --userKey msp/keystore/cf31339d09e8311ac9ca5ed4e27a104a7f82f1e5904b3296a170ba4725ffde0d_sk --userCert msp/signcerts/Admin\@org1.example.com-cert.pem --MSP Org1MSP --tlsCert tls/client.crt --tlsKey tls/client.key peers --server peer0.org1.example.com:7051
```

