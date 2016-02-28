package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.PluginWrapper

interface PluginConfigurationStream extends ExtensionPoint
{
    public String getPluginId()

    public Map doExport(ConfigurationExport.Context context, PluginWrapper plugin)

    public void doImport(ConfigurationImport.Context context, Map configuration)
}
