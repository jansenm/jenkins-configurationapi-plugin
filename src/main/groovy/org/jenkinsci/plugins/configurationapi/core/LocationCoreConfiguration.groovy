package org.jenkinsci.plugins.configurationapi.core

import hudson.Extension
import jenkins.model.JenkinsLocationConfiguration
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ConfigurationImport
import org.jenkinsci.plugins.configurationapi.CoreConfigurationStream

@Extension
class LocationCoreConfiguration implements CoreConfigurationStream
{
    @Override
    void doImport(ConfigurationImport.Context context, Map configuration)
    {
        def locationConfig = JenkinsLocationConfiguration.get()
        locationConfig.setAdminAddress((String)configuration['adminAddress'])
        locationConfig.setUrl((String)configuration.url)
    }

    @Override
    Map doExport(ConfigurationExport.Context context)
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

