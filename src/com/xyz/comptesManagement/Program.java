package com.xyz.comptesManagement;

import java.time.LocalDate;

public class Program {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*Scenario
		 * 
		 * 
		 */
		
		Client client = new Client(11,"Ali","Baba");
		Compte compte = new Compte(11,"4567123",client);
		client.setCompte(compte);
		Operation op1 = new Operation(new LocalDate(2020,03,05)), "VERS", 4000, compte);
		compte.addOperation(op1);
		
		Operation op2 = new Operation(new LocalDate(2020,03,07), "VERS", 5000, compte);
		compte.addOperation(op2);
		
		compte.addOperation(new Operation(new LocalDate(2020,03,25), "RETR", 2000, compte));
		
		compte.addOperation(new Operation(new LocalDate(2020,03,30), "RETR", 3000, compte));
		
		compte.addOperation(new Operation(new LocalDate(2020,04,05,), "RETR", 6000, compte));
		
System.out.println("Succes!");