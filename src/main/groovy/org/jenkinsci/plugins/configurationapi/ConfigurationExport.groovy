package org.jenkinsci.plugins.configurationapi

import groovy.json.JsonOutput
import hudson.Extension
import hudson.PluginWrapper
import hudson.cli.CLICommand
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.tools.ToolDescriptor
import hudson.tools.ToolInstallation
import hudson.tools.ToolInstaller
import hudson.tools.ToolProperty
import hudson.util.DescribableList
import jenkins.model.GlobalConfiguration
import jenkins.model.Jenkins
import org.kohsuke.args4j.Argument

import java.util.logging.Logger

@Extension
public class ConfigurationExport extends ConfigurationBase
{
    public static class Context extends ConfigurationBase.Context
    {

        public Context(Jenkins jenkins, ConfigurationExport exporter)
        {
            super(jenkins)
            this.exporter = exporter
        }

        public ConfigurationExport getExporter()
        {
            return exporter
        }

        private ConfigurationExport exporter
    }

    @Argument(
            usage = "password for encryption of credentials",
            metaVar = "PASSWORD",
            required = false,
            multiValued = false)
    private String password = null

    private static final Logger LOGGER = Logger.getLogger(ConfigurationExport.class.getName())

    @Override
    String getShortDescription()
    {
        // :TODO: Add short description
        return "todo"
    }

    private Map exportCoreConfiguration(Context context)
    {
        def rc = [:]
        def extensions = context.getJenkins().getExtensionList(GlobalConfigurationStream)
        for (config in GlobalConfiguration.all())
        {
            // Look for an extension that knows how to export the plugin
            def stream = extensions.find {
                config.getClass().getName() == it.getGlobalConfigurationClass()
            }
            // Determine that success
            def state
            def configuration
            if (stream == null)
            {
                stderr.println("Unsupported global configuration ${config.getClass().getName()} found!")
                state = RETURN_CODES.UNSUPPORTED
                configuration = null
            }
            else
            {
                state = RETURN_CODES.SUCCESS
                configuration = stream.doExport(context, config)
            }
            // Add the plugin to the export
            rc["${config.getClass().getName()}"] = [
                    "status"       : state,
                    "configuration": configuration
            ]
        }
        return rc

        /* OLD IMPLEMENTATION
        def rc = [:]
        // Look for an extension that knows how to export the plugin
        jenkins.getExtensionList(CoreConfigurationStream).each { stream ->
            rc[stream.getId()] = stream.doExport(jenkins)
        }
        return rc
        */
    }

    private Map exportNodeConfiguration(
            Context context,
            DescribableList<NodeProperty<Node>, NodePropertyDescriptor> properties)
    {

        def rc = [:]
        def extensions = context.getJenkins().getExtensionList(NodeConfigurationStream)
        for (nodeProperty in properties)
        {
            // Look for an extension that knows how to export the plugin
            def stream = extensions.find {
                nodeProperty.getClass().getName() == it.getNodePropertyClass()
            }
            // Determine that success
            def state
            def configuration
            if (stream == null)
            {
                stderr.println("Unsupported node propery ${nodeProperty.getClass().getName()} found!")
                state = RETURN_CODES.UNSUPPORTED
                configuration = null
            }
            else
            {
                state = RETURN_CODES.SUCCESS
                configuration = stream.doExport(context, nodeProperty)
            }
            // Add the plugin to the export
            rc["${nodeProperty.getClass().getName()}"] = [
                    "status"       : state,
                    "configuration": configuration
            ]
        }
        return rc
    }

    private Map exportPluginConfiguration(Context context, PluginWrapper plugin)
    {
        // Look for an extension that knows how to export the plugin
        def stream = context.getJenkins().getExtensionList(PluginConfigurationStream).find {
            plugin.getShortName() == it.getPluginId()
        }
        if (stream == null)
        {
            stderr.println("Unsupported plugin ${plugin.getShortName()} found!")
            return null
        }
        return stream.doExport(context, plugin)
    }

    @Override
    protected int run() throws Exception
    {
        def context = new Context(Jenkins.getInstance(), this)

        if (context.getJenkins() == null)
        {
            stderr.println("The Jenkins jenkins has not been started, or was already shut down!");
            return -1;
        }

        //
        // Export the jenkins core configuration
        // =====================================
        //
        Map core = exportCoreConfiguration(context)

        //
        // Export the global plugin configurations
        // =======================================
        //
        def plugins = [:]
        for (plugin in context.getJenkins().getPluginManager().getPlugins())
        {
            // Try to export the plugins configuration
            def configuration = exportPluginConfiguration(context, plugin)
            // Determine that success
            def state
            if (configuration == null)
            {
                state = RETURN_CODES.UNSUPPORTED
            }
            else
            {
                state = RETURN_CODES.SUCCESS
            }
            // Add the plugin to the export
            plugins[plugin.getShortName()] = [
                    "name"         : plugin.getShortName(),
                    "displayName"  : plugin.getDisplayName(),
                    "dependencies" : plugin.getDependencies().collectEntries { [(it.shortName): it.version] },
                    "version"      : plugin.getVersion(),
                    "enabled"      : plugin.isEnabled(),
                    "status"       : state,
                    "configuration": configuration
            ]
        }

        //
        // Export the global node configuration
        // ====================================
        //
        def global_node = exportNodeConfiguration(context, context.getJenkins().getGlobalNodeProperties())

        //
        // Export tool installations
        // ====================================
        //
        def toolInstallations = exportToolInstallations(context)

        //
        // Export the node configurations
        // ==============================
        //
        def nodes = [:]

        // The master node
        nodes["master"] = exportNodeConfiguration(context, context.getJenkins().getNodeProperties())

        // Export all other nodes
        for (node in context.getJenkins().getNodes())
        {
            // Try to export the plugins configuration
            nodes[node.getNodeName()] = exportNodeConfiguration(context, node.getNodeProperties())
        }

        // :TODO: Check where it ends
        LOGGER.warning("WARNING!!!!!!!!!!")
        def rc = [
                'core'             : core,
                'plugins'          : plugins,
                'global_nodes'     : global_node,
                'nodes'            : nodes,
                'toolInstallations': toolInstallations
        ]

        // Now send the configuration back to the caller
        stdout.println(JsonOutput.prettyPrint(JsonOutput.toJson(rc)))
        return 0
    }

    def exportToolInstallations(Context context)
    {
        def rc = [:]
        def extensions = context.getJenkins().getExtensionList(ToolInstallationConfigurationStream)

        context.getJenkins().getDescriptorList(ToolInstallation).each() { ToolDescriptor toolInstallation ->

            // Look for an extension that knows how to export the plugin
            def stream = extensions.find {
                toolInstallation.getClass().getName() == it.getToolInstallationClass()
            }
            // Determine that success
            def state
            def configuration
            if (stream == null)
            {
                stderr.println("Unsupported tool installation configuration ${toolInstallation.getClass().getName()} found!")
                state = RETURN_CODES.UNSUPPORTED
                configuration = null
            }
            else
            {
                state = RETURN_CODES.SUCCESS
                configuration = stream.doExport(context, toolInstallation)
            }
            // Add the plugin to the export
            rc["${toolInstallation.getClass().getName()}"] = [
                    "status"       : state,
                    "configuration": configuration,
            ]
        }
        return rc
    }

    Map exportToolProperties(Context context, ToolProperty toolProperty)
    {
        def extensions = context.getJenkins().getExtensionList(ToolPropertyConfigurationStream)

        // Look for an extension that knows how to export the plugin
        def stream = extensions.find { ToolPropertyConfigurationStream it ->
            toolProperty.getClass().getName() == it.getToolPropertyClass()
        }

        // Determine that success
        def state
        def configuration
        if (stream == null)
        {
            stderr.println("Unsupported tool property ${toolProperty.getClass().getName()} found!")
            state = RETURN_CODES.UNSUPPORTED
            configuration = null
        }
        else
        {
            state = RETURN_CODES.SUCCESS
            configuration = stream.doExport(context, toolProperty)
        }
        // Add the plugin to the export
        return [
                className    : toolProperty.getClass().getName(),
                status       : state,
                configuration: configuration
        ]
    }

    Map exportToolInstaller(Context context, ToolInstaller toolInstaller)
    {
        def extensions = context.getJenkins().getExtensionList(ToolInstallerConfigurationStream)

        // Look for an extension that knows how to export the plugin
        def stream = extensions.find { ToolInstallerConfigurationStream it ->
            toolInstaller.getClass().getName() == it.getToolInstallerClass()
        }

        // Determine that success
        def state
        def configuration
        if (stream == null)
        {
            stderr.println("Unsupported tool installer ${toolInstaller.getClass().getName()} found!")
            state = RETURN_CODES.UNSUPPORTED
            configuration = null
        }
        else
        {
            state = RETURN_CODES.SUCCESS
            configuration = stream.doExport(context, toolInstaller)
        }
        // Add the plugin to the export
        return [
                className    : toolInstaller.getClass().getName(),
                status       : state,
                configuration: configuration
        ]
    }

}
