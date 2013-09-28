package carnero.princ.data;

import java.util.ArrayList;
import java.util.HashMap;

public class Breweries {

	public static final HashMap<String, Brewery> map;

	static {
		map = new HashMap<String, Brewery>();

		map.put("Aspall", new Brewery("Aspall", "Aspall", true));
		map.put("Břevnov", new Brewery("Břevnov", "Břevnovský pivovar", true));
		map.put("Celia", new Brewery("Celia", "Žatec", false));
		map.put("Dudák", new Brewery("Dudák", "Strakonice", false));
		map.put("Chotěboř", new Brewery("Chotěboř", "Chotěboř", true));
		map.put("Chýně", new Brewery("Chýně", "Chýně", true));
		map.put("Ježek", new Brewery("Ježek", "Jihlava", false));
		map.put("Kocour", new Brewery("Kocour", "Kocour", true));
		map.put("Krakonoš", new Brewery("Krakonoš", "Krakonoš", true));
		map.put("Matuška", new Brewery("Matuška", "Matuška", true));
		map.put("Opat", new Brewery("Opat", "Broumov", false));
		map.put("Podkováň", new Brewery("Podkováň", "Podkováň", true));
		map.put("Primátor", new Brewery("Primátor", "Primátor", true));
		map.put("Rebel", new Brewery("Rebel", "Havlíčkův Brod", false));
		map.put("Rohozec", new Brewery("Rohozec", "Rohozec", true));
		map.put("Rychtář", new Brewery("Rychtář", "Rychtář", true));
		map.put("Svijany", new Brewery("Svijany", "Svijany", true));
		map.put("Žatec", new Brewery("Žatec", "Žatec", true));
	}
}
