package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import jenkins.model.Jenkins

@Extension
class MailerStream extends ConfigurationStream
{

    @Override
    String getPluginId()
    {
        return "mailer"
    }

    @Override
    Map doExport(Jenkins instance, PluginWrapper plugin)
    {
        def rc = [:]
        hudson.tasks.Mailer.DescriptorImpl htm =
                instance.getDescriptor('hudson.tasks.MailerStream')
        if (htm == null) {
            return rc
        }
        rc['smtpHost'] = htm.getSmtpServer()
        rc['smtpPort'] = htm.getSmtpPort()
        rc['smtpAuthPassword'] = htm.getSmtpAuthPassword()
        rc['smtpAuthUsername'] = htm.getSmtpAuthUserName()
        rc['smtpUseSSL'] = htm.getUseSsl()
        rc['url'] = htm.getUrl()
        rc['adminAddress'] = htm.getAdminAddress()
        rc['replyToAddress'] = htm.getReplyToAddress()
        rc['charset'] = htm.getCharset()
        return rc
    }

    @Override
    void doImport(Jenkins instance, Map configuration)
    {
    }

}

