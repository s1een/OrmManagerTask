package client;


import client.entities.Animal;
import client.entities.Person;
import client.entities.Zoo;
import manager.OrmManager;

public class App {
    public static void main(String[] args) {
        OrmManager orm = OrmManager.get("postgre.db");
        orm.prepareRepositoryFor(Animal.class);
        orm.prepareRepositoryFor(Zoo.class);
        orm.prepareRepositoryFor(Person.class);
    }
}
