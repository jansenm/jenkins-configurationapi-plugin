package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import hudson.PluginWrapper
import jenkins.model.Jenkins

interface PluginConfigurationStream extends ExtensionPoint
{
    public abstract String getPluginId()

    public abstract Map doExport(Jenkins instance, PluginWrapper plugin)

    public abstract void doImport(Jenkins instance, Map configuration)
}
