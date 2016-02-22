package org.jenkinsci.plugins.configurationapi.node

import hudson.Extension
import hudson.model.Node
import hudson.slaves.NodeProperty
import hudson.tools.ToolDescriptor
import hudson.tools.ToolLocationNodeProperty
import jenkins.model.Jenkins

@Extension
class ToolLocationStream extends ConfigurationStream
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
    void doImport(Jenkins jenkins, Node node, Map configuration)
    {
        def toolLocations = []
        configuration.each { tl ->
            toolLocations << new ToolLocationNodeProperty.ToolLocation(
                    (ToolDescriptor)jenkins.getDescriptor((String)tl['type']),
                    (String)tl['name'],
                    (String)tl['home']
            )
        }
        node.nodeProperties.clear()
        node.nodeProperties.addAll(toolLocations)
    }
}

