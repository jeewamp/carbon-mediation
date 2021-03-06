/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.inbound.endpoint.protocol.file;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskStartupObserver;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

public class VFSProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver {

    private static final String ENDPOINT_POSTFIX = "FILE-EP";
    
	private FilePollingConsumer fileScanner;    
    private Properties vfsProperties;    
    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    
    private static final Log log = LogFactory.getLog(VFSProcessor.class);

    public VFSProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.vfsProperties = params.getProperties();
        this.interval =
                Long.parseLong(vfsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL));
        this.sequential = true;
        if (vfsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL) != null) {
            this.sequential = Boolean.parseBoolean(vfsProperties
                    .getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }
        this.coordination = true;
        if (vfsProperties.getProperty(PollingConstants.INBOUND_COORDINATION) != null) {
            this.coordination = Boolean.parseBoolean(vfsProperties
                    .getProperty(PollingConstants.INBOUND_COORDINATION));
        }
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
    }

    public void init() {
    	log.info("Inbound file listener " + name + " starting ...");
    	fileScanner = new FilePollingConsumer(vfsProperties, name, synapseEnvironment, interval);
    	fileScanner.registerHandler(new FileInjectHandler(injectingSeq, onErrorSeq, sequential, synapseEnvironment, vfsProperties));
    	start();
    }
      
    /**
     * Register/start the schedule service
     * */
    public void start() {       
        log.info("Inbound FILE listener Started : " + name);
        Task task = new FileTask(fileScanner);
        start(task, ENDPOINT_POSTFIX);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public void update() {
		start();
	}
}


