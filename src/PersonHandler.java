import java.util.HashMap;

public class PersonHandler {

    private Database database;
    private HashMap<String, String> hM;

    public PersonHandler(Database database, HashMap<String, String> hM)
    {
        this.database = database;
        this.hM = hM;
    }

    public void createPerson()
    {
        String personalNumber;

        DatabaseObjectsFactory factory = new DatabaseObjectsFactory();

        if(!hM.containsKey("personalNumber")) {

            while (true) {

                personalNumber = generatePersonalNr();

                if (!database.getPersons().containsKey(personalNumber)) {
                    break;
                }
            }
        }
        else
        {
            personalNumber = hM.get("personalNumber");
        }

        //Creating an object from the HashMap parameters
        DatabaseObject databaseObject = factory.createDatabaseObject("person", personalNumber, hM.get("firstName"), hM.get("lastName"));
        database.addPerson((Person)databaseObject);
        database.listPersons();
    }

    private String generatePersonalNr(){

        String birthYear;
        String birthMonth;
        String birthday;
        String lastFour;

        birthYear = getRandomNr(1, 99);

        birthMonth = getRandomNr(1, 12);

        birthday = getRandomNr(1, 31);

        lastFour = getRandomNr(1111, 9999);

        return birthYear + birthMonth + birthday + lastFour;

    }

    private String getRandomNr(int min, int max){

        String number;

        int random = (int)(Math.random() * ((max - min) + 1) + min);

        if(random < 10)
        {
            number = "0" + random;
        }
        else
        {
            number = Integer.toString(random);
        }

        return number;
    }
}
