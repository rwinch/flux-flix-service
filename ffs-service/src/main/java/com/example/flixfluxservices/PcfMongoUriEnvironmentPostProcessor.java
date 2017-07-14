/*
 * Copyright 2012-2017 the original author or authors.
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

package com.example.flixfluxservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * If present, VCAP_SERVICES environment variable and includes the Mongo URI as {@code spring.data.monogodb.uri} in the
 * Spring Environment. For example:
 *
 * <pre>
 * {
 * "VCAP_SERVICES": {
 * "mlab": [
 * {
 * "credentials": {
 * "uri": "mongodb://CloudFoundry_abc:123@SOMETEST.mlab.com:12345/CloudFoundry_XYZ"
 * },
 * ....
 * </pre>
 *
 * Will populate the {@code spring.data.monogodb.uri} with
 * "mongodb://CloudFoundry_abc:123@SOMETEST.mlab.com:12345/CloudFoundry_XYZ"
 *
 * @author Rob Winch
 */
public class PcfMongoUriEnvironmentPostProcessor implements EnvironmentPostProcessor {
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String mongoUri = getMongoUriFromVcapServices();
		if(mongoUri == null) {
			return;
		}
		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.data.mongodb.uri", mongoUri);
		MapPropertySource propertySource = new MapPropertySource("pcf", properties);
		environment.getPropertySources().addFirst(propertySource);
	}

	private static String getMongoUriFromVcapServices() {
		try {
			String vcapJson = System.getenv("VCAP_SERVICES");
			if (vcapJson == null) {
				return null;
			}
			Map<String, List<Map<String,
					Map<String, String>>>> vcap = new ObjectMapper().readValue(vcapJson, Map.class);
			return vcap.get("mlab").get(0).get("credentials").get("uri");
		} catch(Exception e) {
			return null;
		}
	}
}
