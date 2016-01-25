package grails.query.sample

class Person {

    String firstName
    String lastName

    @Override
    String toString() {
        return "$firstName $lastName"
    }
}
