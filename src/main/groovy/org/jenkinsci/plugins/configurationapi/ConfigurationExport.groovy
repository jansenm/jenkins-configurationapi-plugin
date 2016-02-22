package org.jenkinsci.plugins.configurationapi

import groovy.json.JsonOutput
import hudson.Extension
import hudson.PluginWrapper
import hudson.cli.CLICommand
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.jenkinsci.plugins.configurationapi.core.ConfigurationStream as CoreConfigurationStream
import org.jenkinsci.plugins.configurationapi.node.ConfigurationStream as NodeConfigurationStream
import org.jenkinsci.plugins.configurationapi.plugin.ConfigurationStream as PluginConfigurationStream
import org.kohsuke.args4j.Argument

import java.util.logging.Logger

@Extension
public class ConfigurationExport extends CLICommand
{

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

    public enum RETURN_CODES
    {
        UNSUPPORTED('unsupported', 'support for plugin not implemented'),
        SUCCESS('success', 'plugin successfully handled')

        final String id
        final String description

        RETURN_CODES(String id, String description)
        {
            this.id = id
            this.description = description
        }
    }

    private Map exportCoreConfiguration(Jenkins jenkins)
    {
        def rc = [:]
        // Look for an extension that knows how to export the plugin
        jenkins.getExtensionList(org.jenkinsci.plugins.configurationapi.core.ConfigurationStream).each { stream ->
            rc[stream.getId()] = stream.doExport(jenkins)
        }
        return rc
    }

    private Map exportNodeConfiguration(
            Jenkins jenkins,
            DescribableList<NodeProperty<Node>, NodePropertyDescriptor> properties)
    {

        def rc = [:]
        def extensions = jenkins.getExtensionList(org.jenkinsci.plugins.configurationapi.node.ConfigurationStream)
        for (nodeProperty in properties)
        {
            // Look for an extension that knows how to export the plugin
            org.jenkinsci.plugins.configurationapi.node.ConfigurationStream stream = extensions.find {
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
                configuration = stream.doExport(jenkins, nodeProperty)
            }
            // Add the plugin to the export
            rc["property:${nodeProperty.getClass()}"] = [
                    "status"       : state,
                    "configuration": configuration
            ]
        }
        return rc
    }

    private Map exportPluginConfiguration(Jenkins jenkins, PluginWrapper plugin)
    {
        // Look for an extension that knows how to export the plugin
        PluginConfigurationStream stream = jenkins.getExtensionList(PluginConfigurationStream).find {
            plugin.getShortName() == it.getPluginId()
        }
        if (stream == null)
        {
            stderr.println("Unsupported plugin ${plugin.getShortName()} found!")
            return null
        }
        return stream.doExport(jenkins, plugin)
    }

    @Override
    protected int run() throws Exception
    {
        final Jenkins jenkins = Jenkins.getInstance()

        if (jenkins == null)
        {
            stderr.println("The Jenkins instance has not been started, or was already shut down!");
            return -1;
        }

        //
        // Export the jenkins core configuration
        // =====================================
        //
        def core = [:]
        for (coreConfig in jenkins.getExtensionList(org.jenkinsci.plugins.configurationapi.core.ConfigurationStream))
        {
            core[coreConfig.getId()] = exportCoreConfiguration(jenkins)
        }

        //
        // Export the global plugin configurations
        // =======================================
        //
        def plugins = [:]
        for (plugin in jenkins.getPluginManager().getPlugins())
        {
            // Try to export the plugins configuration
            def configuration = exportPluginConfiguration(jenkins, plugin)
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
                    "version"      : plugin.getVersion(),
                    "status"       : state,
                    "configuration": configuration
            ]
        }


        //
        // Export the global node configuration
        // ====================================
        //
        def global_node = exportNodeConfiguration(jenkins, jenkins.getGlobalNodeProperties())

        //
        // Export the node configurations
        // ==============================
        //
        def nodes = [:]

        // The master node
        nodes["master"] = exportNodeConfiguration(jenkins, jenkins.getNodeProperties())

        // Export all other nodes
        for (node in jenkins.getNodes())
        {
            // Try to export the plugins configuration
            nodes[node.getNodeName()] = exportNodeConfiguration(jenkins, node.getNodeProperties())
        }

        // :TODO: Check where it ends
        LOGGER.warning("WARNING!!!!!!!!!!")
        def rc = [
                'core': core,
                'plugins': plugins,
                'global_nodes': global_node,
                'nodes': nodes
        ]

        // Now send the configuration back to the caller
        stdout.println(JsonOutput.prettyPrint(JsonOutput.toJson(rc)))
        return 0
    }

}
