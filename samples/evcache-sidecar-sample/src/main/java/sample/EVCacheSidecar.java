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

package sample;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.github.aafwu00.evcache.sidecar.spring.cloud.EnableEVCacheSidecar;

/**
 * see application.yml
 *
 * @author Taeho Kim
 */
@SpringBootApplication
@EnableEVCacheSidecar
public class EVCacheSidecar {
    public static void main(final String[] args) {
        new SpringApplicationBuilder(EVCacheSidecar.class).run(args);
    }
}
