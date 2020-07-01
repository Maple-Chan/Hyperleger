package com.cg.gateway.serviceDiscovery;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hyperledger.fabric.sdk.Channel.DiscoveryOptions.createDiscoveryOptions;

public class ServiceDiscoveryDemo {
    private static final long waitTime = 10000;

    public static void main(String[] args) throws Exception {
        String mspId = "MSP-org1";
        String chaincodeName = "fabcar";
        String userName = "dzw123";
        String keyFile = "src/main/resources/msp/keystore/4a0fb5be341a53c9b751942397a1bf48715a9ba8bfaabd1728d86aeb779dadd7_sk";
        String certFile = "src/main/resources/msp/signcerts/cert.pem";

        final HFClient client = HFClient.createNewInstance();

        FabricUser user = new FabricUser(userName, mspId, keyFile, certFile);
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(user);

        Channel channel = client.newChannel("foochannel"); //create channel that will be discovered.

        //create peer0org1
        String peer0org1TLSCert = "src/main/resources/msp/tlscerts/tlscert.pem";
        Properties peer0org1_properties = new Properties();
        peer0org1_properties.put("pemBytes", Files.readAllBytes(Paths.get(peer0org1TLSCert)));
        peer0org1_properties.setProperty("sslProvider", "openSSL");
        peer0org1_properties.setProperty("negotiationType", "TLS");
        peer0org1_properties.setProperty("trustServerCertificate", "true");
        String peer0org1Name = "peer0.morg1.com";

        String peer0org1URL = "grpcs://peer0.morg1.com:9001";
        // String peer0org1URL = "grpcs://172.18.21.78:9001";
        peer0org1_properties.setProperty("hostnameOverride", peer0org1Name);

        Peer peer0org1 = client.newPeer(peer0org1Name, peer0org1URL, peer0org1_properties);



        /*//create peer0org2
        String peer0org2TLSCert = "src/main/resources/msp/tlscerts/tlscert.pem";
        Properties peer0org2_properties = new Properties();
        peer0org2_properties.put("pemBytes", Files.readAllBytes(Paths.get(peer0org2TLSCert)));
        peer0org2_properties.setProperty("sslProvider", "openSSL");
        peer0org2_properties.setProperty("negotiationType", "TLS");
        peer0org2_properties.setProperty("trustServerCertificate", "true");
        String peer0org2Name = "peer0.morg2.com";
        // String peer0org2URL = "grpcs://172.18.21.79:9011";
        String peer0org2URL = "grpcs://peer0.morg2.com:9011";
        peer0org2_properties.setProperty("hostnameOverride", peer0org2Name);
        Peer peer0org2 = client.newPeer(peer0org2Name, peer0org2URL, peer0org2_properties);

        //create peer0org3
        String peer0org3TLSCert = "src/main/resources/msp/tlscerts/tlscert.pem";
        Properties peer0org3_properties = new Properties();
        peer0org3_properties.put("pemBytes", Files.readAllBytes(Paths.get(peer0org3TLSCert)));
        peer0org3_properties.setProperty("sslProvider", "openSSL");
        peer0org3_properties.setProperty("negotiationType", "TLS");
        peer0org3_properties.setProperty("trustServerCertificate", "true");
        String peer0org3Name = "peer0.morg3.com";
        //String peer0org3URL = "grpcs://172.18.21.79:9021";
        String peer0org3URL = "grpcs://peer0.morg3.com:9021";
        peer0org3_properties.setProperty("hostnameOverride", peer0org3Name);
        Peer peer0org3 = client.newPeer(peer0org3Name, peer0org3URL, peer0org3_properties);*/

        //channel.addPeer(peer0org1);
        channel.addPeer(peer0org1, Channel.PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.SERVICE_DISCOVERY, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.EVENT_SOURCE, Peer.PeerRole.CHAINCODE_QUERY)));
        //channel.addPeer(peer0org2, Channel.PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.SERVICE_DISCOVERY, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.EVENT_SOURCE, Peer.PeerRole.CHAINCODE_QUERY)));
        //channel.addPeer(peer0org3, Channel.PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.SERVICE_DISCOVERY, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.EVENT_SOURCE, Peer.PeerRole.CHAINCODE_QUERY)));

        // foo.setServiceDiscoveryProperties(sdprops);  IT里面的，需要吗
        // channel.setServiceDiscoveryProperties(peer0org1_properties);

        //init channel
        channel.initialize();

        //insertByChaincode(client,chaincodeName,channel);
        queryByChaincode(client, chaincodeName, channel);
    }

    public static void queryByChaincode(HFClient client, String chaincodeName , Channel channel) throws Exception {

        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).build();
        System.out.println("chiancodeId = " + chaincodeID);

        //build args
        ArrayList<String> argsList = new ArrayList<>();
        String function = "queryAllCars";
        executeTransaction(client, channel, chaincodeID,false, function, argsList);
    }

    public static void insertByChaincode(HFClient client, String chaincodeName , Channel channel) throws Exception {
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).build();

        ArrayList<String> argsList = new ArrayList<>();
        argsList.add("CAR10");
        argsList.add("TEST1");
        argsList.add("TEST2");
        argsList.add("TESTColor");
        argsList.add("TESTName");

        String function = "createCar";
        executeTransaction(client, channel, chaincodeID,true ,function, argsList);

    }


    private static void executeTransaction(HFClient client, Channel channel, ChaincodeID chaincodeID, boolean invoke, String func, ArrayList<String> args) throws Exception {
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        // transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);

        transactionProposalRequest.setFcn(func);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setProposalWaitTime(waitTime);
        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();
        // java sdk
        // 通常会发送交易请求给所有peer节点，如果有一些peer节点宕机，但是有Response到达了背书节点的话，我们可以不选择重新发送交易请求，使用服务发现功能可以实现这一目标。
        Collection<ProposalResponse> transactionPropResp;
        // 确保配置有服务发现功能的peer节点数量大于0
        if (channel.getPeers(EnumSet.of(Peer.PeerRole.SERVICE_DISCOVERY)).size() > 0) {
            System.out.println("配置有服务发现功能的peer节点数量：" + channel.getPeers(EnumSet.of(Peer.PeerRole.SERVICE_DISCOVERY)).size());
            // 配置服务发现在fabric网络中，使用服务发现来寻找背书节点（endorsing peers）
            Channel.DiscoveryOptions discoveryOptions = Channel.DiscoveryOptions.createDiscoveryOptions();
            // 随机选取满足背书策略的peer节点组合 ENDORSEMENT_SELECTION_RANDOM
            // 选取满足背书策略的，状态最新、块高最大的peer节点组合 ENDORSEMENT_SELECTION_LEAST_REQUIRED_BLOCKHEIGHT
            discoveryOptions.setEndorsementSelector(ServiceDiscovery.EndorsementSelector.ENDORSEMENT_SELECTION_RANDOM);
            // discoveryOptions.setEndorsementSelector(ServiceDiscovery.EndorsementSelector.ENDORSEMENT_SELECTION_LEAST_REQUIRED_BLOCKHEIGHT);
            // setForceDiscovery true :每一次发送proposal时都调用discovery服务获取peer列表，会有一定的资源消耗
            // setForceDiscovery false :发送proposal时使用discovery服务缓存的peer列表，默认2分钟刷新一次
            discoveryOptions.setForceDiscovery(false);
            // setInspectResults true: 关闭SDK 背书策略检查，由应用逻辑进行判断
            // false:SDK 自动进行背书策略检查，不满足抛出异常
            discoveryOptions.setInspectResults(true);
            transactionPropResp = channel.sendTransactionProposalToEndorsers(transactionProposalRequest, discoveryOptions);
        } else {
            System.out.println("peers:" + channel.getPeers(EnumSet.of(Peer.PeerRole.SERVICE_DISCOVERY)).size());
            transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)));
        }

        for (ProposalResponse response : transactionPropResp) {
            System.out.println("ChaincodeActionResponseStatus:" + response.getChaincodeActionResponseStatus());
            System.out.println("ChaincodeActionResponsePayload:" + Arrays.toString(response.getChaincodeActionResponsePayload()));
            System.out.println("" + response.getProposalResponse());
            //System.out.println();
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                String payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                if (payload.isEmpty()) {
                    System.out.println("******************************************************************************");
                    System.out.println(String.format("[√] 得到成功响应从 peer %s", response.getPeer().getName()));
                } else {
                    System.out.println("******************************************************************************");
                    System.out.println(
                            String.format("[√] 得到成功响应从 peer %s => payload: %s", response.getPeer().getName(), payload));
                }
                successful.add(response);
            } else {
                String status = response.getStatus().toString();
                String msg = response.getMessage();
                System.out.println(String.format("[×] 响应失败从 peer %s => %s: %s ", response.getPeer().getName(), status, msg));
                failed.add(response);
            }
        }

        if (invoke) {
            Channel.TransactionOptions opts = new Channel.TransactionOptions();
            Channel.NOfEvents nOfEvents = Channel.NOfEvents.createNofEvents();
            nOfEvents.addPeers(channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)));
            nOfEvents.setN(1);
            opts.nOfEvents(nOfEvents);
            System.out.println("向orderers发送交易...");
            channel.sendTransaction(successful, opts).thenApply(transactionEvent -> {
                System.out.println("Orderer 响应: txid" + transactionEvent.getTransactionID());
                System.out.println("Orderer 响应: 区块编号: " + transactionEvent.getBlockEvent().getBlockNumber());
                return null;
            }).exceptionally(e -> {
                System.out.println("Orderer exception happened: "+ e);
                return null;
            }).get(waitTime, TimeUnit.SECONDS);
        }

    }


}
