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

import org.onlab.packet.IpAddress;
import org.onosproject.cfg.ComponentConfigService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Properties;

import org.everis.app.OvsdbRestException.BridgeNotFoundException;
import org.everis.app.OvsdbRestException.BridgeAlreadyExistsException;
import org.everis.app.OvsdbRestException.OvsdbDeviceException;

//import static org.onlab.util.Tools.get;

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
    protected ComponentConfigService cfgService;

    @Activate
    protected void activate() {
        cfgService.registerProperties(getClass());
        log.info("Hello World the App is working...");
    }

    @Deactivate
    protected void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
        log.info("Reconfigured");
    }


    @Override
    public void createBridge(IpAddress ovsdbAddress, String bridgeName)
            throws OvsdbDeviceException, BridgeAlreadyExistsException {
        log.info("This is your IP {} and the new of the bridge {}", ovsdbAddress, bridgeName);
    }

    @Override
    public void deleteBridge(IpAddress ovsdbAddress, String bridgeName)
            throws OvsdbDeviceException, BridgeNotFoundException {

    }

    @Override
    public void addPort(IpAddress ovsdbAddress, String bridgeName, String portName)
            throws OvsdbDeviceException, BridgeNotFoundException {

    }

    @Override
    public void removePort(IpAddress ovsdbAddress, String bridgeName, String portName)
            throws OvsdbDeviceException, BridgeNotFoundException {

    }

    @Override
    public void createPatchPeerPort(IpAddress ovsdbAddress, String bridgeName,
                                    String portName, String patchPeer)
            throws OvsdbDeviceException {

    }

    @Override
    public void createGreTunnel(IpAddress ovsdbAddress, String bridgeName, String portName,
                                IpAddress localIp, IpAddress remoteIp, String key)
            throws OvsdbDeviceException, BridgeNotFoundException {

    }

    @Override
    public void deleteGreTunnel(IpAddress ovsdbAddress, String bridgeName, String portName)
            throws OvsdbDeviceException {

    }
}
