import grails.util.BuildSettings
import grails.util.Environment

import java.nio.charset.Charset

// See http://logback.qos.ch/manual/groovy.html for details on configuration
def fileLogPattern = '%date{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] \\(%logger\\) %mdc{jobid}%message%n'
def consoleLogPattern = '%d{HH:mm:ss.SSS} %highlight(%-5level) [%thread] %cyan(\\(%logger{39}\\)) %mdc{jobid}%message%n'
def logDir = new File(BuildSettings.TARGET_DIR, "logs")

//
// Appenders
//

appender('STDOUT', ConsoleAppender) {
    withJansi = true
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern = consoleLogPattern
    }
}

appender('FILE', RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern = fileLogPattern
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${logDir}/my.%d{yyyy-MM-dd}.log"
        maxHistory = 90
    }
}

appender('FULL_STACKTRACE', RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern = fileLogPattern
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${logDir}/my-stacktrace.%d{yyyy-MM-dd}.log"
    }
}

//
// Helpers
//

def error = { logger(it, ERROR, ['STDOUT', 'FILE'], false) }
def warn =  { logger(it, WARN,  ['STDOUT', 'FILE'], false) }
def info =  { logger(it, INFO,  ['STDOUT', 'FILE'], false) }
def debug = { logger(it, DEBUG, ['STDOUT', 'FILE'], false) }
def trace = { logger(it, TRACE, ['STDOUT', 'FILE'], false) }

//
// Configurations
//

root INFO, ['STDOUT', 'FILE']

logger "StackTrace", ERROR, ['FULL_STACKTRACE'], false

warn 'org.grails'
info 'grails.app'
warn 'grails.plugin'
warn 'grails.plugins'
warn 'org.springframework'
warn 'org.hibernate'
error 'net.sf.ehcache.hibernate'
error 'org.hibernate.hql.internal.ast.HqlSqlWalker'
error 'net.sf.ehcache.config.ConfigurationFactory'
warn 'org.apache.coyote'
warn 'org.apache.tomcat'
warn 'org.apache.catalina'
error 'org.apache.sshd'
warn 'org.quartz'

Environment.executeForCurrentEnvironment {
    development {
        debug 'grails.query.sample'
        trace 'org.hibernate.type.descriptor.sql.BasicBinder'
        trace 'org.hibernate.type.EnumType'
        debug 'org.hibernate.SQL'
        debug 'groovy.sql.Sql'
    }
    test {
        debug 'grails.query.sample'
        trace 'org.hibernate.type.descriptor.sql.BasicBinder'
        trace 'org.hibernate.type.EnumType'
        debug 'org.hibernate.SQL'
        debug 'groovy.sql.Sql'
    }
}
