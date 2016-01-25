package grails.query.sample

import grails.gorm.DetachedCriteria

class Person2 {

    String firstName
    String lastName

    @Override
    String toString() {
        return "$firstName $lastName"
    }

    static class NamedWhere {
        static namedBart() {
            def callable = { firstName == 'Bart' } as DetachedCriteria<Person2>
            return callable
        }

        static namedAs(String lastName) {
            def callable = { eq 'lastName', lastName } as DetachedCriteria<Person2>
            return callable
        }
    }
}
