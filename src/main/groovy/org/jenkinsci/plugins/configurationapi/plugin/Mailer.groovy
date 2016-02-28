package org.jenkinsci.plugins.configurationapi.plugin

import hudson.Extension
import hudson.PluginWrapper
import hudson.tasks.Mailer.DescriptorImpl
import org.jenkinsci.plugins.configurationapi.ConfigurationExport
import org.jenkinsci.plugins.configurationapi.ConfigurationImport
import org.jenkinsci.plugins.configurationapi.PluginConfigurationStream

@Extension
class Mailer implements PluginConfigurationStream
{

    @Override
    String getPluginId()
    {
        return "mailer"
    }

    @Override
    Map doExport(ConfigurationExport.Context context, PluginWrapper plugin)
    {
        def rc = [:]
        DescriptorImpl htm = (DescriptorImpl) context.getJenkins().getDescriptor('hudson.tasks.Mailer')
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
    void doImport(ConfigurationImport.Context context, Map configuration)
    {
        DescriptorImpl htm = (DescriptorImpl) context.getJenkins().getDescriptor('hudson.tasks.Mailer')
        htm.setSmtpHost((String)configuration['smtpHost'])
        htm.setSmtpPort((String)configuration['smtpPort'])
        htm.setSmtpAuth((String)configuration['smtpAuthUsername'], (String)configuration['smtpAuthPassword'])
        htm.setUseSsl((boolean)configuration['smtpUseSSL'])
        htm.setReplyToAddress((String)configuration['replyToAddress'])
        htm.setCharset((String)configuration['charset'])
        htm.setDefaultSuffix((String)configuration['defaultSuffix'])
        htm.save()
    }

}

