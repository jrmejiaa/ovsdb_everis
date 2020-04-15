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
import org.onlab.packet.IpAddress;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Sample web resource.
 */
@Path("test/")
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
     * @param ovsdbIp OVSDB IP Address.
     * @param bridgeName Bridge Name.
     * @return Return 200 OK if OVSDB IP exitis.
     */
    @POST
    @Path("{ovsdb-ip}/bridge/{bridge-name}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBridge(@PathParam("ovsdb-ip") String ovsdbIp,
                              @PathParam("bridge-name") String bridgeName) {
        try {
            IpAddress ovsdbAddress = IpAddress.valueOf(ovsdbIp);
            log.info("It is inside of the POST REST API");
            ObjectNode node = mapper().createObjectNode().put("OVSDB IP", ovsdbAddress.toString());
            return ok(node).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.CONFLICT).entity("A bridge with this name already exists").build();
        }
    }

}
