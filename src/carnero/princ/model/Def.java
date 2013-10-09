package carnero.princ.model;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Def {

	public ArrayList<DefBrewery> breweries; // from json
	public HashMap<String, Pair<DefBrewery, DefBeer>> map = new HashMap<String, Pair<DefBrewery, DefBeer>>();
}
