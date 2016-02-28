package org.jenkinsci.plugins.configurationapi.core

import hudson.Extension
import hudson.tools.InstallSourceProperty
import hudson.tools.ToolProperty
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ConfigurationImport
import org.jenkinsci.plugins.configurationapi.ToolPropertyConfigurationStream

@Extension
class InstallSourcePropertyConfiguration implements ToolPropertyConfigurationStream
{
    @Override
    public ToolProperty doImport(ConfigurationImport.Context context, Map configuration)
    {
        return new InstallSourceProperty(
                configuration.installers.collect() {
                    context.getImporter().importToolInstaller(context, (Map)it)
                })
    }

    @Override
    public Map doExport(ConfigurationExport.Context context, ToolProperty property)
    {
        def installSourceProperty = (InstallSourceProperty) property
        return [
                installers: installSourceProperty.installers.collect() {
                    context.getExporter().exportToolInstaller(context, it)
                }
        ]
    }

    @Override
    public String getToolPropertyClass()
    {
        return InstallSourceProperty.getName()
    }

}
