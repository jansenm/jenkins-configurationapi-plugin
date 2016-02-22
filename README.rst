********************************
Jenkins Configuration API Plugin
********************************

.. contents:: Table of Contents

A jenkins plugin that provides jenkins cli commands to export and import the jenkins configuration.

State
=====

At this time consider the plugin pre-alpha quality. Its only meant as a proof of concept to show this is a feasible
concept and to request feedback from the jenkins community.

.. note:: THIS IS ALPHA QUALITY

Concept
=======

The plugin exports the configuration of a jenkins instance into json format. A call to export the configuration looks
like this:

.. code-block:: sh

   $ java -jar jenkins-cli.jar -s http://<JENKINS_INSTANCE>:8080 configuration-export [PASSWORD] | tee CONFIG

The configuration is printed to `stdout`. The `PASSWORD` one day will be used to encrypt all sensible data but this
is not yet implemented.

Later you can reimport this configuration into a jenkins instance like this:

.. code-block:: sh

   $ java -jar jenkins-cli.jar -s http://<JENKINS_INSTANCE>:8080 configuration-import CONFIG [PASSWORD]

The plugin distuinguishes between the following kind of jenkins configuration:

:Core Configuration:
   Global jenkins configuration. Anything available without any active plugin.

:Plugin Configuration:
   Global Configuration that belongs to a plugin.

:Node Configuration:
   Node Configuration. This can be either core or plugin related.

For each kind of configuration `ExtensionPoints`_ are defined.

- `core.ConfigurationStream`_,
- `plugin.ConfigurationStream`_,
- `node.ConfigurationStream`_

For each extension point an example is implemented.

Open Issues
===========

Encrypt Passwords
-----------------
The password needs to be forwarded to the Extension points so they can encrypt sensible data. Unless a better design
is found.

Install Plugins
---------------
The initial idea was to install the plugins in the exact same configuration as specified in the configuration on
import. Jenkins unfortunately lacks a method to install any other plugin but the latest plugin automatically. I need
to find out how to work around this. The `InstallPluginCommand` from jenkins core should provide a solution.

On a initial run plugins should be

- removed
- installed
- downgraded
- upgraded

to make sure the configuration is exactly the same on import as it was on export. Then it should cancel the import if
needed and inform the user a restart is required to continue (on upgrade/downgrade/remove).

Alternatively we need to find a way to provide upgrade/downgrade path on the import step. As in plugin version 23
knows how to handle configuration export from plugin version 20. Unfortunately for the other way thats impossible.
Thats why i would like to go the strict same configuration way.

Move Plugin Related Code Into Plugins
-------------------------------------
After the design has been hammered out the best solution is to move the plugin related ConfigurationStream into the
respective plugins. But for this to happen the design needs to be really stabled.

.. _ExtensionPoints: http://javadoc.jenkins-ci.org/hudson/ExtensionPoint.html
.. _core.ConfigurationStream: src/main/groovy/org/jenkinsci/plugins/configurationapi/core/ConfigurationStream.groovy
.. _plugin.ConfigurationStream: src/main/groovy/org/jenkinsci/plugins/configurationapi/core/ConfigurationStream.groovy
.. _node.ConfigurationStream: src/main/groovy/org/jenkinsci/plugins/configurationapi/core/ConfigurationStream.groovy




