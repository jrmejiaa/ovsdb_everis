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
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.onlab.util.Tools.readTreeFromStream;

/**
 * Possible REST APIs to make changes in the switches of the ONOS Cluster.
 */
@Path("config/")
public class AppWebResource extends AbstractWebResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Set<String> CREATE_BRIDGE = new HashSet<>(Arrays.asList("a", "b"));

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    @Path("sample/")
    public Response getGreeting() {
        ObjectNode node = mapper().createObjectNode().put("hello", "world");
        return ok(node).build();
    }

    /**
     * Create a Bridge using JSON.
     * @param stream JSON Parameter
     * @return OK 200
     * @onos.rsModel createBridge
     */
    @POST
    @Path("creatingBridge/")
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
                return Response.status(Response.Status.CONFLICT).entity(node).build();
            }
            // Changing the values to be able to use the create Bridge app
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIP);
            // Go to the createBridge function to create the bridge according to the given information
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createBridge(ovsdbAddress, bridgeName);

            node.put("bridge-created:", "true");
            return ok(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        } catch (OvsdbRestException.BridgeAlreadyExistsException ex) {
            node.put("bridge-created:", "false");
            node.put("error:",
                    "The Bridge Already Exists, please use another name");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(node).build();
        } catch (IOException e) {
            node.put("bridge-created:", "false");
            node.put("error:",
                    "The JSON has build problems, please make sure that you have all the required elements");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Delete the bridge with the given information.
     * @param ovsdbIp OVSDB IP Address
     * @param bridgeName Bridge Name to delete
     * @return 200 OK if the bridge was deleted
     */
    @DELETE
    @Path("/{ovsdb-ip}/bridge/{bridge-name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBridge(@PathParam("ovsdb-ip") String ovsdbIp,
                                 @PathParam("bridge-name") String bridgeName) {

        ObjectNode node = mapper().createObjectNode();

        try {

            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.deleteBridge(ovsdbAddress, bridgeName);

            node.put("bridge-deleted:", "true");
            // Return 200 OK
            return ok(node).build();
        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.NOT_FOUND).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Add a port in the bridge with the given information.
     * @param ovsdbIp OVSDB IP Address
     * @param bridgeName Bridge Name
     * @param portName Port name to add
     * @return 200 OK
     */
    @POST
    @Path("/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPort(@PathParam("ovsdb-ip") String ovsdbIp,
                            @PathParam("bridge-name") String bridgeName,
                            @PathParam("port-name") String portName) {
        ObjectNode node = mapper().createObjectNode();
        try {
            log.info("See if the IP Address is valid");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            log.info("Start the addPort function");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.addPort(ovsdbAddress, bridgeName, portName);

            node.put("port-added:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.NOT_FOUND).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Delete the port of a bridge.
     * @param ovsdbIp OVSDB IP Address
     * @param bridgeName Bridge Name
     * @param portName Port Name to delete
     * @return 200 OK
     */
    @DELETE
    @Path("/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePort(@PathParam("ovsdb-ip") String ovsdbIp,
                               @PathParam("bridge-name") String bridgeName,
                               @PathParam("port-name") String portName) {
        ObjectNode node = mapper().createObjectNode();
        try {
            log.info("See if the IP Address is valid");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);

            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.removePort(ovsdbAddress, bridgeName, portName);

            node.put("port-deleted:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.NOT_FOUND).entity(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }

    /**
     * Creates Patch Peer Port to connect with another port.
     * @param ovsdbIp OVSDB IP Address
     * @param bridgeName Name of the Bridge
     * @param portName Port Name
     * @param patchPeer PatchPeer
     * @return 200 OK If everything goes fine
     */
    @POST
    @Path("/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}/patch_peer/{patch-peer}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPatchPeerPort(@PathParam("ovsdb-ip") String ovsdbIp,
                                        @PathParam("bridge-name") String bridgeName,
                                        @PathParam("port-name") String portName,
                                        @PathParam("patch-peer") String patchPeer) {
        ObjectNode node = mapper().createObjectNode();
        try {
            log.info("Checking the address...");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            log.info("Start the process in the createPatchPeerPort function...");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createPatchPeerPort(ovsdbAddress, bridgeName, portName, patchPeer);

            node.put("patch-peer-created:", "true");
            // Return 200 OK
            return ok(node).build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.NOT_FOUND).entity(node).build();
        }
    }

    /**
     * Create a VXLAN Tunnel Port.
     * @param ovsdbIp OVSDB IP Address
     * @param bridgeName Bridge Name
     * @param portName Port Name
     * @param localIp Local IP
     * @param remoteIp Remote IP
     * @param key it has to be flow
     * @return OK 200
     */
    @POST
    @Path("/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}/gre/{local-ip}/{remote-ip}/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addVxlanTunnel(@PathParam("ovsdb-ip") String ovsdbIp,
                                 @PathParam("bridge-name") String bridgeName,
                                 @PathParam("port-name") String portName,
                                 @PathParam("local-ip") String localIp,
                                 @PathParam("remote-ip") String remoteIp,
                                 @PathParam("key") String key) {
        ObjectNode node = mapper().createObjectNode();
        try {
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            IpAddress tunnelLocalIp = IpAddress.valueOf(localIp);
            IpAddress tunnelRemoteIp = IpAddress.valueOf(remoteIp);
            log.info("Start the createVXLAN function...");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createVxlanTunnel(ovsdbAddress, bridgeName,
                    portName, tunnelLocalIp, tunnelRemoteIp, key);

            node.put("vxlan-created:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("No bridge found with the specified name").build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            node.put("bridge-created:", "false");
            node.put("error:", "The bridge was not found");
            return Response.status(Response.Status.CONFLICT).entity(node).build();
        }
    }
}
