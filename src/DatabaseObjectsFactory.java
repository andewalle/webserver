public class DatabaseObjectsFactory {

    public DatabaseObject createDatabaseObject(String objectType, String firstName, String lastName)
    {
        if(objectType.equalsIgnoreCase("person"))
        {
            return new Person(firstName, lastName);
        }
        else
        {
            return null;
        }

    }
}
