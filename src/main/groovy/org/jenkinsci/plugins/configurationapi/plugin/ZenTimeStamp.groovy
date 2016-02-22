package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import hudson.plugins.zentimestamp.ZenTimestampNodeProperty
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.jenkinsci.plugins.configurationapi.NodeConfigurationStream
import org.jenkinsci.plugins.configurationapi.PluginConfigurationStream

@Extension
class ZenTimeStamp implements PluginConfigurationStream, NodeConfigurationStream
{
    // =================== PluginConfigurationStream

    @Override
    String getPluginId()
    {
        return "zentimestamp"
    }

    @Override
    Map doExport(Jenkins instance, PluginWrapper plugin)
    {
        // This plugin has no global configuration. Only node configurations.
        return [:]
    }

    @Override
    void doImport(Jenkins instance, Map configuration)
    {
        // Nothing to do
    }


    // =================== NodeConfigurationStream
    @Override
    String getNodePropertyClass()
    {
        return ZenTimestampNodeProperty.getName()
    }

    @Override
    Map doExport(Jenkins instance, NodeProperty nodeProperty)
    {
        ZenTimestampNodeProperty timestamp = (ZenTimestampNodeProperty) nodeProperty
        return [
                'pattern': timestamp.getPattern()
        ]
    }

    @Override
    void doImport(Jenkins instance, DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties, Map
            configuration)
    {
        ZenTimestampNodeProperty timestamp = new ZenTimestampNodeProperty(configuration['pattern'])
        nodeProperties.replace(timestamp)
    }

}

