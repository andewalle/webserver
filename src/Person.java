public class Person implements DatabaseObject{

    private String personalNumber;
    private String firstName;
    private String lastName;

    public Person(String personalNumber, String firstName, String lastName)
    {
        this.personalNumber = personalNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return ("PersonalNumber: " + personalNumber + "\n     firstName: " + firstName +
                "\n     lastName: " + lastName + "\n");

    }
}
