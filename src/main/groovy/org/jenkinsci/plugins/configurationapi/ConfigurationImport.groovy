package org.jenkinsci.plugins.configurationapi

import groovy.json.JsonSlurper
import hudson.Extension
import hudson.cli.util.ScriptLoader
import hudson.slaves.NodeProperty
import hudson.slaves.NodePropertyDescriptor
import hudson.tools.ToolInstaller
import hudson.tools.ToolProperty
import hudson.util.DescribableList
import jenkins.model.Jenkins
import org.apache.commons.io.IOUtils
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException

import java.util.logging.Logger

@Extension
public class ConfigurationImport extends ConfigurationBase
{
    public static class Context extends ConfigurationBase.Context
    {

        public Context(Jenkins jenkins, ConfigurationImport importer)
        {
            super(jenkins)
            this.importer = importer
        }

        public ConfigurationImport getImporter()
        {
            return this.importer
        }

        private ConfigurationImport importer
    }

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

    private void importCoreConfiguration(Context context, Map config)
    {
        def coreExtensions = context.getJenkins().getExtensionList(CoreConfigurationStream)
        for (entry in config)
        {
            CoreConfigurationStream stream = coreExtensions.find { entry.key == it.getId() }
            if (stream == null)
            {
                stderr.println("Unsupported global configuration ${entry.key} found!")
                continue
            }
            stream.doImport(context, (Map) entry.value)
        }
    }

    private Map importNodeConfiguration(
            Context context,
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties,
            Map configuration)
    {
        def coreExtensions = context.getJenkins().getExtensionList(NodeConfigurationStream)
        // :TODO: Setup a node if necessary
        configuration.each { nodePropertyName, nodePropertyConfig ->
            // :TODO: error handling for no stream found
            NodeConfigurationStream stream = coreExtensions.find { nodePropertyName == it.getNodePropertyClass() }
            if (stream == null)
            {
                stderr.println("Unsupported node configuration ${nodePropertyName} found!")
                return
            }
            stream.doImport(context, nodeProperties, (Map) nodePropertyConfig.configuration)
        }
    }

    private Map importNodeConfigurations(Context context, Map configuration)
    {
        configuration.each { String nodeName, Map nodeConfig ->
            def nodeProperties
            if (nodeName == 'master')
            {
                nodeProperties = context.getJenkins().getNodeProperties()
            }
            else
            {
                nodeProperties = context.getJenkins().getNode(nodeName).getNodeProperties()
            }
            // :TODO: Setup a node if necessary
            importNodeConfiguration(context, nodeProperties, nodeConfig)
        }
    }

    private int setupPlugins(Context context, Map config)
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
        def pluginManager = context.getJenkins().getPluginManager()
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

    private Map importPluginConfigurations(Context context, Map configuration)
    {
        def extensionList = context.getJenkins().getExtensionList(PluginConfigurationStream)

        configuration.each { String pluginId, Map pluginConfig ->

            // Look for an extension that knows how to export the plugin
            def stream = extensionList.find { pluginId == it.getPluginId() }
            if (stream == null)
            {
                stderr.println("Unsupported plugin ${pluginId} found!")
                return
            }

            stream.doImport(context, (Map) pluginConfig['configuration'])
        }
    }

    private void importToolInstallations(Context context, Map configuration)
    {
        def extensionList = context.getJenkins().getExtensionList(ToolInstallationConfigurationStream)

        configuration.each { String className, Map toolInstallationConfig ->
            stderr.println(className)

            // Look for an extension that knows how to export the plugin
            def stream = extensionList.find { className == it.getToolInstallationClass() }
            if (stream == null)
            {
                stderr.println("Unsupported tool installation ${className} found!")
                return
            }

            stream.doImport(context, (Map) toolInstallationConfig.configuration)
        }
    }

    public List<? extends ToolProperty<?>> importToolProperties(Context context, List properties)
    {
        def extensionList = context.getJenkins().getExtensionList(ToolPropertyConfigurationStream)
        def toolProperties = []

        properties.each { Map toolPropertyConfig ->

            // Look for an extension that knows how to export the plugin
            def stream = extensionList.find { toolPropertyConfig.className == it.getToolPropertyClass() }
            if (stream == null)
            {
                stderr.println("Unsupported tool property ${className} found!")
                return
            }

            toolProperties.add(stream.doImport(context, (Map) toolPropertyConfig.configuration))
        }

        return toolProperties
    }

    ToolInstaller importToolInstaller(Context context, Map configuration)
    {
        def extensions = context.getJenkins().getExtensionList(ToolInstallerConfigurationStream)

        // Look for an extension that knows how to export the plugin
        def stream = extensions.find { ToolInstallerConfigurationStream it ->
            configuration.className == it.getToolInstallerClass()
        }

        if (stream == null)
        {
            stderr.println("Unsupported tool installer ${configuration.className} found!")
            return
        }

        return stream.doImport(context, (Map)configuration.configuration)
    }

    @Override
    protected int run() throws Exception
    {
        def context = new Context(Jenkins.getInstance(), this)

        if (context.getJenkins() == null)
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
        importCoreConfiguration(context, (Map) config['core'])

        //
        // Setup the plugins
        // =================
        //
        def rc = setupPlugins(context, (Map) config['plugins'])
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
        importPluginConfigurations(context, (Map) config['plugins'])

        //
        // Import the global node configuration
        // ====================================
        //
        importNodeConfiguration(context, context.getJenkins().getGlobalNodeProperties(), (Map) config['global_nodes'])

        //
        // Export tool installations
        // ====================================
        //
        importToolInstallations(context, (Map) config['toolInstallations'])

        //
        // Import the node configurations
        // ==============================
        //
        importNodeConfigurations(context, (Map) config['nodes'])

        context.getJenkins().save()

        // We are finished
        return 0
    }

}
