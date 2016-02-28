package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.tools.ToolProperty

interface ToolPropertyConfigurationStream extends ExtensionPoint
{
    public String getToolPropertyClass()

    public Map doExport(ConfigurationExport.Context context, ToolProperty installer)

    public ToolProperty doImport(ConfigurationImport.Context context, Map configuration)
}
