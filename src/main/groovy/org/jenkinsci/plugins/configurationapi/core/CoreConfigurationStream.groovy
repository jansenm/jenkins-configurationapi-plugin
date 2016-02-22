package org.jenkinsci.plugins.configurationapi.core

import hudson.ExtensionPoint
import jenkins.model.Jenkins

interface CoreConfigurationStream extends ExtensionPoint
{
    public abstract String getId()

    public abstract Map doExport(Jenkins instance)

    public abstract void doImport(Jenkins instance, Map configuration)
}
