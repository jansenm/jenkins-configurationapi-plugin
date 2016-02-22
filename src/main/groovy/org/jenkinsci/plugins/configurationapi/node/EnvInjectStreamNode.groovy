package org.jenkinsci.plugins.configurationapi.node

import hudson.Extension
import hudson.model.Node
import hudson.slaves.NodeProperty
import jenkins.model.Jenkins
import org.apache.commons.lang.NotImplementedException
import org.jenkinsci.plugins.envinject.EnvInjectNodeProperty

@Extension
class EnvInjectStreamNode implements NodeConfigurationStream
{
    @Override
    void doImport(Jenkins instance, Node node, Map configuration)
    {
        throw new NotImplementedException()
    }

    @Override
    Map doExport(Jenkins instance, NodeProperty nodeProperty)
    {
        EnvInjectNodeProperty property = (EnvInjectNodeProperty) nodeProperty
        return [
                'unsetSystemVariables': property.isUnsetSystemVariables(),
                'propertiesFilePath'  : property.getPropertiesFilePath()
        ]
    }

    @Override
    String getNodePropertyClass()
    {
        return EnvInjectNodeProperty.getName()
    }
}
