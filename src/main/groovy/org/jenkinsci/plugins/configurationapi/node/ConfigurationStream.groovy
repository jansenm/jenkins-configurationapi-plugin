package org.jenkinsci.plugins.configurationapi.node

import hudson.ExtensionPoint
import hudson.model.Node
import hudson.slaves.NodeProperty
import jenkins.model.Jenkins

abstract class ConfigurationStream implements ExtensionPoint
{
    public abstract String getNodePropertyClass()

    public abstract Map doExport(Jenkins instance, NodeProperty nodeProperty)

    public abstract void doImport(Jenkins instance, Node node, Map configuration)
}
