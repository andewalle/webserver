import java.util.HashMap;

public class Database {

    private HashMap<String, Person> persons = new HashMap<>();

    public HashMap<String, Person> getPersons()
    {
        return persons;
    }

    public void addPerson(Person person)
    {
        String personalNumber = person.getPersonalNumber();

        persons.put(personalNumber, person);
    }

    public void listPersons()
    {
        for (Person person : persons.values()) {

            System.out.println(person);
        }
    }

    public void searchForPerson(String name)
    {
        boolean found = false;

        for (Person person : persons.values()) {

            if(person.getFirstName().toUpperCase().equals(name.toUpperCase()))
            {
                System.out.println(person);
                found = true;
            }
            else if(person.getLastName().toUpperCase().equals(name.toUpperCase()))
            {
                System.out.println(person);
                found = true;
            }
        }

        if(!found)
        {
            System.out.println("Person not found");
        }
    }
}
