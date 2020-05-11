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
package org.everis.app.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.everis.app.OvsdbBridgeService;
import org.everis.app.OvsdbRestException;
import org.onlab.packet.IpAddress;
import org.onosproject.net.intent.PathIntent;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.onlab.util.Tools.readTreeFromStream;

/**
 * Possible REST APIs to make changes in the switches of the ONOS Cluster.
 */
@Path("config/")
public class AppWebResource extends AbstractWebResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    @Path("sample/")
    public Response getGreeting() {
        ObjectNode node = mapper().createObjectNode().put("Hey!", "I am working");
        return ok(node).build();
    }

    /**
     * Create a Bridge using JSON.
     * @param stream JSON Parameter
     * @return OK 200
     * @onos.rsModel create_delete_Bridge
     */
    @POST
    @Path("createBridge/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBridge(InputStream stream) {
        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String ovsdbIP = jsonTree.get("ovsdb-ip").asText();
            String bridgeName = jsonTree.get("bridge-name").asText();

            if (ovsdbIP == null || bridgeName == null) {
                node.put("bridge-created:", "false");
                node.put("error:", "The JSON was not complete to make the operation");
                return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
            }
            // Changing the values to be able to use the create Bridge app
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIP);
            // Go to the createBridge function to create the bridge according to the given information
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createBridge(ovsdbAddress, bridgeName);

            node.put("bridge-created:", "true");
            return ok(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException | IOException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (OvsdbRestException.BridgeAlreadyExistsException ex) {
            node.put("bridge-created:", "false");
            node.put("error:",
                    "The Bridge Already Exists, please use another name");
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (Exception ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "There was an error with the structure of the JSON");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Delete a Bridge using JSON.
     * @param stream JSON Parameter
     * @return OK 200
     * @onos.rsModel create_delete_Bridge
     */
    @POST
    @Path("deleteBridge/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBridge(InputStream stream) {

        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String ovsdbIP = jsonTree.get("ovsdb-ip").asText();
            String bridgeName = jsonTree.get("bridge-name").asText();

            if (ovsdbIP == null || bridgeName == null) {
                node.put("bridge-created:", "false");
                node.put("error:", "The JSON was not complete to make the operation");
                return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
            }
            // Changing the values to be able to use the create Bridge app
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIP);

            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.deleteBridge(ovsdbAddress, bridgeName);

            node.put("bridge-deleted:", "true");
            // Return 200 OK
            return ok(node).build();
        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException | IOException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (Exception ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "There was an error with the structure of the JSON");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Add a port in the bridge with the given information.
     * @param stream JSON Parameter
     * @return 200 OK
     * @onos.rsModel add_delete_Port
     */
    @POST
    @Path("addPort/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPort(InputStream stream) {
        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String ovsdbIP = jsonTree.get("ovsdb-ip").asText();
            String bridgeName = jsonTree.get("bridge-name").asText();
            String portName = jsonTree.get("port-name").asText();

            if (ovsdbIP == null || bridgeName == null || portName == null) {
                node.put("bridge-created:", "false");
                node.put("error:", "The JSON was not complete to make the operation");
                return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
            }
            log.info("See if the IP Address is valid");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIP);
            log.info("Start the addPort function");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.addPort(ovsdbAddress, bridgeName, portName);

            node.put("port-added:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("port-added:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException | IOException ex) {
            node.put("port-added:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (Exception ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "There was an error with the structure of the JSON");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Delete the port of a bridge.
     * @param stream JSON Parameter
     * @return 200 OK
     * @onos.rsModel add_delete_Port
     */
    @POST
    @Path("deletePort/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePort(InputStream stream) {
        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String ovsdbIP = jsonTree.get("ovsdb-ip").asText();
            String bridgeName = jsonTree.get("bridge-name").asText();
            String portName = jsonTree.get("port-name").asText();

            if (ovsdbIP == null || bridgeName == null || portName == null) {
                node.put("bridge-created:", "false");
                node.put("error:", "The JSON was not complete to make the operation");
                return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
            }
            log.info("See if the IP Address is valid");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIP);

            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.removePort(ovsdbAddress, bridgeName, portName);

            node.put("port-deleted:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException | IOException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (Exception ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "There was an error with the structure of the JSON");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Creates Patch Peer Port to connect with another port.
     * @param stream JSON Configuration
     * @return 200 OK If everything goes fine
     * @onos.rsModel createPatchPeerPort
     */
    @POST
    @Path("createPatchPeerPort/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPatchPeerPort(InputStream stream) {
        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String ovsdbIp = jsonTree.get("ovsdb-ip").asText();
            String bridgeName = jsonTree.get("bridge-name").asText();
            String portName = jsonTree.get("port-name").asText();
            String patchPeer = jsonTree.get("patch-peer").asText();

            if (ovsdbIp == null || bridgeName == null || portName == null || patchPeer == null) {
                node.put("bridge-created:", "false");
                node.put("error:", "The JSON was not complete to make the operation");
                return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
            }
            log.info("Checking the address...");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            log.info("Start the process in the createPatchPeerPort function...");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createPatchPeerPort(ovsdbAddress, bridgeName, portName, patchPeer);

            node.put("patch-peer-created:", "true");
            // Return 200 OK
            return ok(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException | IOException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (Exception ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "There was an error with the structure of the JSON");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Create a VXLAN Tunnel Port.
     * @param stream JSON Configuration
     * @onos.rsModel addVxlanTunnel
     * @return OK 200
     */
    @POST
    @Path("createVxlanTunnel/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addVxlanTunnel(InputStream stream) {
        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String ovsdbIp = jsonTree.get("ovsdb-ip").asText();
            String bridgeName = jsonTree.get("bridge-name").asText();
            String portName = jsonTree.get("port-name").asText();
            String remoteIp = jsonTree.get("remote-ip").asText();
            String key = jsonTree.get("key").asText();


            if (ovsdbIp == null || bridgeName == null || portName == null ||
                    remoteIp == null || key == null) {
                node.put("bridge-created:", "false");
                node.put("error:", "The JSON was not complete to make the operation");
                return Response.status(Response.Status.CONFLICT).entity(node).build();
            }
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            IpAddress tunnelRemoteIp = IpAddress.valueOf(remoteIp);
            log.info("Start the createVXLAN function...");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createVxlanTunnel(ovsdbAddress, bridgeName,
                    portName, tunnelRemoteIp, key);

            node.put("vxlan-created:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException | IOException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        } catch (Exception ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "There was an error with the structure of the JSON");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Create a Path Intent for a known Path Port.
     * @param stream JSON Configuration to make the PathIntent
     * @onos.rsModel PathIntent
     * @return OK 200
     */
    @POST
    @Path("createPathIntent/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPathIntent(InputStream stream) {
        ObjectNode node = mapper().createObjectNode();
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            String srcId = jsonTree.get("src-id").asText();
            String dstId = jsonTree.get("dst-id").asText();
            String portSrc = jsonTree.get("port-src").asText();
            String portDst = jsonTree.get("port-dst").asText();
            String setType = jsonTree.get("setType").asText();

            log.info("Start the createPathIntent...");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            if (setType.equals("PRIMARY")) {
                log.info("The path was set as PRIMARY");
                ovsdbBridgeService.createPathIntent(srcId, dstId, portSrc, portDst,
                        PathIntent.ProtectionType.PRIMARY);
            } else if (setType.equals("BACKUP")) {
                log.info("The path was set as BACKUP");
                ovsdbBridgeService.createPathIntent(srcId, dstId, portSrc, portDst,
                        PathIntent.ProtectionType.BACKUP);
            } else if (setType.equals("FAILOVER")) {
                log.info("The path was set as FAILOVER");
                ovsdbBridgeService.createPathIntent(srcId, dstId, portSrc, portDst,
                        PathIntent.ProtectionType.FAILOVER);
            }

            node.put("createPathIntent-created:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (Exception ex) {
            node.put("createPathIntent-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(node).build();
        }
    }
}
