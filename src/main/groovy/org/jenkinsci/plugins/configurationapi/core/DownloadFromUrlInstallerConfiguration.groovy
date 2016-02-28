package org.jenkinsci.plugins.configurationapi.core

import hudson.tools.DownloadFromUrlInstaller
import hudson.tools.ToolInstaller
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ToolInstallerConfigurationStream

abstract class DownloadFromUrlInstallerConfiguration implements ToolInstallerConfigurationStream
{
    @Override
    Map doExport(ConfigurationExport.Context context, ToolInstaller installer)
    {
        def downloadFromUrlInstaller = (DownloadFromUrlInstaller) installer
        def installable = downloadFromUrlInstaller.getInstallable()
        return [
                // Not supported by DownloadFromUrlInstaller
                // label      : downloadFromUrlInstaller.getLabel(),
                id         : downloadFromUrlInstaller.id,
                installable: [
                        name: installable.name,
                        url : installable.url
                ]
        ]
    }

}
