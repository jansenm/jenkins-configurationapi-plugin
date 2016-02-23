package org.jenkinsci.plugins.configurationapi.core

import hudson.Extension
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.jenkinsci.plugins.configurationapi.NodeConfigurationStream

@Extension
class EnvironmentVariablesConfiguration implements NodeConfigurationStream
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
    void doImport(Jenkins jenkins, DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties, Map
            configuration)
    {
        EnvironmentVariablesNodeProperty nodeProperty = nodeProperties.get(EnvironmentVariablesNodeProperty)
        if (nodeProperty == null) {
            nodeProperty = new EnvironmentVariablesNodeProperty()
            nodeProperties.add(nodeProperty)
        }
        nodeProperty.envVars.clear()
        nodeProperty.envVars.putAll(configuration)
    }
}

