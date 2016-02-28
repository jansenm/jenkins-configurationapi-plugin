package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.GlobalConfiguration
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ConfigurationImport

import org.jenkinsci.plugins.configurationapi.NodeConfigurationStream
import org.jenkinsci.plugins.configurationapi.PluginConfigurationStream
import org.jenkinsci.plugins.envinject.EnvInjectNodeProperty
import org.jenkinsci.plugins.envinject.EnvInjectPluginConfiguration

@Extension
class EnvInject implements NodeConfigurationStream, PluginConfigurationStream
{
    /*
     * NODE
     */

    @Override
    void doImport(ConfigurationImport.Context context, DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties, Map
            configuration)
    {
        EnvInjectNodeProperty envInject = new EnvInjectNodeProperty(
                (boolean) configuration['unsetSystemVariables'],
                (String) configuration['propertiesFilePath'])
        nodeProperties.replace(envInject)
    }

    @Override
    Map doExport(ConfigurationExport.Context context, NodeProperty nodeProperty)
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

    /*
     * PLUGIN
     */

    @Override
    String getPluginId()
    {
        return 'envinject'
    }

    @Override
    Map doExport(ConfigurationExport.Context context, PluginWrapper plugin)
    {
        EnvInjectPluginConfiguration config = EnvInjectPluginConfiguration.getInstance()
        return [
                hideInjectedVars : config.isHideInjectedVars(),
                enablePermissions: config.isEnablePermissions()
        ]
    }

    @Override
    void doImport(ConfigurationImport.Context context, Map configuration)
    {
        GlobalConfiguration.all().remove(EnvInjectPluginConfiguration.getInstance())
        // But how to activate it?
        GlobalConfiguration.all().add(new EnvInjectPluginConfiguration(
                (boolean) configuration.hideInjectedVars,
                (boolean) configuration.enablePermissions
        ))
    }
}
