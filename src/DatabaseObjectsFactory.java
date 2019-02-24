public class DatabaseObjectsFactory {

    public DatabaseObject createDatabaseObject(String objectType, String personalNumber, String firstName, String lastName)
    {
        if(objectType.equalsIgnoreCase("person"))
        {
            return new Person(personalNumber, firstName, lastName);
        }
        else
        {
            return null;
        }
    }


}
