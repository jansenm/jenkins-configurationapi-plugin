package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList

interface NodeConfigurationStream extends ExtensionPoint
{
    public String getNodePropertyClass()

    public Map doExport(ConfigurationExport.Context context, NodeProperty nodeProperty)

    public void doImport(
            ConfigurationImport.Context context,
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties,
            Map configuration)
}
