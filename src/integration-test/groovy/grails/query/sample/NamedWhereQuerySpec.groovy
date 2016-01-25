package grails.query.sample

import grails.test.mixin.integration.Integration
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static grails.query.sample.Person2.NamedWhere.*

@Integration
@Transactional
class NamedWhereQuerySpec extends Specification {

    void setup() {
        [["Fred", "Simpson"], ["Bart", "Simpson"], ["Bart", "Bloggs"]].each { firstName, lastName ->
            new Person2(firstName: firstName, lastName: lastName).save(failOnError: true, flush: true)
        }
    }

    def "trial of named detached queries"() {
        when:
        def persons = Person2.where(namedBart()).where(namedAs('Simpson')).list()

        then:
        persons*.toString() == ['Bart Simpson']
    }
}
