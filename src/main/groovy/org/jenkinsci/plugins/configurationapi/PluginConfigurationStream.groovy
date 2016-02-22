package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.PluginWrapper
import jenkins.model.Jenkins

interface PluginConfigurationStream extends ExtensionPoint
{
    public String getPluginId()

    public Map doExport(Jenkins instance, PluginWrapper plugin)

    public void doImport(Jenkins instance, Map configuration)
}
