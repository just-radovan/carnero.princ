package carnero.princ.data;

import java.util.HashMap;

public class Breweries {

	public static final HashMap<String, Brewery> map;

	static {
		map = new HashMap<String, Brewery>();

		map.put("Aspall", new Brewery("Aspall", "Aspall", true));
		map.put("Brevnov", new Brewery("Brevnov", "Břevnovský pivovar", true));
		map.put("Celia", new Brewery("Celia", "Žatec", false));
		map.put("Demon", new Brewery("Demon", "Vysoký Chlumec", false));
		map.put("Dudak", new Brewery("Dudak", "Strakonice", false));
		map.put("Chotebor", new Brewery("Chotebor", "Chotěboř", true));
		map.put("Chyne", new Brewery("Chyne", "Chýně", true));
		map.put("Jezek", new Brewery("Jezek", "Jihlava", false));
		map.put("Klostermann", new Brewery("Klostermann", "Pivovar Strakonice", false));
		map.put("Kocour", new Brewery("Kocour", "Kocour", true));
		map.put("Krakonos", new Brewery("Krakonos", "Krakonoš", true));
		map.put("Matuska", new Brewery("Matuska", "Matuška", true));
		map.put("Opat", new Brewery("Opat", "Broumov", false));
		map.put("Podkovan", new Brewery("Podkovan", "Podkováň", true));
		map.put("Princ", new Brewery("Princ Max", "Vysoký Chlumec", false));
		map.put("Primator", new Brewery("Primator", "Primátor", true));
		map.put("Rebel", new Brewery("Rebel", "Havlíčkův Brod", false));
		map.put("Rataj", new Brewery("Rataj", "Rychtář", false));
		map.put("Rohozec", new Brewery("Rohozec", "Rohozec", true));
		map.put("Rychtar", new Brewery("Rychtar", "Rychtář", true));
		map.put("Svijany", new Brewery("Svijany", "Svijany", true));
		map.put("Unetice", new Brewery("Unetice", "Pivovar Únětice", true));
		map.put("Vevoda", new Brewery("Vevoda", "Vysoký Chlumec", false));
		map.put("Zatec", new Brewery("Zatec", "Žatec", true));
		map.put("Zemsky pivovar", new Brewery("Zemsky pivovar", "Zemský pivovar", true));
		map.put("Zlinsky svec", new Brewery("Zlinsky svec", "Pivovar Malenovice", true));
	}
}
