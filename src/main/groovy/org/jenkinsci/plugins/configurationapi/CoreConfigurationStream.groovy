package org.jenkinsci.plugins.configurationapi

import hudson.ExtensionPoint
import jenkins.model.Jenkins

interface CoreConfigurationStream extends ExtensionPoint
{
    public String getId()

    public Map doExport(Jenkins instance)

    public void doImport(Jenkins instance, Map configuration)
}
