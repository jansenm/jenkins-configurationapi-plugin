package org.jenkinsci.plugins.configurationapi.node

import hudson.Extension
import hudson.model.Node
import hudson.plugins.zentimestamp.ZenTimestampNodeProperty
import hudson.slaves.NodeProperty
import jenkins.model.Jenkins

@Extension
class ZenTimeStampStreamNode implements NodeConfigurationStream
{
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
    void doImport(Jenkins instance, Node node, Map configuration)
    {
    }
}
