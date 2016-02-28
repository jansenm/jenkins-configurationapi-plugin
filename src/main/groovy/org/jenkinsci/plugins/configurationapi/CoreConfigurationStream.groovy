package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint

interface CoreConfigurationStream extends ExtensionPoint
{
    public String getId()

    public Map doExport(ConfigurationExport.Context context)

    public void doImport(ConfigurationImport.Context context, Map configuration)
}
