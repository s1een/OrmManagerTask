package manager;

import client.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrmManagerTest {

    OrmManager ormManager = new OrmManager("H2.db");

    @Test()
    @DisplayName("Test DataBase Creation Failed")
    void getFailedDataBaseCreation() {
        assertThrows(RuntimeException.class,()-> new OrmManager("gdsasd"));
    }

    @Test()
    @DisplayName("Test DataBase Creation Successful")
    void getSuccessfulDataBaseCreation() {
        assertDoesNotThrow(()-> new OrmManager("H2.db"));
    }

    @Test
    @DisplayName("Test Repository Creation")
    void prepareRepositoryFor() {
        try {
            Connection con = ConnectionManager.open("H2.db");
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ZOO", null);
            assertTrue(tables.next());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test Repository Creation Failed Or Wasnt Completed")
    void prepareRepositoryForFailed() {
        try {
            Connection con = ConnectionManager.open("H2.db");
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "Pirson", null);
            assertFalse(tables.next());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test Update Function : false if was successfully updated")
    void update() {
        Animal animal = new Animal(1L,"Alex",20);
        var update = ormManager.update(animal);

        assertFalse(update == 0);
    }

    @Test
    @DisplayName("Test If 'UPDATE' Command Builds Right")
    void updateCommandBuilder() {
        Zoo zoo = new Zoo("vul.Ceo");

        var result = ormManager.updateCommandBuilder(zoo);

        var expectedResult = "UPDATE ZOO SET ADDRESS='vul.Ceo' WHERE ID_ZOO = null;";
        assertEquals(result,expectedResult);

    }

    @Test
    @DisplayName("Test Get All Function")
    void getAll() {
        var result = ormManager.getAll(Animal.class);

        var resultObject = new Animal(1L,"alex",23);

        assertEquals(result.get(0).toString(),resultObject.toString());
    }

    @Test
    @DisplayName("Test If 'SELECT * FROM ...'Command Builds Right")
    void getAllCommandBuilder() {
        var result = ormManager.getAllCommandBuilder(Person.class);

        var expectedResult = "SELECT * FROM PERSON;";
        assertEquals(result,expectedResult);
    }

    @Test
    @DisplayName("Test Save Function")
    void save() {
        Animal animal = new Animal("alex", 23);
        Animal animal1 = new Animal("alexey", 26);
        Animal animal2 = new Animal("dimon", 5);
        Animal animal3 = new Animal("valera", 33);

        ormManager.save(animal);
        ormManager.save(animal1);
        ormManager.save(animal2);
        ormManager.save(animal3);

        var result = ormManager.getAll(Animal.class);

        List<Animal> expectedResultList = new ArrayList<Animal>();
        expectedResultList.add(animal);
        expectedResultList.add(animal1);
        expectedResultList.add(animal2);
        expectedResultList.add(animal3);


        assertEquals(result.toString(),expectedResultList.toString());
    }

    @Test
    @DisplayName("Test If 'INSERT INTO ...'Command Builds Right")
    void insertCommandBuilder() {
        Animal animal = new Animal("Tan",20);

        var result = ormManager.insertCommandBuilder(animal);

        var expectedResult = "INSERT INTO ANIMAL(FULLNAME,AGE) VALUES ('Tan',20);";
        assertEquals(result,expectedResult);
    }

    @Test
    @DisplayName("Test Get By Id Function")
    void getById() {
        var result = ormManager.getById(Person.class,2L);

        var resultObject = new Animal(2L,"crock",27);

        assertEquals(result.toString(),resultObject.toString());
    }

    @Test
    @DisplayName("Test If 'SELECT ... WHERE'Command Builds Right")
    void getByIdCommandBuilder() {
        var id = 2L;

        var result = ormManager.getByIdCommandBuilder(Animal.class,id);

        var expectedResult = "SELECT * FROM ANIMAL WHERE ID = 2;";
        assertEquals(result,expectedResult);

    }

    @Test
    @DisplayName("Test Where Function : should return false")
    void whereFalse() {
        List<String> fields = Arrays.asList("ID","NAME");
        var condition = "idd % 7";

        var res = OrmManager.where(condition,fields);

        assertFalse(res);
    }

    @Test
    @DisplayName("Test Where Function : should return true")
    void whereTrue() {

        List<String> fields = Arrays.asList("ID","NAME");
        var condition = "name=Alex";

        var res = OrmManager.where(condition,fields);

        assertTrue(res);
    }
}