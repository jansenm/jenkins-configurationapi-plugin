package org.jenkinsci.plugins.configurationapi

import groovy.json.JsonSlurper
import hudson.Extension
import hudson.PluginWrapper
import hudson.cli.CLICommand
import hudson.cli.util.ScriptLoader
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.apache.commons.io.IOUtils
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
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties,
            Map configuration)
    {
        def coreExtensions = jenkins.getExtensionList(NodeConfigurationStream)
        // :TODO: Setup a node if necessary
        configuration.each { nodePropertyName, nodePropertyConfig ->
            // :TODO: error handling for no stream found
            NodeConfigurationStream stream = coreExtensions.find { nodePropertyName == it.getNodePropertyClass() }
            stream.doImport(jenkins, nodeProperties, nodePropertyConfig.configuration)
        }
    }

    private Map importNodeConfigurations(Jenkins jenkins, Map configuration)
    {
        configuration.each { String nodeName, Map nodeConfig ->
            def nodeProperties
            if (nodeName == 'master')
            {
                nodeProperties = jenkins.getNodeProperties()
            }
            else
            {
                nodeProperties = jenkins.getNode(nodeName).getNodeProperties()
            }
            // :TODO: Setup a node if necessary
            importNodeConfiguration(jenkins, nodeProperties, nodeConfig)
        }
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
        config.each { String pluginId, Map pluginConfig ->

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

    private Map importPluginConfigurations(Jenkins jenkins, Map configuration)
    {
        def extensionList = jenkins.getExtensionList(PluginConfigurationStream)

        configuration.each { String pluginId, Map pluginConfig ->

            // Look for an extension that knows how to export the plugin
            def stream = extensionList.find { pluginId == it.getPluginId() }
            if (stream == null)
            {
                stderr.println("Unsupported plugin ${pluginId} found!")
                return
            }

            stream.doImport(jenkins, (Map)pluginConfig['configuration'])
        }
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
        importCoreConfiguration(jenkins, (Map)config['core'])

        //
        // Setup the plugins
        // =================
        //
        def rc = setupPlugins(jenkins, (Map)config['plugins'])
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
        importPluginConfigurations(jenkins, (Map)config['plugins'])

        //
        // Import the global node configuration
        // ====================================
        //
        importNodeConfiguration(jenkins, jenkins.getGlobalNodeProperties(), (Map)config['global_nodes'])

        //
        // Import the node configurations
        // ==============================
        //
        importNodeConfigurations(jenkins, (Map) config['nodes'])

        jenkins.save()

        // We are finished
        return 0
    }

}
