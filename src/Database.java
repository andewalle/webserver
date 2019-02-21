import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {

    List<Person> persons = new CopyOnWriteArrayList<>();  //TODO CopyOnWrite???

    public void addPerson(Person person)
    {
        persons.add(person);
    }

    public void listPersons()
    {
        for (Person person : persons) {

            System.out.println(person);
        }
    }

    public void searchForPerson(String name)
    {
        boolean found = false;

        for (Person person : persons) {

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
