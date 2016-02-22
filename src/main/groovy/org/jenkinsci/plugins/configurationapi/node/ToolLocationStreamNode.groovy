package org.jenkinsci.plugins.configurationapi.node

import hudson.Extension
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.tools.ToolDescriptor
import hudson.tools.ToolLocationNodeProperty
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.jenkinsci.plugins.configurationapi.NodeConfigurationStream

@Extension
class ToolLocationStreamNode implements NodeConfigurationStream
{

    @Override
    String getNodePropertyClass()
    {
        return ToolLocationNodeProperty.getClass().getName()
    }

    @Override
    Map doExport(Jenkins jenkins, NodeProperty property)
    {
        def rc = [:]
        ToolLocationNodeProperty toolLocations = (ToolLocationNodeProperty) property
        toolLocations.getLocations().each { tl ->
            rc[(tl.getName())] = [
                    'type': tl.getType(),
                    'home': tl.getHome(),
                    'name': tl.getName(),
            ]

        }
        return rc
    }

    @Override
    void doImport(Jenkins jenkins, DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties, Map
            configuration)
    {
        def toolLocations = []
        configuration.each { tl ->
            toolLocations << new ToolLocationNodeProperty.ToolLocation(
                    (ToolDescriptor)jenkins.getDescriptor((String)tl['type']),
                    (String)tl['name'],
                    (String)tl['home']
            )
        }
        nodeProperties.clear()
        nodeProperties.addAll(toolLocations)
    }
}

