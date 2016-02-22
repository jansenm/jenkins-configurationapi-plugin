package org.jenkinsci.plugins.configurationapi.node

import hudson.Extension
import hudson.model.Node
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.NodeProperty
import jenkins.model.Jenkins

@Extension
class EnvironmentVariablesStreamNode implements NodeConfigurationStream
{

    @Override
    String getNodePropertyClass()
    {
        return EnvironmentVariablesNodeProperty.getName()
    }

    @Override
    Map doExport(Jenkins jenkins, NodeProperty property)
    {
        def envVars = (EnvironmentVariablesNodeProperty) property
        def rc = [:]
        envVars.envVars.each { ev, val ->
            rc[ev] = val
        }
        return rc
    }

    @Override
    void doImport(Jenkins jenkins, Node node, Map configuration)
    {
        EnvironmentVariablesNodeProperty nodeProperty = jenkins.getNodeProperties(EnvironmentVariablesNodeProperty)
        if (nodeProperty == null){
            nodeProperty = new EnvironmentVariablesNodeProperty()
            Jenkins.instance.globalNodeProperties.add(nodeProperty)
        }
        nodeProperty.envVars.clear()
        nodeProperty.envVars.putAll(configuration)
    }
}

