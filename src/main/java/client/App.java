package client;


import client.entities.Animal;
import client.entities.Person;
import client.entities.Zoo;
import manager.OrmManager;

import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class App {
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) throws IOException {

        Handler fileHandler = new FileHandler("src/main/java/logfile.log");
        SimpleFormatter formatter = new SimpleFormatter();
        logger.addHandler(fileHandler);
        fileHandler.setFormatter(formatter);
        logger.setUseParentHandlers(false);

        logger.info("[Status] Start of the program.");

        //my tests (Anton)
        OrmManager orm = OrmManager.get("H2.db");

        orm.prepareRepositoryFor(Animal.class);
        orm.prepareRepositoryFor(Zoo.class);
        orm.prepareRepositoryFor(Person.class);

        Animal animal = new Animal("alex", 23);
        Animal animal1 = new Animal("alexey", 26);
        Animal animal2 = new Animal("dimon", 5);
        Animal animal3 = new Animal("valera", 33);

        //insert
        orm.save(animal);
        orm.save(animal1);
        orm.save(animal2);
        orm.save(animal3);

        List<Animal> list = orm.getAll(Animal.class);// returns list of objects of the specified class
        for (var i : list) {
            System.out.println(i);
        }

        animal2.setAge(animal2.getAge() + 5);
        int res = orm.update(animal2);// return updates amount
        System.out.println(res + " updates");//1
        System.out.println(animal2.getName() + " is " + animal2.getAge() + " years old");//10

        //delete
        orm.delete(Animal.class, "AGE>30");
        orm.delete(Animal.class, "FULL_AGE>2");

        list = orm.getAll(Animal.class);// returns list of objects of the specified class
        for (var i : list) {
            System.out.println(i);
        }
        logger.info("[Status] End of the program.");
    }
}
