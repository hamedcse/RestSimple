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

import org.sonatype.restsimple.WebDriver;
import org.sonatype.restsimple.annotation.Consumes;
import org.sonatype.restsimple.annotation.Delete;
import org.sonatype.restsimple.annotation.Get;
import org.sonatype.restsimple.annotation.Path;
import org.sonatype.restsimple.annotation.PathParam;
import org.sonatype.restsimple.annotation.Post;
import org.sonatype.restsimple.annotation.Produces;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.client.WebException;
import org.sonatype.restsimple.client.WebProxy;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.FileAssert.fail;

public abstract class RootResourceProxyTest extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        acceptHeader = PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON;

        Action action = new PetstoreAction();
        serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition
                .withPath("/foo")
                .withHandler(new GetServiceHandler("/getPet/:pet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new GetServiceHandler("/getPetString/:pet", action).consumeWith(JSON, Pet.class).producing(new MediaType("text", "plain")))
                .withHandler(new DeleteServiceHandler("/deletePet/:pet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("/addPet/:pet", action).consumeWith(JSON, Pet.class).producing(JSON));

        webDriver = WebDriver.getDriver().serviceDefinition(serviceDefinition);
        targetUrl = webDriver.getUri();
        logger.info("Local HTTP server started successfully");
    }

    @Test(timeOut = 20000)
    public void testBasicPostGenerate() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);
    }

    @Test(timeOut = 20000)
    public void testBasicGetGenerate() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.get("myPet");
        assertNotNull(pet);

        String petString = client.getString("myPet");
        assertEquals(petString, "Pet{name='pouetpouet'}");
    }

    @Test(timeOut = 20000)
    public void testDelete() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.delete("myPet");
        assertNotNull(pet);

        try {
            client.getString("myPet");
            fail("No exception");
        } catch(WebException ex) {
            assertEquals(ex.getClass(), WebException.class);
        }
    }

    @Path("/foo")
    public static interface ProxyClient {

        @Get
        @Path("/getPet/{id}")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet get(@PathParam("getPet") String path);
        
        @Get
        @Path("/getPetString/{id}")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes("text/plain")
        public String getString(@PathParam("getPetString") String path);

        @Post
        @Path("/addPet/:id")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(@PathParam("addPet") String myPet, String body);

        @Delete
        @Path("/deletePet/:id")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)                
        public Pet delete(@PathParam("deletePet") String path);

    }


}
