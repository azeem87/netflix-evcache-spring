/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.aafwu00.evcache.client.spring.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.archaius.ArchaiusAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.evcache.connection.IConnectionFactoryProvider;
import com.netflix.evcache.pool.EVCacheClientPoolManager;
import com.netflix.evcache.util.EVCacheConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Taeho Kim
 */
class EVCacheCloudAutoConfigurationTest {
    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EVCacheCloudAutoConfiguration.class,
                                                     CacheAutoConfiguration.class,
                                                     ArchaiusAutoConfiguration.class));
    }

    @Test
    void should_be_loaded_EVCacheClientPoolManager() {
        contextRunner.withPropertyValues("evcache.clusters[0].appName=test",
                                         "evcache.clusters[0].cachePrefix=test1",
                                         "spring.application.name=test")
                     .withUserConfiguration(EnableCachingConfiguration.class)
                     .run(context -> assertAll(
                         () -> assertThat(context).hasSingleBean(EVCacheClientPoolManager.class),
                         () -> assertThat(context).hasSingleBean(IConnectionFactoryProvider.class),
                         () -> assertThat(context.getEnvironment().getProperty("evcache.use.simple.node.list.provider")).isEqualTo("false"),
                         () -> assertThat(EVCacheConfig.getInstance()
                                                       .getDynamicBooleanProperty("evcache.use.simple.node.list.provider", true)
                                                       .get()).isFalse()
                     ));
    }

    @Test
    void should_be_loaded_EVCacheClientPoolManager_when_simpleNodeListProvider_is_false() {
        contextRunner.withPropertyValues("evcache.clusters[0].appName=test",
                                         "evcache.clusters[0].cachePrefix=test1",
                                         "spring.application.name=test",
                                         "evcache.use.simple.node.list.provider=false")
                     .withUserConfiguration(EnableCachingConfiguration.class)
                     .run(context -> assertAll(
                         () -> assertThat(context).hasSingleBean(EVCacheClientPoolManager.class),
                         () -> assertThat(context.getBean(IConnectionFactoryProvider.class)).isNotNull(),
                         () -> assertThat(context.getBean(Environment.class)
                                                 .getProperty("evcache.use.simple.node.list.provider", Boolean.class)).isFalse(),
                         () -> assertThat(EVCacheConfig.getInstance()
                                                       .getDynamicBooleanProperty("evcache.use.simple.node.list.provider", true)
                                                       .get()).isFalse()
                     ));
    }

    @Test
    void should_be_loaded_EVCacheClientPoolManager_when_exists_EVCacheClientPoolManager() {
        contextRunner.withPropertyValues("evcache.clusters[0].appName=test",
                                         "evcache.clusters[0].cachePrefix=test1",
                                         "spring.application.name=test")
                     .withUserConfiguration(ExistsEVCacheClientPoolManagerConfiguration.class)
                     .run(context -> assertAll(
                         () -> assertThat(context.getBean(EVCacheClientPoolManager.class)).isNotNull(),
                         () -> assertThat(context.getBean(IConnectionFactoryProvider.class)).isNotNull(),
                         () -> assertThat(context.getBean(Environment.class)
                                                 .getProperty("evcache.use.simple.node.list.provider", Boolean.class)).isNull()
                     ));
    }

    @Test
    void should_not_be_loaded_EVCacheClientPoolManager_when_disabled_evcache_cloud() {
        contextRunner.withPropertyValues("evcache.clusters[0].appName=test",
                                         "evcache.clusters[0].cachePrefix=test1",
                                         "evcache.cloud.enabled=false",
                                         "spring.application.name=test")
                     .withUserConfiguration(EnableCachingConfiguration.class)
                     .run(context -> assertAll(
                         () -> assertThatThrownBy(() -> context.getBean(EVCacheClientPoolManager.class)).isExactlyInstanceOf(
                             NoSuchBeanDefinitionException.class),
                         () -> assertThat(context.getBean(Environment.class)
                                                 .getProperty("evcache.use.simple.node.list.provider", Boolean.class)).isNull()
                     ));
    }

    @Test
    void should_not_be_loaded_EVCacheClientPoolManager_when_simpleNodeListProvider_is_true() {
        contextRunner.withPropertyValues("evcache.clusters[0].appName=test",
                                         "evcache.clusters[0].cachePrefix=test1",
                                         "evcache.cloud.enabled=false",
                                         "evcache.use.simple.node.list.provider=true")
                     .withUserConfiguration(EnableCachingConfiguration.class)
                     .run(context -> assertAll(
                         () -> assertThatThrownBy(() -> context.getBean(EVCacheClientPoolManager.class)).isExactlyInstanceOf(
                             NoSuchBeanDefinitionException.class)
                     ));
    }

    @Test
    void should_not_be_loaded_EVCacheClientPoolManager_when_not_exists_eurekaClient() {
        contextRunner.withPropertyValues("evcache.clusters[0].appName=test",
                                         "evcache.clusters[0].cachePrefix=test1",
                                         "spring.application.name=test")
                     .withUserConfiguration(NoEurekaClientConfiguration.class)
                     .run(context -> assertAll(
                         () -> assertThat(context).doesNotHaveBean(EVCacheClientPoolManager.class),
                         () -> assertThat(context.getEnvironment().containsProperty("evcache.use.simple.node.list.provider")).isFalse()
                     ));
    }

    @Test
    void should_be_not_loaded_EVCacheClientPoolManager_when_not_exists_not_evcacheManager() {
        contextRunner.withPropertyValues("spring.cache.type=none", "spring.application.name=test")
                     .withUserConfiguration(EnableCachingConfiguration.class)
                     .run(context -> assertThat(context).doesNotHaveBean(EVCacheClientPoolManager.class));
    }

    @Test
    void should_be_not_loaded_EVCacheClientPoolManager_when_not_enableCaching() {
        contextRunner.withPropertyValues("spring.cache.type=none", "spring.application.name=test")
                     .withUserConfiguration(NoCacheableConfiguration.class)
                     .run(context -> assertThat(context).doesNotHaveBean(EVCacheClientPoolManager.class));
    }

    @Configuration
    static class NoCacheableConfiguration {
    }

    @Configuration
    @EnableCaching
    static class EnableCachingConfiguration {
        @Bean
        ApplicationInfoManager applicationInfoManager() {
            final ApplicationInfoManager result = mock(ApplicationInfoManager.class);
            final InstanceInfo instanceInfo = mock(InstanceInfo.class);
            doReturn(instanceInfo).when(result).getInfo();
            final DataCenterInfo dataCenterInfo = new MyDataCenterInfo(DataCenterInfo.Name.MyOwn);
            doReturn(dataCenterInfo).when(instanceInfo).getDataCenterInfo();
            return result;
        }

        @Bean
        EurekaClient eurekaClient() {
            return mock(DiscoveryClient.class);
        }
    }

    @Configuration
    @EnableCaching
    static class EnableCachingConfigurationWithProxyBean extends EnableCachingConfiguration {
        @Bean
        ApplicationInfoManager applicationInfoManager() {
            return super.applicationInfoManager();
        }

        @Bean
        EurekaClient eurekaClient() {
            return (EurekaClient) new DefaultAopProxyFactory().createAopProxy(new ProxyFactory(mock(DiscoveryClient.class))).getProxy();
        }
    }

    @Configuration
    @EnableCaching
    static class ExistsEVCacheClientPoolManagerConfiguration extends EnableCachingConfiguration {
        @Bean
        EVCacheClientPoolManager evCacheClientPoolManager() {
            return mock(EVCacheClientPoolManager.class);
        }
    }

    @Configuration
    @EnableCaching
    static class NoEurekaClientConfiguration {
    }
}
