package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import hudson.plugins.zentimestamp.ZenTimestampNodeProperty
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ConfigurationImport
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
    Map doExport(ConfigurationExport.Context context, PluginWrapper plugin)
    {
        // This plugin has no global configuration. Only node configurations.
        return [:]
    }

    @Override
    void doImport(ConfigurationImport.Context context, Map configuration)
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
    Map doExport(ConfigurationExport.Context context, NodeProperty nodeProperty)
    {
        ZenTimestampNodeProperty timestamp = (ZenTimestampNodeProperty) nodeProperty
        return [
                'pattern': timestamp.getPattern()
        ]
    }

    @Override
    void doImport(ConfigurationImport.Context context, DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties, Map
            configuration)
    {
        ZenTimestampNodeProperty timestamp = new ZenTimestampNodeProperty(configuration['pattern'])
        nodeProperties.replace(timestamp)
    }

}

