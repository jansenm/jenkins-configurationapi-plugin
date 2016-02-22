package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.Jenkins

interface NodeConfigurationStream extends ExtensionPoint
{
    public String getNodePropertyClass()

    public Map doExport(Jenkins instance, NodeProperty nodeProperty)

    public void doImport(
            Jenkins jenkins,
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties,
            Map configuration)
}
