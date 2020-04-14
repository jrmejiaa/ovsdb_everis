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
 *
 * Auto-generated by OnosSwaggerMojo.
 */
package org.everis.app;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onosproject.rest.ApiDocProvider;
import org.onosproject.rest.ApiDocService;

@Component(immediate = true)
public class ApiDocRegistrator {

    protected final ApiDocProvider provider;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ApiDocService service;

    public ApiDocRegistrator() {
        provider = new ApiDocProvider("/onos/org.everis", "app REST API",
                                      getClass().getClassLoader());
    }

    @Activate
    protected void activate() {
        service.register(provider);
    }

    @Deactivate
    protected void deactivate() {
        service.unregister(provider);
    }
}
