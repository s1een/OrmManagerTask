package client.entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

@Entity("Zoo")
public class Zoo {

    @Id("id_zoo")
    Long id;

    @Column
    String address;

//    @OneToMany(mappedBy = "zoo_id")
//    List<Animal> animals = new ArrayList<>();

    public Zoo() {
    }

    public Zoo(String address) {
        this.address = address;
    }
}