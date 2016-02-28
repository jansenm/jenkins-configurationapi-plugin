package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import hudson.tasks.Maven.MavenInstallation
import hudson.tools.ToolDescriptor
import hudson.tools.ToolProperty
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ConfigurationImport
import org.jenkinsci.plugins.configurationapi.PluginConfigurationStream
import org.jenkinsci.plugins.configurationapi.ToolInstallationConfigurationStream
import org.jenkinsci.plugins.configurationapi.core.DownloadFromUrlInstallerConfiguration

class Maven
{

    @Extension
    static class Plugin implements PluginConfigurationStream
    {
        @Override
        String getPluginId()
        {
            return "maven-plugin"
        }

        @Override
        void doImport(ConfigurationImport.Context context, Map configuration)
        {

        }

        @Override
        Map doExport(ConfigurationExport.Context context, PluginWrapper toolInstallation)
        {
            return
        }
    }

    @Extension
    static class ToolInstallation implements ToolInstallationConfigurationStream
    {

        @Override
        String getToolInstallationClass()
        {
            return MavenInstallation.DescriptorImpl.getName()
        }

        @Override
        Map doExport(ConfigurationExport.Context context, ToolDescriptor toolDescriptor)
        {
            def mavenDescriptor = (MavenInstallation.DescriptorImpl) toolDescriptor
            def rc = [:]
            mavenDescriptor.getInstallations().each { installation ->
                rc[installation.getName()] = [
                        home      : installation.getHome(),
                        properties: installation.getProperties().collect() {
                            context.getExporter().exportToolProperties(context, it)
                        }
                ]
            }

            return rc
        }


        @Override
        void doImport(ConfigurationImport.Context context, Map configuration)
        {
            def mavenDescriptor = context.getJenkins().getDescriptorByType(MavenInstallation.DescriptorImpl)
            def mavenInstallations = new ArrayList<MavenInstallation>()
            configuration.each() { String name, Map installConfiguration ->
                mavenInstallations.add(new MavenInstallation(
                        name,
                        (String) installConfiguration.home,
                        context.getImporter().importToolProperties(context, (List)installConfiguration.properties)
                ))
            }
            mavenDescriptor.setInstallations(mavenInstallations.toArray(new MavenInstallation[mavenInstallations.size ()]))
        }
    }

    @Extension
    public static class MavenInstaller extends DownloadFromUrlInstallerConfiguration
    {
        @Override
        String getToolInstallerClass()
        {
            return hudson.tasks.Maven.MavenInstaller.getName()
        }

        @Override
        hudson.tasks.Maven.MavenInstaller doImport(ConfigurationImport.Context context, Map configuration)
        {
            return new hudson.tasks.Maven.MavenInstaller((String) configuration.id)
            // The installables are only exported for humans convenience. They are hardcoded.
        }
    }
}
