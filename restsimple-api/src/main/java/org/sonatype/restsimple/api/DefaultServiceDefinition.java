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
package org.sonatype.restsimple.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A default {@link ServiceDefinition} which can be injected or created directly.
 */
public class DefaultServiceDefinition implements ServiceDefinition {
    private String path = "";
    private final List<MediaType> mediaTypeToProduce = new ArrayList<MediaType>();
    private final List<MediaType> mediaTypeToConsume = new ArrayList<MediaType>();
    private final List<ServiceHandler> serviceHandlers = new ArrayList<ServiceHandler>();
    private final AtomicBoolean configured = new AtomicBoolean(false);
    private final List<Class<?>> extensions = new ArrayList<Class<?>>();

    public DefaultServiceDefinition() {
    }

    @Override
    public String toString() {
        return "DefaultServiceDefinition{" +
                "path='" + path + '\'' +
                ", mediaTypeToProduce=" + mediaTypeToProduce +
                ", mediaTypeToConsume=" + mediaTypeToConsume +
                ", serviceHandlers=" + serviceHandlers +
                '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override    
    public ServiceDefinition withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition withHandler(ServiceHandler serviceHandler) {
        serviceHandlers.add(serviceHandler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition producing(MediaType mediaType) {
        mediaTypeToProduce.add(mediaType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition consuming(MediaType mediaType) {
        mediaTypeToConsume.add(mediaType);
        return this;
    }
     /**
     * {@inheritDoc}
     */
    @Override
    public String path() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceHandler> serviceHandlers() {

        if (!configured.getAndSet(true)) {
            for (ServiceHandler serviceHandler: serviceHandlers) {

                if (serviceHandler.mediaToProduce().size() == 0) {
                    for(MediaType p: mediaTypeToProduce) {
                        serviceHandler.producing(p);
                    }
                }

                //TODO: Could have several
                if (serviceHandler.consumeMediaType() == null && mediaTypeToConsume.size() > 0) {
                    serviceHandler.consumeWith(mediaTypeToConsume.get(0), null);
                }
            }
        }
        return Collections.unmodifiableList(serviceHandlers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MediaType> mediaToConsume() {
        return Collections.unmodifiableList(mediaTypeToConsume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MediaType> mediaToProduce() {
        if (mediaTypeToProduce.isEmpty()) {
            mediaTypeToProduce.add(new MediaType( "text", "json"));
        }
        return Collections.unmodifiableList(mediaTypeToProduce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition extendWith(Class<?> clazz) {
        extensions.add(clazz);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<?>> extensions(){
        return Collections.unmodifiableList(extensions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition get(String path, Action action) {
        withHandler(new GetServiceHandler(path,action));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition post(String path, Action action) {
        withHandler(new PostServiceHandler(path,action));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition put(String path, Action action) {
        withHandler(new PutServiceHandler(path,action));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition delete(String path, Action action) {
        withHandler(new DeleteServiceHandler(path,action));
        return this;
    }
}
