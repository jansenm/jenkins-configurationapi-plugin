package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import hudson.tasks.Mailer
import jenkins.model.Jenkins
import org.jenkinsci.plugins.configurationapi.PluginConfigurationStream

@Extension
class MailerStreamPlugin implements PluginConfigurationStream
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
        Mailer.DescriptorImpl htm = (Mailer.DescriptorImpl) instance.getDescriptor('hudson.tasks.Mailer')
        if (htm == null) {
            return rc
        }
        rc['smtpHost'] = htm.getSmtpServer()
        rc['smtpPort'] = htm.getSmtpPort()
        rc['smtpAuthPassword'] = htm.getSmtpAuthPassword()
        rc['smtpAuthUsername'] = htm.getSmtpAuthUserName()
        rc['smtpUseSSL'] = htm.getUseSsl()
        rc['replyToAddress'] = htm.getReplyToAddress()
        rc['charset'] = htm.getCharset()
        rc['defaultSuffix'] = htm.getDefaultSuffix()
        return rc
    }

    @Override
    void doImport(Jenkins instance, Map configuration)
    {
        println("here")
        Mailer.DescriptorImpl htm = (Mailer.DescriptorImpl) instance.getDescriptor('hudson.tasks.Mailer')
        assert htm
        htm.setSmtpHost((String)configuration['smtpHost'])
        htm.setSmtpPort(((Integer)configuration['smtpPort']).toString())
        htm.setSmtpAuth((String)configuration['smtpAuthUsername'], (String)configuration['smtpAuthPassword'])
        htm.setUseSsl((boolean)configuration['smtpUseSSL'])
        htm.setReplyToAddress((String)configuration['replyToAddress'])
        htm.setCharset((String)configuration['charset'])
        htm.setDefaultSuffix((String)configuration['defaultSuffix'])
        htm.save()
    }

}

