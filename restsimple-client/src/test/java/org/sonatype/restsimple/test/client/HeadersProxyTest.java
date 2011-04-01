/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.restsimple.test.client;

import org.sonatype.restsimple.client.WebProxy;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.testng.annotations.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class HeadersProxyTest extends BaseTest {

    @Test(timeOut = 20000)
    public void testBasicPostGenerate() throws Throwable {
    logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "chatchien", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);
        assertEquals(pet.getName(), "pouetpouet--chatchien");        
    }

    @Test(timeOut = 20000)
    public void testBasicGetGenerate() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet",  "chatchien", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.get("myPet");
        assertNotNull(pet);

        String petString = client.getString("myPet");
        assertEquals(petString, "{\"name\":\"pouetpouet--chatchien\"}");
    }

    public static interface ProxyClient {

        @GET
        @Path("getPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet get(@PathParam("myPet") String path);

        @GET
        @Path("getPetString")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public String getString(@PathParam("myPet") String path);

        @POST
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(@PathParam("myPet") String myPet, @HeaderParam(PetstoreAction.PET_EXTRA_NAME) String petType, String body);


    }


}