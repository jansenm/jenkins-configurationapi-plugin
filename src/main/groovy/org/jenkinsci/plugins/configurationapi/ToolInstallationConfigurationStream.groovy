package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.tools.ToolDescriptor

interface ToolInstallationConfigurationStream extends ExtensionPoint
{
    public String getToolInstallationClass()

    public Map doExport(ConfigurationExport.Context context, ToolDescriptor toolDescriptor)

    public void doImport(ConfigurationImport.Context context, Map configuration)
}
