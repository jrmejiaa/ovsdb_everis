/*
 * Copyright 2020-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everis.app;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.onlab.packet.IpAddress;
import org.onlab.packet.TpPort;
import org.onlab.packet.Ethernet;
import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Path;
import org.onosproject.net.Link;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.net.behaviour.BridgeName;
import org.onosproject.net.behaviour.BridgeConfig;
import org.onosproject.net.behaviour.BridgeDescription;
import org.onosproject.net.behaviour.TunnelDescription;
import org.onosproject.net.behaviour.TunnelEndPoints;
import org.onosproject.net.behaviour.TunnelKey;
import org.onosproject.net.behaviour.DefaultBridgeDescription;
import org.onosproject.net.behaviour.DefaultTunnelDescription;
import org.onosproject.net.behaviour.ControllerInfo;
import org.onosproject.net.behaviour.ControllerConfig;
import org.onosproject.net.behaviour.DefaultPatchDescription;
import org.onosproject.net.behaviour.PatchDescription;
import org.onosproject.net.behaviour.InterfaceConfig;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.PathIntent;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.cluster.ClusterService;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DriverService;
import org.onosproject.ovsdb.controller.OvsdbController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.everis.app.OvsdbNodeConfig.OvsdbNode;
import org.everis.app.OvsdbRestException.BridgeNotFoundException;
import org.everis.app.OvsdbRestException.BridgeAlreadyExistsException;
import org.everis.app.OvsdbRestException.OvsdbDeviceException;

import static org.onosproject.net.DeviceId.deviceId;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true,
           service = {OvsdbBridgeService.class},
            property = {
                "someProperty=Some Default String Value",
            })
public class AppComponent implements OvsdbBridgeService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Some configurable property. */
    private String someProperty;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private OvsdbController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private DeviceAdminService adminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private DriverService driverService;

    private static final int DPID_BEGIN = 4;
    private static final int OFPORT = 6633;
    private static final TpPort OVSPORT = TpPort.tpPort(6640);
    private final AtomicLong datapathId = new AtomicLong(DPID_BEGIN);
    private static final ProviderId PROVIDER_ID = new ProviderId("AppComponent",
            "org.onosproject.net.intent");

    // {bridgeName: datapathId} structure to manage the creation/deletion of bridges
    private Map<String, DeviceId> bridgeIds = Maps.newConcurrentMap();

    public AppComponent() {
    }

    @Activate
    protected void activate() {
        cfgService.registerProperties(getClass());
        log.info("The App was successfully activated");
    }

    @Deactivate
    protected void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        log.info("The App was successfully deactivated");
    }

    @Modified
    public void modified() {
        log.info("Reconfigured");
    }

    @Override
    public void createBridge(IpAddress ovsdbAddress, String bridgeName)
            throws OvsdbDeviceException, BridgeAlreadyExistsException {
        OvsdbNode ovsdbNode = new OvsdbNode(ovsdbAddress, OVSPORT);

        // Get all the bridge devices
        getAllBridges();
        // construct a unique dev id'
        DeviceId dpid = getNextUniqueDatapathId(datapathId);

        if (isBridgeCreated(bridgeName)) {
            log.warn("A bridge with this name already exists, aborting.");
            throw new BridgeAlreadyExistsException();
        }

        List<ControllerInfo> controllers = new ArrayList<>();
        Sets.newHashSet(clusterService.getNodes()).forEach(controller -> {
            ControllerInfo ctrlInfo = new ControllerInfo(controller.ip(), OFPORT, "tcp");
            controllers.add(ctrlInfo);
            log.info("controller {}:{} added", ctrlInfo.ip().toString(), ctrlInfo.port());
        });
        try {
            Device device = deviceService.getDevice(ovsdbNode.ovsdbId());
            if (device == null) {
                log.warn("Ovsdb device not found, aborting.");
                throw new OvsdbDeviceException("Ovsdb device not found");
            }
            if (device.is(BridgeConfig.class)) {
                BridgeConfig bridgeConfig = device.as(BridgeConfig.class);
                BridgeDescription bridgeDescription = DefaultBridgeDescription.builder()
                        .name(bridgeName)
                        .datapathId(dpid.toString())
                        .controllers(controllers)
                        .build();
                bridgeConfig.addBridge(bridgeDescription);
                bridgeIds.put(bridgeName, bridgeDescription.deviceId().get());
                log.info("Correctly created bridge {} at {}", bridgeName, ovsdbAddress);
            } else {
                log.warn("The bridging behaviour is not supported in device {}", device.id());
                throw new OvsdbDeviceException(
                        "The bridging behaviour is not supported in device " + device.id()
                );
            }
        } catch (ItemNotFoundException e) {
            log.warn("Failed to create integration bridge on {}", ovsdbNode.ovsdbIp());
            throw new OvsdbDeviceException("Error with ovsdb device: item not found");
        }
    }

    @Override
    public void deleteBridge(IpAddress ovsdbAddress, String bridgeName)
            throws OvsdbDeviceException, BridgeNotFoundException {

        log.warn("Deleting bridge {} at {}", bridgeName, ovsdbAddress);
        OvsdbNode ovsdbNode = new OvsdbNode(ovsdbAddress, OVSPORT);

        // Get all the bridge devices
        getAllBridges();
        // Get the device ID using the name to delete the bridge
        DeviceId deviceId = bridgeIds.get(bridgeName);
        if (deviceId == null) {
            log.warn("No bridge with this name, aborting.");
            throw new BridgeNotFoundException();
        }
        try {
            Device device = deviceService.getDevice(ovsdbNode.ovsdbId());
            if (device == null) {
                log.warn("Ovsdb device not found, aborting.");
                throw new OvsdbDeviceException("Ovsdb device not found");
            }
            if (device.is(BridgeConfig.class)) {

                // unregister bridge from its controllers
                deviceId = DeviceId.deviceId(deviceId.uri());
                DriverHandler h = driverService.createHandler(deviceId);
                ControllerConfig controllerConfig = h.behaviour(ControllerConfig.class);
                controllerConfig.setControllers(new ArrayList<>());

                // remove bridge from onos devices
                adminService.removeDevice(deviceId);

                // remove bridge from ovsdb
                BridgeConfig bridgeConfig = device.as(BridgeConfig.class);
                bridgeConfig.deleteBridge(BridgeName.bridgeName(bridgeName));
                bridgeIds.remove(bridgeName);

                log.info("Correctly deleted bridge {} at {}", bridgeName, ovsdbAddress);
            } else {
                log.warn("The bridging behaviour is not supported in device {}", device.id());
                throw new OvsdbDeviceException(
                        "The bridging behaviour is not supported in device " + device.id()
                );
            }
        } catch (ItemNotFoundException e) {
            log.warn("Failed to delete bridge on {}", ovsdbNode.ovsdbIp());
            throw new OvsdbDeviceException("Error with ovsdb device: item not found");
        }
    }

    @Override
    public void addPort(IpAddress ovsdbAddress, String bridgeName, String portName)
            throws OvsdbDeviceException, BridgeNotFoundException {
        log.info("Adding port {} to bridge {} at {}", portName, bridgeName, ovsdbAddress);
        OvsdbNode ovsdbNode = new OvsdbNode(ovsdbAddress, OVSPORT);

        // Get all the bridges
        getAllBridges();
        if (!isBridgeCreated(bridgeName)) {
            log.warn("A bridge with this name does not exists, aborting.");
            throw new BridgeNotFoundException();
        }

        try {
            Device device = deviceService.getDevice(ovsdbNode.ovsdbId());

            if (device == null) {
                log.warn("Ovsdb device not found, aborting.");
                throw new OvsdbDeviceException("Ovsdb device not found");
            }
            if (device.is(BridgeConfig.class)) {
                log.info("Start Add Port Process");
                // add port to bridge through ovsdb
                BridgeConfig bridgeConfig = device.as(BridgeConfig.class);
                bridgeConfig.addPort(BridgeName.bridgeName(bridgeName), portName);
                log.info("Correctly added port {} to bridge {} at {}", portName, bridgeName, ovsdbAddress);
            } else {
                log.warn("The bridging behaviour is not supported in device {}", device.id());
                throw new OvsdbDeviceException(
                        "The bridging behaviour is not supported in device " + device.id()
                );
            }
        } catch (ItemNotFoundException e) {
            log.warn("Failed to delete bridge on {}", ovsdbNode.ovsdbIp());
            throw new OvsdbDeviceException("Error with ovsdb device: item not found");
        }
    }

    @Override
    public void removePort(IpAddress ovsdbAddress, String bridgeName, String portName)
            throws OvsdbDeviceException, BridgeNotFoundException {

        log.warn("Deleting port {} to bridge {} at {}", portName, bridgeName, ovsdbAddress);
        OvsdbNode ovsdbNode = new OvsdbNode(ovsdbAddress, OVSPORT);

        // Get all the bridges
        getAllBridges();
        if (!isBridgeCreated(bridgeName)) {
            log.warn("A bridge with this name does not exists, aborting.");
            throw new BridgeNotFoundException();
        }

        try {
            Device device = deviceService.getDevice(ovsdbNode.ovsdbId());
            if (device == null) {
                log.warn("Ovsdb device not found, aborting.");
                throw new OvsdbDeviceException("Ovsdb device not found");
            }
            if (device.is(BridgeConfig.class)) {

                // delete port from bridge through ovsdb
                BridgeConfig bridgeConfig = device.as(BridgeConfig.class);
                bridgeConfig.deletePort(BridgeName.bridgeName(bridgeName), portName);

                log.info("Correctly deleted port {} from bridge {} at {}", portName, bridgeName, ovsdbAddress);

            } else {
                log.warn("The bridging behaviour is not supported in device {}", device.id());
                throw new OvsdbDeviceException(
                        "The bridging behaviour is not supported in device " + device.id()
                );
            }
        } catch (ItemNotFoundException e) {
            log.warn("Failed to delete bridge on {}", ovsdbNode.ovsdbIp());
            throw new OvsdbDeviceException("Error with ovsdb device: item not found");
        }
    }

    @Override
    public void createPatchPeerPort(IpAddress ovsdbAddress, String bridgeName,
                                    String portName, String patchPeer)
            throws OvsdbDeviceException, BridgeNotFoundException {

        log.info("Setting port {} as peer of port {}", portName, patchPeer);

        OvsdbNode ovsdbNode = new OvsdbNode(ovsdbAddress, OVSPORT);

        // Get all the bridges
        getAllBridges();
        if (!isBridgeCreated(bridgeName)) {
            log.warn("A bridge with this name does not exists, aborting.");
            throw new BridgeNotFoundException();
        }

        Device device = deviceService.getDevice(ovsdbNode.ovsdbId());
        log.info("OvsdbNode.ovsdbId = " + ovsdbNode.ovsdbId());
        if (device == null) {
            log.warn("Ovsdb device not found, aborting.");
            throw new OvsdbDeviceException("Ovsdb device not found");
        }

        if (device.is(InterfaceConfig.class)) {
            InterfaceConfig interfaceConfig = device.as(InterfaceConfig.class);

            // prepare patch
            PatchDescription.Builder builder = DefaultPatchDescription.builder();
            PatchDescription patchDescription = builder
                    .deviceId(bridgeName)
                    .ifaceName(portName)
                    .peer(patchPeer)
                    .build();
            // add patch to port through ovsdb
            interfaceConfig.addPatchMode(portName, patchDescription);
            log.info("Correctly created port {} on device {} as peer of port {}", portName, bridgeName, patchPeer);
        } else {
            log.warn("The interface behaviour is not supported in device {}", device.id());
            throw new OvsdbDeviceException(
                    "The interface behaviour is not supported in device " + device.id()
            );
        }
    }

    @Override
    public void createVxlanTunnel(IpAddress ovsdbAddress, String bridgeName, String portName,
                                  IpAddress remoteIp, String key)
            throws OvsdbDeviceException, BridgeNotFoundException {

        log.info("Setting up tunnel VXLAN to {} with key {}",
                remoteIp, key);
        OvsdbNode ovsdbNode = new OvsdbNode(ovsdbAddress, OVSPORT);

        // Get all the bridges
        getAllBridges();
        if (!isBridgeCreated(bridgeName)) {
            log.warn("A bridge with this name does not exists, aborting.");
            throw new BridgeNotFoundException();
        }

        try {
            Device device = deviceService.getDevice(ovsdbNode.ovsdbId());
            log.debug("OvsdbNode.ovsdbId = " + ovsdbNode.ovsdbId());
            if (device == null) {
                log.warn("Ovsdb device not found, aborting.");
                throw new OvsdbDeviceException("Ovsdb device not found");
            }

            if (device.is(InterfaceConfig.class)) {
                InterfaceConfig interfaceConfig = device.as(InterfaceConfig.class);

                // prepare tunnel
                TunnelDescription tunnelDescription = DefaultTunnelDescription.builder()
                        .deviceId(bridgeName)
                        .ifaceName(portName)
                        .type(TunnelDescription.Type.VXLAN)
                        .remote(TunnelEndPoints.ipTunnelEndpoint(remoteIp))
                        .key(new TunnelKey<>(key))
                        .build();
                // create tunnel to port through ovsdb
                interfaceConfig.addTunnelMode(portName, tunnelDescription);
                log.info("Correctly added tunnel VXLAN to {} with key {}", remoteIp, key);
            } else {
                log.warn("The interface behaviour is not supported in device {}", device.id());
                throw new OvsdbDeviceException(
                        "The interface behaviour is not supported in device " + device.id()
                );
            }
        } catch (ItemNotFoundException e) {
            log.warn("Failed to delete bridge on {}", ovsdbNode.ovsdbIp());
            throw new OvsdbDeviceException("Error with ovsdb device: item not found");
        }
    }

    @Override
    public void deleteGreTunnel(IpAddress ovsdbAddress, String bridgeName, String portName)
            throws OvsdbDeviceException {

    }

    @Override
    public void createPathIntent(String srcId, String dstId, String portSrc, String portDst,
                                 PathIntent.ProtectionType setType)
            throws Exception {
        log.info("Start the createPathIntent function");
        IntentService intentService = AbstractShellCommand.get(IntentService.class);
        log.info("Start TrafficSelector");
        // Set Variables to make the builder
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_ARP)
                .build();

        DeviceId srcDid = deviceId(srcId);
        DeviceId dstDid = deviceId(dstId);
        if (deviceService.getDevice(srcDid) == null || deviceService.getDevice(dstDid) == null) {
            throw  new Exception("The Src or Dst Device don't exists");
        }

        log.info("Set Topology Service");
        TopologyService topologyService = AbstractShellCommand.get(TopologyService.class);
        Topology topology = topologyService.currentTopology();
        Set<? extends Path> paths = topologyService.getPaths(topology, srcDid, dstDid);
        // Path path = paths.stream().findFirst().get();
        if (paths == null) {
            throw  new Exception("The Src and Dst Port don't have any path");
        }
        Path pathUser = null;
        for (Path path : paths) {
            List<Link> links = path.links();
            AtomicBoolean foundPath = new AtomicBoolean(false);
            links.forEach(link -> {
                log.info("This is the source port: {}", link.src().port().toString());
                log.info("This is the destination port: {}", link.dst().port().toString());
                if (portSrc.equals(link.src().port().toString()) &&
                        portDst.equals(link.dst().port().toString())) {
                    log.info("We found the path that the user want");
                    foundPath.set(true);
                }
            });
            log.info("The value of the boolean is {}", foundPath);
            if (foundPath.get()) {
                pathUser = path;
                break;
            }
        }
        if (pathUser == null) {
            throw  new Exception("The Path that the user want doesn't exist");
        }
        log.info("The path was received correctly: {}", pathUser);

        log.info("Start to create the Intent");
        Intent intent = PathIntent.builder()
                .appId(coreService.getAppId("org.onosproject.cli"))
                .selector(selector)
                .priority(400)
                .path(pathUser)
                .setType(setType)
                .build();
        log.info("Send the created Intent to apply the changes");
        // Send the created intent
        intentService.submit(intent);
    }

    /**
     * Use the deviceService to get all the devices which are Bridges and save this information
     * in the bridgeIds Map.
     */
    private void getAllBridges() {
        // Clean the bridges variable to avoid write data more than once
        bridgeIds.clear();
        Iterable<Device> devices = deviceService.getDevices(Device.Type.CONTROLLER);
        devices.forEach(device -> {
            BridgeConfig bridgeConfigDevice = device.as(BridgeConfig.class);
            Collection<BridgeDescription> setBridges = bridgeConfigDevice.getBridges();
            setBridges.forEach(bridge -> {
                if (bridge.deviceId().isPresent()) {
                    bridgeIds.put(bridge.name(), bridge.deviceId().get());
                } else {
                    log.info("There is a problem with the bridges it is not returning ID");
                }
            });
        });
    }

    /**
     * Checks if the bridge exists and is available.
     *
     * @return true if the bridge is available, false otherwise
     */
    private boolean isBridgeCreated(String bridgeName) {
        DeviceId deviceId = bridgeIds.get(bridgeName);
        return (deviceId != null
                && deviceService.getDevice(deviceId) != null
                && deviceService.isAvailable(deviceId));
    }


    /**
     * Gets an available datapath id for the new bridge.
     *
     * @param datapathId the integer used to generate ids
     * @return the datapath id
     */
    private DeviceId getNextUniqueDatapathId(AtomicLong datapathId) {
        DeviceId dpid;
        do {
            String stringId = String.format("of:%16X", datapathId.getAndIncrement()).replace(' ', '0');
            log.info("This is a possible id: {}", stringId);
            dpid = DeviceId.deviceId(stringId);

        } while (deviceService.getDevice(dpid) != null);
        return dpid;
    }

}
