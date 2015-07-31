package com.orange.oss.cloudfoundry.cscpi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.oss.cloudfoundry.cscpi.domain.CPIResponse;
import com.orange.oss.cloudfoundry.cscpi.domain.Network;
import com.orange.oss.cloudfoundry.cscpi.domain.Networks;
import com.orange.oss.cloudfoundry.cscpi.domain.ResourcePool;

public class CPIAdapterImpl implements CPIAdapter {

	private static Logger logger = LoggerFactory.getLogger(CPIAdapterImpl.class.getName());

	@Autowired
	private CPI cpi;


	@Override
	public CPIResponse execute(JsonNode json) {

		
		ObjectMapper mapper=new ObjectMapper();
		//prepare response
		CPIResponse response = new CPIResponse();
		
		try {

			String method = json.get("method").asText();
			logger.info("method : {}", method);

			String arguments = json.get("arguments").textValue();
			logger.info("arguments : {}", arguments);

			String context = json.get("context").textValue();
			logger.info("context : {}", context);
			
			Iterator<JsonNode> args=json.get("arguments").elements();
			
			

			if (method.equals("create_disk")) {
				Integer size=args.next().asInt();
				Map<String, String> cloud_properties=new HashMap<String, String>();
				String diskId=this.cpi.create_disk(size,cloud_properties);
				response.result.add(diskId);

			} else if (method.equals("delete_disk")) {
				String disk_id=args.next().asText();
				this.cpi.delete_disk(disk_id);


			} else if (method.equals("attach_disk")) {
				String vm_id=args.next().asText();
				String disk_id=args.next().asText();;
				this.cpi.attach_disk(vm_id, disk_id);

			} else if (method.equals("detach_disk")) {
				String vm_id=args.next().asText();
				String disk_id=args.next().asText();
				this.cpi.detach_disk(vm_id, disk_id);

			} else if (method.equals("create_vm")) {

				//FIXME: TODO
				String agent_id=args.next().asText();;
				String stemcell_id=args.next().asText();;
				ResourcePool resource_pool=this.parseResourcePool(args.next());
				Networks networks=this.parseNetwork(args.next());
				
				List<String> disk_locality=new ArrayList<String>();				
				Map<String, String> env=new HashMap<String, String>();

				String vmId=this.cpi.create_vm(agent_id, stemcell_id, resource_pool, networks, disk_locality, env);
				response.result.add(vmId);
				
				
			} else if (method.equals("reboot_vm")) {
				String vm_id=args.next().asText();
				this.cpi.reboot_vm(vm_id);

			} else if (method.equals("set_vm_metadata")) {
				String vm_id=args.next().asText();
				Map<String, String> metadata=mapper.convertValue(args.next(), HashMap.class);
				this.cpi.set_vm_metadata(vm_id, metadata);
				
			} else if (method.equals("delete_vm")) {
				String vm_id=args.next().asText();
				this.cpi.delete_vm(vm_id);

			} else if (method.equals("create_stemcell")) {
				String image_path=args.next().asText();				
				Map<String, String> cloud_properties=mapper.convertValue(args.next(), HashMap.class);
				
				String stemcell=this.cpi.create_stemcell(image_path, cloud_properties);
				response.result.add(stemcell);

			} else
				throw new IllegalArgumentException("Unknown method :" + method);

			
			return response;

		}
//		 catch (CPIException e) {
//		 logger.error("Caught Exception {}, converted to CPI response.", e);
//		 CPIResponse response = new CPIResponse();
//		 response.error = e.toString() + "\n" + e.getMessage() + "\n"
//		 + e.getCause();
//		 return response;
//		
//		 }

		catch (Exception e) {
			logger.error("Caught Exception {}, converted to CPI response.", e);
			response.error = e.toString() + "\n" + e.getMessage() + "\n" + e.getCause();
			return response;
		}

	}
	
	
	
	/**
	 * Utility to parse JSON resource pool
	 * @param resource_pool
	 * @return
	 */
	private ResourcePool parseResourcePool(JsonNode resource_pool) {
    	ObjectMapper mapper=new ObjectMapper();
    	ResourcePool rp=mapper.convertValue(resource_pool, ResourcePool.class);
    	return rp;
	}
	
	/**
	 * Utility to parse JSON network list
	 * @param networks
	 * @return
	 */
    private Networks parseNetwork(JsonNode networks) {
    	ObjectMapper mapper=new ObjectMapper();
    	
    	Networks nets=new Networks();
    	Iterator<JsonNode> it=networks.elements();
    	
    	//FIXME :cant parse the bosh network name (unused anyway but ...)
    	
    	while (it.hasNext()){
    		JsonNode n=it.next();
    		nets.networks.put("xx",mapper.convertValue(n, Network.class));
    	}

		return nets;
	}
	
}
