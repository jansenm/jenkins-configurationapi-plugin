package org.jenkinsci.plugins.configurationapi

import groovy.json.JsonSlurper
import hudson.Extension
import hudson.PluginManager
import hudson.PluginWrapper
import hudson.cli.CLICommand
import hudson.cli.util.ScriptLoader
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import hudson.util.VersionNumber
import jenkins.model.Jenkins
import org.apache.commons.io.IOUtils
import org.jenkinsci.plugins.configurationapi.core.ConfigurationStream as CoreConfigurationStream
import org.jenkinsci.plugins.configurationapi.node.ConfigurationStream as NodeConfigurationStream
import org.jenkinsci.plugins.configurationapi.plugin.ConfigurationStream as PluginConfigurationStream
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException

import java.util.logging.Logger

@Extension
public class ConfigurationImport extends CLICommand
{
    @Argument(
            usage = "Script to be executed. File, URL or '=' to represent stdin.",
            metaVar = "SCRIPT",
            index = 0)
    public String script;

    @Argument(
            usage = "password for encryption of credentials",
            metaVar = "PASSWORD",
            required = false,
            multiValued = false,
            index = 1)
    private String password = null

    private String loadConfiguration() throws CmdLineException, IOException, InterruptedException
    {
        if (script == null)
            throw new CmdLineException(null, "No script is specified");
        if (script.equals("="))
            return IOUtils.toString(stdin);

        return checkChannel().call(new ScriptLoader(script));
    }


    private static final Logger LOGGER = Logger.getLogger(ConfigurationImport.class.getName())

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

    private void importCoreConfiguration(Jenkins jenkins, Map config)
    {
        def coreExtensions = jenkins.getExtensionList(CoreConfigurationStream)
        for (entry in config)
        {
            CoreConfigurationStream stream = coreExtensions.find { entry.key == it.getId() }
            stream.doImport(jenkins, entry.value)
        }
    }

    private Map importNodeConfiguration(
            Jenkins jenkins,
            DescribableList<NodeProperty<Node>, NodePropertyDescriptor> properties)
    {

        def rc = [:]
        def extensions = jenkins.getExtensionList(NodeConfigurationStream)
        for (nodeProperty in properties)
        {
            // Look for an extension that knows how to import the plugin
            NodeConfigurationStream stream = extensions.find {
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
            // Add the plugin to the import
            rc["property:${nodeProperty.getClass()}"] = [
                    "status"       : state,
                    "configuration": configuration
            ]
        }
        return rc
    }

    private int setupPlugins(Jenkins jenkins, Map config)
    {
        // Initially i though i could download and install the exact same set of plugins as given in the
        // configuration. This apparently is not THAT easy given jenkins by default does not have a method to install
        // a plugin in a given (older) version. It has to be done manually.
        //
        // So i need to find out a way to do it myself.
        // A)
        //      - download the jpi/hpi plugin manually (works only for jenkins-ci.com plugins afaics [EASY]
        //      - install that plugin [NO IDEA YET]
        // B)
        //      For me the solution is simple. I use my ansible playbook which can install a set of plugins in a
        //      fixed version. So for now just verify the plugins and give feedback
        //
        def pluginManager = jenkins.getPluginManager()
        def final IDENTICAL = 1
        def final VERSION_MISMATCH = 2
        def final PLUGINS_MISSING = 3

        def rc = IDENTICAL
        config.each { pluginId, pluginConfig ->

            // Check if the plugin is installed
            def currentInstallation = pluginManager.getPlugin(pluginId)
            if (currentInstallation)
            {
                // The plugin is installed. Check if its the correct version
                if (currentInstallation.getVersion() == pluginConfig['version'])
                {
                    stderr.println("Plugin ${pluginId} is installed with the correct version ${pluginConfig.version}.")
                }
                else
                {
                    stderr.println("Plugin ${pluginId} is installed with the wrong version\
                                    ${currentInstallation.getVersion()} (configured ${pluginConfig.version})")
                    rc = [VERSION_MISMATCH, rc].max()
                }

                if (pluginConfig.get('enabled', true))
                {
                    if (!currentInstallation.isEnabled())
                    {
                        currentInstallation.enable()
                    }
                }
                else if (currentInstallation.isEnabled())
                {
                    currentInstallation.disable()
                }
            }
            else
            {
                stderr.println("Plugin ${pluginId} is NOT INSTALLED.")
                rc = [PLUGINS_MISSING, rc].max()
            }

        }

        // Give back the status
        return rc
    }

    private Map importPluginConfiguration(Jenkins jenkins, PluginWrapper plugin)
    {
        // Look for an extension that knows how to import the plugin
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
        // Load the configuration
        //
        def json = new JsonSlurper()
        def config = (Map) json.parseText(loadConfiguration())

        //
        // Import the jenkins core configuration
        // =====================================
        //
        importCoreConfiguration(jenkins, config['core'])

        //
        // Setup the plugins
        // =================
        //
        def rc = setupPlugins(jenkins, config['plugins'])
        switch (rc)
        {
            case 3:
                stderr.println('error: the plugins are not configured correctly (check above). can not continue!')
                return -1
            case 2:
                stderr.println('warn: the plugins are not configured identically (check above). will continue but ' +
                        'expect errors!')
                break
            default:
                break
        }

        //
        // Import the global plugin configurations
        // =======================================
        //

        //
        // Import the global node configuration
        // ====================================
        //

        //
        // Import the node configurations
        // ==============================
        //

        // We are finished
        return 0
    }

}
