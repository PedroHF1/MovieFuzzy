import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MainMovie {
	public static void main(String[] args) {		
		enum Generos {
			Action,
			Adventure,
			Animation,
			Comedy,
			Crime,
			Documentary,
			Drama,
			Family,
			Fantasy,
			Foreign,
			History,
			Horror,
			Music,
			Mystery,
			Romance,
			Science,
			Thriler,
			TV,
			War,
			Western
		}
		
		GrupoVariaveis grupoPopularidade = new GrupoVariaveis();
		grupoPopularidade.add(new VariavelFuzzy("MPP", 0, 0, 10, 20));
		grupoPopularidade.add(new VariavelFuzzy("PP", 0, 0, 10, 20));
		grupoPopularidade.add(new VariavelFuzzy("P", 0, 0, 10, 20));
		grupoPopularidade.add(new VariavelFuzzy("MP", 0, 0, 10, 20));
		grupoPopularidade.add(new VariavelFuzzy("EP", 0, 0, 10, 20));
		
		GrupoVariaveis grupoGenero = new GrupoVariaveis();
		grupoGenero.add(new VariavelFuzzy("GR",0,0,3,6));
		grupoGenero.add(new VariavelFuzzy("GB",5,7,8,10));
		grupoGenero.add(new VariavelFuzzy("GF",7,9,10,10));
		
		GrupoVariaveis grupoVotos = new GrupoVariaveis();
		grupoVotos.add(new VariavelFuzzy("V_MPV",0,0,10,20));
		grupoVotos.add(new VariavelFuzzy("V_PV",10,20,50,60));
		grupoVotos.add(new VariavelFuzzy("V_MEV",40,80,200,300));
		grupoVotos.add(new VariavelFuzzy("V_BAV",200,300,500,1000));
		grupoVotos.add(new VariavelFuzzy("V_MUV",400,500,3200,3200));
		
		GrupoVariaveis grupoFavorito = new GrupoVariaveis();
		grupoFavorito.add(new VariavelFuzzy("NF",0,0,3,6));
		grupoFavorito.add(new VariavelFuzzy("F",5,7,8,10));
		grupoFavorito.add(new VariavelFuzzy("MF",7,9,10,10));
		
		GrupoVariaveis grupoLancamento = new GrupoVariaveis();
        // "Launched": movies with age 0-5 (full membership from 0 to 3, then decays)
        grupoLancamento.add(new VariavelFuzzy("Launched", 0, 0, 3, 5));
        // "Recent": movies with age roughly 3-12 (increasing from 3 to 5, full between 5 and 10, then decreasing)
        grupoLancamento.add(new VariavelFuzzy("Recent", 3, 5, 10, 12));
        // "Old Movie": movies with age roughly 10-60 (increasing from 10 to 15, full between 15 and 50, then decreasing)
        grupoLancamento.add(new VariavelFuzzy("Old Movie", 10, 15, 50, 60));
		
        Set<String> favoriteGenres = new HashSet<>();
        favoriteGenres.add("Action");
        favoriteGenres.add("Adventure");
        favoriteGenres.add("Comedy");
        favoriteGenres.add("Drama");
        favoriteGenres.add("Thriler");

		
		try {
			BufferedReader bfr = new BufferedReader(new FileReader(new File("movie_dataset_filtered.csv")));
			
			String header = bfr.readLine();
			String splitheader[] = header.split(";");
			for (int i = 0; i < splitheader.length;i++) {
				System.out.println(""+i+" "+splitheader[i]);
			}
			
			String line = "";
			
			while((line=bfr.readLine())!=null) {
				String spl[] = line.split(";");
				HashMap<String,Float> asVariaveis = new HashMap<String,Float>();
				
				float popularidade = Float.parseFloat(spl[9]);
				grupoPopularidade.fuzzifica(popularidade, asVariaveis);
				
				String genero = spl[2];
                String[] splitGenero = genero.split(" ");
                int countFavorite = 0;
                for(String g : splitGenero) {
                    if(favoriteGenres.contains(g.trim())) {
                        countFavorite++;
                    }
                }
                float genreScore = 0;
                if(splitGenero.length > 0) {

                    genreScore = (countFavorite / (float) splitGenero.length) * 10;
                }

                grupoFavorito.fuzzifica(genreScore, asVariaveis);
				
				float votos = Float.parseFloat(spl[20]);
				grupoVotos.fuzzifica(votos, asVariaveis);
				
				 // Assume release date is in column index 11 and is in the format "YYYY-MM-DD"
                String releaseDate = spl[12];
                int releaseYear = Integer.parseInt(releaseDate.split("-")[0]);
                int movieAge = 2025 - releaseYear; // using 2025 as the current year
                grupoLancamento.fuzzifica((float) movieAge, asVariaveis);
                
                System.out.println(spl[7] + " - popularidade " + popularidade + 
                        " genero " + genero + " (score: " + genreScore + ") votos " + votos + 
                        " release date: " + releaseDate + " (age: " + movieAge + ")");
				//System.out.println("rating "+rating+" -> "+asVariaveis);
				
				// Barato e B -> A
				// Muito Barato e B -> A
				// Muito Barato e MB -> MA
				// Barato e MB -> MA
				// Barato e R -> NA
				// Muito Barato e R -> A
				// Muito Barato e MR -> NA
				
//				rodaRegraE(asVariaveis,"Barato","B","A");
//				rodaRegraE(asVariaveis,"Muito Barato","B","A");
//				rodaRegraE(asVariaveis,"Muito Barato","MB","MA");
//				rodaRegraE(asVariaveis,"Barato","MB","MA");
//				rodaRegraE(asVariaveis,"Barato","R","NA");
//				rodaRegraE(asVariaveis,"Muito Barato","R","A");
//				rodaRegraE(asVariaveis,"Muito Barato","MR","NA");
//				rodaRegraE(asVariaveis,"Muito Caro","MR","NA");
//				rodaRegraE(asVariaveis,"Muito Caro","R","NA");
//				rodaRegraE(asVariaveis,"Muito Caro","B","NA");
//				rodaRegraE(asVariaveis,"Muito Caro","MB","A");
//				
//				rodaRegraE(asVariaveis,"MA","V_MPV","NA");
//				rodaRegraE(asVariaveis,"MA","V_PV","A");
//				rodaRegraE(asVariaveis,"MA","V_MEV","A");
//				
//				rodaRegraE(asVariaveis,"A","V_MPV","NA");
//				rodaRegraE(asVariaveis,"A","V_PV","NA");
//				rodaRegraE(asVariaveis,"A","V_MEV","NA");
				
//				float NA = asVariaveis.get("NA");
//				float A = asVariaveis.get("A");
//				float MA = asVariaveis.get("MA");
//				
//				float score = (NA*1.5f+A*7.0f+MA*9.5f)/(NA+A+MA);
//				
//				System.out.println("NA "+NA+" A "+A +" MA "+MA);
//				System.out.println(" "+custodinheiro+" "+rating +"-> "+score);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void rodaRegraE(HashMap<String, Float> asVariaveis,String var1,String var2,String varr) {
		float v = Math.min(asVariaveis.get(var1),asVariaveis.get(var2));
		if(asVariaveis.keySet().contains(varr)) {
			float vatual = asVariaveis.get(varr);
			asVariaveis.put(varr, Math.max(vatual, v));
		}else {
			asVariaveis.put(varr, v);
		}
	}
	
	private static void rodaRegraOU(HashMap<String, Float> asVariaveis,String var1,String var2,String varr) {
		float v = Math.max(asVariaveis.get(var1),asVariaveis.get(var2));
		if(asVariaveis.keySet().contains(varr)) {
			float vatual = asVariaveis.get(varr);
			asVariaveis.put(varr, Math.max(vatual, v));
		}else {
			asVariaveis.put(varr, v);
		}
	}
	
}
