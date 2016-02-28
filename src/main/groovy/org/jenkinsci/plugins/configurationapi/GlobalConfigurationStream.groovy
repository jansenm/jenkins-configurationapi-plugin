package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import jenkins.model.GlobalConfiguration

interface GlobalConfigurationStream extends ExtensionPoint
{
    public String getGlobalConfigurationClass()

    public Map doExport(ConfigurationExport.Context context, GlobalConfiguration config)

    public void doImport(
            ConfigurationImport.Context context,
            GlobalConfiguration config,
            Map configuration)
}
