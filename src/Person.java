public class Person implements DatabaseObject{

    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
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


    //TODO tostring eller inte?? 
    @Override
    public String toString() {
        return String.format("firstName: " + firstName + "\nlastName: " + lastName);

    }
}
