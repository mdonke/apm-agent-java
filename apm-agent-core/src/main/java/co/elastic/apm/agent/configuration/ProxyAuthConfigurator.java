/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2020 Elastic and contributors
 * %%
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
package co.elastic.apm.agent.configuration;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.apm.agent.context.AbstractLifecycleListener;
import co.elastic.apm.agent.impl.ElasticApmTracer;

/**
 * Sets a new proxy authenticator as suggested in https://rolandtapken.de/blog/2012-04/java-process-httpproxyuser-and-httpproxypassword
 * <p>
 */
public class ProxyAuthConfigurator extends AbstractLifecycleListener {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAuthConfigurator.class);

	public ProxyAuthConfigurator() {

	}

	@Override
	public void init(ElasticApmTracer tracer) {
		logger.info("Setting Proxy Auth Config...");
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				if (getRequestorType() == RequestorType.PROXY) {
					String prot = getRequestingProtocol().toLowerCase();
					String host = System.getProperty(prot + ".proxyHost", "");
					String port = System.getProperty(prot + ".proxyPort", "");
					String user = System.getProperty(prot + ".proxyUser", "");
					String password = System.getProperty(prot + ".proxyPassword", "");
					if (getRequestingHost().toLowerCase().equals(host.toLowerCase())) {
						if (Integer.parseInt(port) == getRequestingPort()) {
							logger.trace("Sending auth data for host requested: {}, port requested: {}", getRequestingHost(), getRequestingPort());
							return new PasswordAuthentication(user, password.toCharArray());
						}
					}
				}
				return null;
			}
		});
	}
}
