package com.orange.oss.cloudfoundry.cscpi.boshregistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * bosh registry client see
 * https://github.com/cloudfoundry/bosh/blob/master/bosh
 * -registry/spec/unit/bosh/registry/api_controller_spec.rb
 * 
 * @author poblin
 *
 */
public class BoshRegistryClientImpl implements BoshRegistryClient {

	private static Logger logger = LoggerFactory.getLogger(BoshRegistryClientImpl.class.getName());

	@Value("${registry.endpoint}")
	String endpoint;

	@Value("${registry.user}")
	String user;

	@Value("${registry.password}")
	String password;

	@Override
	public void put(String vm_id, String settings) {

		String uri = "http://localhost:8080/instances/" + vm_id;
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(uri, settings, String.class);

	}

	@Override
	public String get(String vm_id) {
		String uri = "http://localhost:8080/instances/" + vm_id + "/settings";
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(uri, String.class);
		return result;
	}

}
