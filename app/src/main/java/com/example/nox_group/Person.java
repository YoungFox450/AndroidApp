package com.example.nox_group;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "persons")
public class Person implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nom;
    public String postNom;
    public String prenom;
    public String sexe;
    public String classe;
    public String description;
    public String imagePath; // Ajout du chemin de l'image

    public Person(String nom, String postNom, String prenom, String sexe, String classe, String description, String imagePath) {
        this.nom = nom;
        this.postNom = postNom;
        this.prenom = prenom;
        this.sexe = sexe;
        this.classe = classe;
        this.description = description;
        this.imagePath = imagePath;
    }
}
