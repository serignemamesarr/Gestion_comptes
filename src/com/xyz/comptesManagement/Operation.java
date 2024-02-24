package com.xyz.comptesManagement;

import java.time.LocalDate;

public class Operation {
private LocalDate date;
private String type;
private double montant;
private Compte compte;

public Operation() {
  }

public Operation(LocalDate date, String type, double montant, Compte compte) {
	this.date = date;
	this.type = type;
	this.montant = montant;
	this.compte = compte;
}



}
