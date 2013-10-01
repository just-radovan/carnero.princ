package carnero.princ.data;

import java.util.HashMap;

public class Breweries {

	public static final HashMap<String, Brewery> map;

	static {
		map = new HashMap<String, Brewery>();

		map.put("Aspall", new Brewery("Aspall", "Aspall", true));
		map.put("Břevnov", new Brewery("Břevnov", "Břevnovský pivovar", true));
		map.put("Celia", new Brewery("Celia", "Žatec", false));
		map.put("Démon", new Brewery("Démon", "Vysoký Chlumec", false));
		map.put("Dudák", new Brewery("Dudák", "Strakonice", false));
		map.put("Chotěboř", new Brewery("Chotěboř", "Chotěboř", true));
		map.put("Chýně", new Brewery("Chýně", "Chýně", true));
		map.put("Ježek", new Brewery("Ježek", "Jihlava", false));
		map.put("Klostermann", new Brewery("Klostermann", "Pivovar Strakonice", false));
		map.put("Kocour", new Brewery("Kocour", "Kocour", true));
		map.put("Krakonoš", new Brewery("Krakonoš", "Krakonoš", true));
		map.put("Matuška", new Brewery("Matuška", "Matuška", true));
		map.put("Opat", new Brewery("Opat", "Broumov", false));
		map.put("Podkováň", new Brewery("Podkováň", "Podkováň", true));
		map.put("Princ", new Brewery("Princ Max", "Vysoký Chlumec", false));
		map.put("Primátor", new Brewery("Primátor", "Primátor", true));
		map.put("Rebel", new Brewery("Rebel", "Havlíčkův Brod", false));
		map.put("Rataj", new Brewery("Rataj", "Rychtář", false));
		map.put("Rohozec", new Brewery("Rohozec", "Rohozec", true));
		map.put("Rychtář", new Brewery("Rychtář", "Rychtář", true));
		map.put("Svijany", new Brewery("Svijany", "Svijany", true));
		map.put("Unětice", new Brewery("Unětice", "Pivovar Únětice", true));
		map.put("Vévoda", new Brewery("Vévoda", "Vysoký Chlumec", false));
		map.put("Žatec", new Brewery("Žatec", "Žatec", true));
		map.put("Zlínský švec", new Brewery("Zlínský švec", "Pivovar Malenovice", true));
	}
}
