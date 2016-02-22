package org.jenkinsci.plugins.configurationapi.plugin

import hudson.ExtensionPoint
import hudson.PluginWrapper
import jenkins.model.Jenkins

abstract class ConfigurationStream implements ExtensionPoint
{
    public abstract String getPluginId()

    public abstract Map doExport(Jenkins instance, PluginWrapper plugin)

    public abstract void doImport(Jenkins instance, Map configuration)
}
