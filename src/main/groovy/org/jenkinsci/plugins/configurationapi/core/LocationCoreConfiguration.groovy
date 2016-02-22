package org.jenkinsci.plugins.configurationapi.core

import hudson.Extension
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

@Extension
class LocationCoreConfiguration implements CoreConfigurationStream
{
    @Override
    void doImport(Jenkins instance, Map configuration)
    {
        def locationConfig = JenkinsLocationConfiguration.get()
        locationConfig.setAdminAddress((String)configuration['adminAddress'])
        locationConfig.setUrl((String)configuration.url)
    }

    @Override
    Map doExport(Jenkins instance)
    {
        def locationConfig = JenkinsLocationConfiguration.get()
        return [
                adminAddress: locationConfig.getAdminAddress(),
                url: locationConfig.getUrl(),
        ]
    }

    @Override
    String getId()
    {
        return JenkinsLocationConfiguration.getName()
    }
}

