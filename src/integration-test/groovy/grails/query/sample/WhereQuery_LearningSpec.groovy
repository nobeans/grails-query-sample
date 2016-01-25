package grails.query.sample

import grails.gorm.DetachedCriteria
import grails.test.mixin.integration.Integration
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

@Integration
@Transactional
class WhereQuery_LearningSpec extends Specification {

    void setup() {
        [["Fred", "Simpson"], ["Bart", "Simpson"], ["Bart", "Bloggs"]].each { firstName, lastName ->
            new Person(firstName: firstName, lastName: lastName).save(failOnError: true, flush: true)
        }
    }

    def "where() and list(): it's available where-style criteria"() {
        when:
        def persons = Person.where { lastName == 'Simpson' }.list()

        then:
        persons*.toString() == ['Fred Simpson', 'Bart Simpson']

        when: 'with sorting'
        persons = Person.where { lastName == 'Simpson' }.list(sort: 'firstName', order: 'asc')

        then:
        persons*.toString() == ['Bart Simpson', 'Fred Simpson']
    }

    def "findAll(): it can be used instead of where() and list()"() {
        when:
        def persons = Person.findAll { firstName == 'Bart' }

        then:
        persons*.toString() == ['Bart Simpson', 'Bart Bloggs']

        when: 'with sorting'
        persons = Person.findAll(sort: 'lastName', order: 'asc') { firstName == 'Bart' }

        then:
        persons*.toString() == ['Bart Bloggs', 'Bart Simpson']
    }

    def "where() and get(): it's available where-style criteria"() {
        when:
        def person = Person.where { lastName == 'Simpson' }.get()

        then:
        person.toString() == 'Fred Simpson'

        when: 'with sorting'
        person = Person.where { lastName == 'Simpson' }.get(sort: 'firstName', order: 'asc')

        then:
        person.toString() == 'Bart Simpson'
    }

    def "find(): it can be used instead of where() and get()"() {
        when:
        def person = Person.find { lastName == 'Simpson' }

        then:
        person.toString() == 'Fred Simpson'
    }

    def "where() and Criteria: an additional Criteria can be used for DetachedCriteria"() {
        when:
        def persons = Person.where { firstName == 'Bart' }.list { eq 'lastName', 'Simpson' }

        then:
        persons*.toString() == ['Bart Simpson']

        when: 'Criteria is also available'
        persons = Person.where { firstName == 'Bart' }.where { eq 'lastName', 'Simpson' }.list()

        then:
        persons*.toString() == ['Bart Simpson']

        when: 'build() is also available (where() for DetachCriteria is an alias of build()'
        persons = Person.where { firstName == 'Bart' }.build { eq 'lastName', 'Simpson' }.list()
        //persons = Person.build { firstName == 'Bart' }.build { eq 'lastName', 'Simpson' }.list() // build() can't be called for a domain class directly.

        then:
        persons*.toString() == ['Bart Simpson']
    }

    def "where(): it's supported composition by multiple 'where' conditions"() {
        when:
        def persons = Person.where { firstName == 'Bart' }.where { lastName == 'Simpson' }.list()

        then:
        persons*.toString() == ['Bart Simpson']
    }

    def "where(): it's supported composition by pre-declared Closures"() {
        given:
        def personNamedBart = { firstName == 'Bart' } as DetachedCriteria<Person>
        def personNamedSimpson = { lastName == 'Simpson' } as DetachedCriteria<Person>
        //DetachedCriteria<Person> personNamedSimpson = { lastName == 'Simpson' } // This doesn't work. You must use 'as'.

        when:
        def persons = Person.where(personNamedBart).where(personNamedSimpson).list()

        then:
        persons*.toString() == ['Bart Simpson']
    }

    def "where(): it's supported composition by pre-declared Closure and Criteria"() {
        given:
        def personNamedBart = { firstName == 'Bart' } as DetachedCriteria<Person>
        def personNamedSimpson = { eq 'lastName', 'Simpson' } as DetachedCriteria<Person>

        when:
        def persons = Person.where(personNamedBart).where(personNamedSimpson).list()

        then:
        persons*.toString() == ['Bart Simpson']
    }

    def "build(): it can be used for DetachedCriteria when not directly for a domain class"() {
        given:
        def personNamedBart = { firstName == 'Bart' } as DetachedCriteria<Person>
        def personNamedSimpson = { eq 'lastName', 'Simpson' } as DetachedCriteria<Person>

        when:
        def persons = Person.where(personNamedBart).build(personNamedSimpson).list()

        then:
        persons*.toString() == ['Bart Simpson']

        when:
        persons = Person.where(personNamedSimpson).build(personNamedBart).list()

        then:
        persons*.toString() == ['Bart Simpson']
    }

    private static personNamedBart(String firstName_) {
        // AST transformation would apply to 'as DetachedCriteria<xxx>' pattern.
        // A direct return isn't supported. You have to bind it to a variable, and then return it.
        def callable = { firstName == firstName_ } as DetachedCriteria<Person>
        return callable
    }

    def "where(): pre-declared Closure and Criteria can use parameters"() {
        given:
        def personNamedSimpson = { String lastName_ ->
            def callable = { eq 'lastName', lastName_ } as DetachedCriteria<Person>
            return callable
        }

        when:
        def persons = Person.where(personNamedBart('Bart')).build(personNamedSimpson('Simpson')).list()

        then:
        persons*.toString() == ['Bart Simpson']
    }

    def "where() and build(): it returns DetachedQuery"() {
        expect:
        Person.where { firstName == 'Bart' } instanceof DetachedCriteria<Person>

        and:
        Person.where { eq 'firstName', 'Bart' } instanceof DetachedCriteria<Person>

        and:
        Person.where { firstName == 'Bart' }.where { lastName == 'Simpson' } instanceof DetachedCriteria<Person>

        and:
        Person.where { firstName == 'Bart' }.where { eq 'firstName', 'Bart' } instanceof DetachedCriteria<Person>

        and:
        Person.where { firstName == 'Bart' }.build { lastName == 'Simpson' } instanceof DetachedCriteria<Person>

        and:
        Person.where { firstName == 'Bart' }.build { eq 'firstName', 'Bart' } instanceof DetachedCriteria<Person>
    }

    def "AST transformation to the same line would be conflicted"() {
        expect: 'when there are where query and power-assert where query is just ignored.'
        Person.where { lastName == 'Simpson' }.list()*.toString() == ['Fred Simpson', 'Bart Simpson', 'Bart Bloggs'] // including Bloggs!

        when: 'When without power-assert expression...'
        def persons = Person.where { lastName == 'Simpson' }.list()

        then:
        persons*.toString() == ['Fred Simpson', 'Bart Simpson'] // not including Bloggs!
    }

}
