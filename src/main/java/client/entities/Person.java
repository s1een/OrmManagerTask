package client.entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.time.LocalDate;

@Entity("Person")
public class Person {
    @Id("id_person")
    Long id;

    @Column(value = "firstName")
    String name;

    String lastName;

    @Column
    LocalDate dateOfBirth;

    public Person() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
