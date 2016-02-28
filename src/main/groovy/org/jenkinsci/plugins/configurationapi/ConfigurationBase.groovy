package org.jenkinsci.plugins.configurationapi

import hudson.cli.CLICommand
import jenkins.model.Jenkins

abstract class ConfigurationBase extends CLICommand
{

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

    public static class Context {

        public Context(Jenkins jenkins)
        {
            this.instance = jenkins
        }

        public Jenkins getJenkins() {
            return instance
        }

        private Jenkins instance;
    }

}
