package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.tools.ToolInstaller

interface ToolInstallerConfigurationStream extends ExtensionPoint
{
    public String getToolInstallerClass()

    public Map doExport(ConfigurationExport.Context context, ToolInstaller installer)

    public ToolInstaller doImport(ConfigurationImport.Context context, Map configuration)
}
