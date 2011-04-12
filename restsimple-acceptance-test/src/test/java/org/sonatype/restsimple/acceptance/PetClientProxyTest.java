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
package org.sonatype.restsimple.acceptance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.client.WebException;
import org.sonatype.restsimple.client.WebProxy;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.FileAssert.fail;

public class PetClientProxyTest {
    protected static final Logger logger = LoggerFactory.getLogger(PetClientProxyTest.class);

    protected final static MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

    protected String targetUrl;

    protected String acceptHeader;

    protected WebDriver webDriver;

    protected ServiceDefinition serviceDefinition;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        acceptHeader = PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON;

        Action action = new PetstoreAction();
        serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition
                .withHandler(new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new GetServiceHandler("getPetString", action).consumeWith(JSON, Pet.class).producing(new MediaType("text", "plain")))
                .withHandler(new DeleteServiceHandler("deletePet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("addPet", action).consumeWith(JSON, Pet.class).producing(JSON));

        webDriver = WebDriver.getDriver().serviceDefinition(serviceDefinition);
        targetUrl = webDriver.getUri();
        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        webDriver.shutdown();
    }

    @Test(timeOut = 20000)
    public void testBasicPostGenerate() throws Throwable {
        logger.info("running test: testBasicPostGenerate");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);
    }

    @Test(timeOut = 20000)
    public void testBasicGetGenerate() throws Throwable {
        logger.info("running test: testBasicGetGenerate");
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
        logger.info("running test: testDelete");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.delete("myPet");
        assertNotNull(pet);

        try {
            client.getString("myPet");
            fail("No exception");
        } catch (WebException ex) {
            assertEquals(ex.getClass(), WebException.class);
        }
    }

    public static interface ProxyClient {

        @Get
        @Path("getPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet get(@PathParam("myPet") String path);
        
        @Get
        @Path("getPetString")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes("text/plain") 
        public String getString(@PathParam("myPet") String path);

        @Post
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(@PathParam("myPet") String myPet, String body);

        @Delete
        @Path("deletePet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet delete(@PathParam("myPet") String path);

    }


}
