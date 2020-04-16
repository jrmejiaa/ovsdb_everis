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
        ObjectNode node = mapper().createObjectNode().put("hello", "world");
        return ok(node).build();
    }

    /**
     * Create a bridge using the IP of a known Device Manager.
     *
     * @param ovsdbIp OVSDB IP Address
     * @param bridgeName Bridge Name
     * @return Return 200 OK if OVSDB IP exitis
     */
    @POST
    @Path("{ovsdb-ip}/bridge/{bridge-name}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBridge(@PathParam("ovsdb-ip") String ovsdbIp,
                              @PathParam("bridge-name") String bridgeName) {
        try {
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            // Go to the createBridge function to create the bridge according to the given information
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.createBridge(ovsdbAddress, bridgeName);
            ObjectNode node = mapper().createObjectNode().put("bridge-created:", "true");
            // Return 200 OK with a JSON of successful connection
            return ok(node).build();

        } catch (OvsdbRestException.BridgeAlreadyExistsException ex) {
            return Response.status(Response.Status.CONFLICT).entity("A bridge with this name already exists").build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
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
        try {

            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.deleteBridge(ovsdbAddress, bridgeName);
            ObjectNode node = mapper().createObjectNode().put("bridge-deleted:", "true");
            // Return 200 OK
            return ok(node).build();
        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("No bridge found with the specified name").build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
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
        try {
            log.info("See if the IP Address is valid");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            log.info("Start the addPort function");
            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.addPort(ovsdbAddress, bridgeName, portName);

            ObjectNode node = mapper().createObjectNode().put("port-added:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    "No bridge found with the specified name").build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
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
        try {
            log.info("See if the IP Address is valid");
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);

            OvsdbBridgeService ovsdbBridgeService = get(OvsdbBridgeService.class);
            ovsdbBridgeService.removePort(ovsdbAddress, bridgeName, portName);

            ObjectNode node = mapper().createObjectNode().put("port-deleted:", "true");
            // Return 200 OK
            return ok(node).build();

        } catch (OvsdbRestException.BridgeNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    "No bridge found with the specified name").build();
        } catch (OvsdbRestException.OvsdbDeviceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

}
