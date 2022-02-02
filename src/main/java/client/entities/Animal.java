package client.entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

@Entity("Animal")
public class Animal {
    public Animal() {
    }

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Animal(Long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Id
    private Long id;

//    @ManyToOne("zoo_id")
//    Zoo zoo;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(value = "Fullname")
    private String name;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Column(value = "Age")
    private int age;

    @Override
    public String toString(){
        return "Id = " + getId().toString()
                + "; FullName = " + getName()
                + "; Age = " + getAge() + ".";
    }
}
