package org.jenkinsci.plugins.configurationapi.node

import hudson.Extension
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.jenkinsci.plugins.configurationapi.NodeConfigurationStream
import org.jenkinsci.plugins.envinject.EnvInjectNodeProperty

@Extension
class EnvInjectStreamNode implements NodeConfigurationStream
{
    @Override
    void doImport(Jenkins jenkins, DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties, Map
            configuration)
    {
        EnvInjectNodeProperty envInject = new EnvInjectNodeProperty(
                (boolean)configuration['unsetSystemVariables'],
                (String) configuration['propertiesFilePath'])
        nodeProperties.replace(envInject)
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
