package carnero.princ.data;

import java.util.HashMap;

public class Breweries {

	public static final HashMap<String, Brewery> map;

	static {
		map = new HashMap<String, Brewery>();

		// breweries
		map.put("Anderson Valley", new Brewery("Anderson Valley", "Anderson Valley Brewing Co. (USA)", true));
		map.put("Aspall", new Brewery("Aspall", "Aspall (UK)", true));
		map.put("Auer", new Brewery("Auer", "Schlossbrauerei Au-Hallertau (D)", true));
		map.put("Belgian Yeti", new Brewery("Belgian Yeti", "Great Divide Brewing Co. (USA)", false));
		map.put("Bernard", new Brewery("Bernard", "Bernard", true));
		map.put("Brevnov", new Brewery("Brevnov", "Břevnovský pivovar", true));
		map.put("Celia", new Brewery("Celia", "Žatec", false));
		map.put("Demon", new Brewery("Demon", "Vysoký Chlumec", false));
		map.put("Dudak", new Brewery("Dudak", "Strakonice", false));
		map.put("Hubertus", new Brewery("Hubertus", "Pivovar Kácov", false));
		map.put("Chipper", new Brewery("Chipper", "Primátor", false));
		map.put("Chlumecky", new Brewery("Chlumecky", "Pivovar Vysoký Chlumec", false));
		map.put("Chotebor", new Brewery("Chotebor", "Chotěboř", true));
		map.put("Chyne", new Brewery("Chyne", "Chýně", true));
		map.put("Jezek", new Brewery("Jezek", "Jihlava", false));
		map.put("Kaltenecker", new Brewery("Kaltenecker", "Kaltenecker (SK)", true));
		map.put("Klaster", new Brewery("Klaster", "Pivovar Klášter", true));
		map.put("Klostermann", new Brewery("Klostermann", "Pivovar Strakonice", false));
		map.put("Kocour", new Brewery("Kocour", "Kocour", true));
		map.put("Krakonos", new Brewery("Krakonos", "Krakonoš", true));
		map.put("Kraus", new Brewery("Kraus", "Brauerei Kraus (D)", true));
		map.put("Magic Rock", new Brewery("Magic Rock", "Magic Rock Brewing (USA)", true));
		map.put("Mahr´s Brau", new Brewery("Mahr´s Brau", "Mahrs Bräu (D)", true));
		map.put("Mahr's Brau", new Brewery("Mahr's Brau", "Mahrs Bräu (D)", true));
		map.put("Matuska", new Brewery("Matuska", "Matuška", true));
		map.put("Merlin", new Brewery("Merlin", "Pivovar Protivín", false));
		map.put("Mikkeller", new Brewery("Mikkeller", "Mikkeller (DK)", true));
		map.put("Nomad", new Brewery("Nomad", "Nomád", true));
		map.put("Opat", new Brewery("Opat", "Broumov", false));
		map.put("Permon", new Brewery("Permon", "Permon", true));
		map.put("Podkovan", new Brewery("Podkovan", "Podkováň", true));
		map.put("Princ Max", new Brewery("Princ Max", "Vysoký Chlumec", false));
		map.put("Primator", new Brewery("Primator", "Primátor", true));
		map.put("Rebel", new Brewery("Rebel", "Havlíčkův Brod", false));
		map.put("Rataj", new Brewery("Rataj", "Rychtář", false));
		map.put("Rohozec", new Brewery("Rohozec", "Rohozec", true));
		map.put("Rychtar", new Brewery("Rychtar", "Rychtář", true));
		map.put("Schneider Weisse", new Brewery("Schneider Weisse", "Schneider-Weisse (D)", true));
		map.put("Skalak", new Brewery("Skalak", "Rohozec", false));
		map.put("Svijany", new Brewery("Svijany", "Svijany", true));
		map.put("Tambor", new Brewery("Tambor", "Tambor", true));
		map.put("Unetice", new Brewery("Unetice", "Pivovar Únětice", true));
		map.put("Uneticke pivo", new Brewery("Uneticke pivo", "Pivovar Únětice", true));
		map.put("Vevoda", new Brewery("Vevoda", "Vysoký Chlumec", false));
		map.put("Vyskov", new Brewery("Vyskov", "Pivovar Vyškov", true));
		map.put("Vyskovsky", new Brewery("Vyskovsky", "Pivovar Vyškov", true));
		map.put("Zatec", new Brewery("Zatec", "Žatec", true));
		map.put("Zemsky pivovar", new Brewery("Zemsky pivovar", "Zemský pivovar", true));
		map.put("Zlinsky svec", new Brewery("Zlinsky svec", "Pivovar Malenovice", false));
		map.put("Zvikov", new Brewery("Zvikov", "Zvíkov", true));
		// specific beers
		map.put("Libertine Black Ale", new Brewery("Libertine Black Ale", "BrewDog (UK)", false));
		map.put("Punk IPA", new Brewery("Punk IPA", "BrewDog (UK)", false));
		map.put("Rebel IPA", new Brewery("Rebel IPA", "Boston Beer Co. (USA)", false));
		map.put("Raging Bitch", new Brewery("Raging Bitch", "Flying Dog Brewery (USA)", false));
		map.put("Snake Dog", new Brewery("Snake Dog", "Flying Dog Brewery (USA)", false));
	}
}
