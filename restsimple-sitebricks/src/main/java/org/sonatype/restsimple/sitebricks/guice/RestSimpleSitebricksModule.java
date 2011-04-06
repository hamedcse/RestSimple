/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.restsimple.sitebricks.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.sitebricks.impl.SitebricksServiceDefinitionGenerator;
import org.sonatype.restsimple.sitebricks.impl.SitebricksServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

/**
 * A Sitebricks module that install the appropriate object needed to generate Sitebricks Resource.
 */
public class RestSimpleSitebricksModule extends AbstractModule {

    private final Binder binder;
    private final ServiceHandlerMapper mapper;
    private final Class<? extends NegotiationTokenGenerator> tokenGenerator;
    private final Class<? extends ServiceDefinitionProvider> provider;

    public RestSimpleSitebricksModule(Binder binder,
                                      ServiceHandlerMapper mapper,
                                      Class<? extends NegotiationTokenGenerator> tokenGenerator,
                                      Class<? extends ServiceDefinitionProvider> provider) {
        this.binder = binder;
        this.mapper = mapper;
        this.tokenGenerator = tokenGenerator;
        this.provider = provider;
    }

    public RestSimpleSitebricksModule(Binder binder, Class<? extends NegotiationTokenGenerator> tokenGenerator) {
        this(binder, new ServiceHandlerMapper(), tokenGenerator, SitebricksServiceDefinitionProvider.class);
    }

    public RestSimpleSitebricksModule(Binder binder, ServiceHandlerMapper mapper) {
        this(binder, mapper, RFC2295NegotiationTokenGenerator.class, SitebricksServiceDefinitionProvider.class);
    }

    public RestSimpleSitebricksModule(Binder binder) {
        this(binder, new ServiceHandlerMapper(), RFC2295NegotiationTokenGenerator.class, SitebricksServiceDefinitionProvider.class);
    }

    @Override
    protected void configure() {
        bind(ServiceHandlerMapper.class).toInstance(mapper);
        bind(NegotiationTokenGenerator.class).to(tokenGenerator);
        binder.bind(ServiceHandlerMapper.class).toInstance(mapper);

        bind(ResourceModuleConfig.class).toInstance(new ResourceModuleConfig<Module>(){

            @Override
            public <A> void bindToInstance(Class<A> clazz, A instance) {
                binder.bind(clazz).toInstance(instance);
            }

            @Override
            public <A> void bindTo(Class<A> clazz, Class<? extends A> clazz2) {
                binder.bind(clazz).to(clazz2);
            }

            @Override
            public void bind(Class<?> clazz) {
                binder.bind(clazz);
            }

            @Override
            public void install(Module module) {
                binder.install(module);
            }
        });
        bind(ServiceDefinitionGenerator.class).to(SitebricksServiceDefinitionGenerator.class);
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(provider);
        
    }
}
