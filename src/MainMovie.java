import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainMovie {

    // Classe que representa um conjunto fuzzy (função de pertinência trapezoidal)
    static class FuzzySet {
        String label;
        double a, b, c, d;
        
        public FuzzySet(String label, double a, double b, double c, double d) {
            this.label = label;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
        
        // Calcula o grau de pertinência para um valor x
        public double getMembership(double x) {
            if (x < a || x > d) {
                return 0;
            }
            if (x >= b && x <= c) {
                return 1;
            }
            if (x >= a && x < b) {
                return (x - a) / (b - a);
            }
            if (x > c && x <= d) {
                return (d - x) / (d - c);
            }
            return 0;
        }
    }

    // Classe que representa uma variável fuzzy contendo diversos conjuntos
    static class FuzzyVariable {
        String name;
        List<FuzzySet> sets;
        
        public FuzzyVariable(String name) {
            this.name = name;
            this.sets = new ArrayList<>();
        }
        
        public void addFuzzySet(FuzzySet set) {
            sets.add(set);
        }
        
        // Retorna o grau de pertinência para um conjunto com label específico para o valor x
        public double getMembershipForLabel(String label, double x) {
            for (FuzzySet set : sets) {
                if (set.label.equalsIgnoreCase(label)) {
                    return set.getMembership(x);
                }
            }
            return 0;
        }
    }
    
    // Interface para avaliação da condição da regra
    interface RuleEvaluator {
        double evaluate(double genreHigh, double genreMed, double voteHigh, double voteMed, double voteLow, double runtimeIdeal, double directorFav);
    }
    
    // Classe que representa uma regra fuzzy
    static class FuzzyRule {
        String ruleName;
        double outputRepresentative; // valor representativo do output (score)
        RuleEvaluator evaluator;
        
        public FuzzyRule(String ruleName, double outputRepresentative, RuleEvaluator evaluator) {
            this.ruleName = ruleName;
            this.outputRepresentative = outputRepresentative;
            this.evaluator = evaluator;
        }
        
        // Retorna o grau de ativação da regra, dada a fuzzificação dos inputs
        public double getActivation(double genreHigh, double genreMed, double voteHigh, double voteMed, double voteLow, double runtimeIdeal, double directorFav) {
            return evaluator.evaluate(genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav);
        }
    }
    
    // Classe para armazenar o título do filme e seu score final
    static class MovieRanking {
        String title;
        double score;
        
        public MovieRanking(String title, double score) {
            this.title = title;
            this.score = score;
        }
    }
    
    public static void main(String[] args) {
        // Definindo os gêneros favoritos
        Set<String> favoriteGenres = new HashSet<>();
        favoriteGenres.add("Action");
        favoriteGenres.add("Adventure");
        favoriteGenres.add("Comedy");

        // Definindo diretores favoritos
        Set<String> favoriteDirectors = new HashSet<>();
        favoriteDirectors.add("Christopher Nolan");
        favoriteDirectors.add("Steven Spielberg");

        // Criação da variável fuzzy para Gênero (score de 0 a 10)
        FuzzyVariable genreVar = new FuzzyVariable("Gênero");
        genreVar.addFuzzySet(new FuzzySet("Baixo", 0, 0, 3, 6));
        genreVar.addFuzzySet(new FuzzySet("Medio", 3, 5, 7, 9));
        genreVar.addFuzzySet(new FuzzySet("Alto", 7, 9, 10, 10));

        // Criação da variável fuzzy para Runtime (duração do filme, em minutos)
        FuzzyVariable runtimeVar = new FuzzyVariable("Runtime");
        runtimeVar.addFuzzySet(new FuzzySet("Curto", 0, 0, 80, 100));
        runtimeVar.addFuzzySet(new FuzzySet("Ideal", 80, 100, 120, 140));
        runtimeVar.addFuzzySet(new FuzzySet("Longo", 120, 140, 300, 300));

        // Criação da variável fuzzy para Vote_Average (avaliação dos usuários, escala 0-10)
        FuzzyVariable voteVar = new FuzzyVariable("Vote_Average");
        voteVar.addFuzzySet(new FuzzySet("Baixo", 0, 0, 4, 6));
        voteVar.addFuzzySet(new FuzzySet("Medio", 4, 6, 7, 8));
        voteVar.addFuzzySet(new FuzzySet("Alto", 7, 8, 10, 10));

        // Definindo as regras fuzzy (valores representativos: Baixo = 3, Medio = 5.5, Alto = 8.5)
        List<FuzzyRule> rules = new ArrayList<>();
        
        // Regra 1: IF (Gênero é Alto) AND (Vote_Average é Alto) AND (Diretor é Favorito) THEN Score é Alto.
        rules.add(new FuzzyRule("Rule 1", 8.5, (genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav) -> 
                Math.min(Math.min(genreHigh, voteHigh), directorFav)
        ));
        
        // Regra 2: IF (Gênero é Alto) AND (Vote_Average é Alto) THEN Score é Alto.
        rules.add(new FuzzyRule("Rule 2", 8.5, (genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav) -> 
                Math.min(genreHigh, voteHigh)
        ));
        
        // Regra 3: IF (Gênero é Alto) AND (Vote_Average é Medio) THEN Score é Medio.
        rules.add(new FuzzyRule("Rule 3", 5.5, (genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav) -> 
                Math.min(genreHigh, voteMed)
        ));
        
        // Regra 4: IF (Gênero é Medio) AND (Vote_Average é Alto) THEN Score é Medio.
        rules.add(new FuzzyRule("Rule 4", 5.5, (genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav) -> 
                Math.min(genreMed, voteHigh)
        ));
        
        // Regra 5: IF (Vote_Average é Baixo) THEN Score é Baixo.
        rules.add(new FuzzyRule("Rule 5", 3.0, (genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav) -> 
                voteLow
        ));
        
        // Regra 6: IF (Runtime é Ideal) THEN Score é Medio.
        rules.add(new FuzzyRule("Rule 6", 5.5, (genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav) -> 
                runtimeIdeal
        ));
        
        List<MovieRanking> movieRankings = new ArrayList<>();
        
        // Caminho do arquivo CSV (certifique-se de que o arquivo esteja no diretório correto)
        String csvFile = "movie_dataset_filtered.csv";
        String line = "";
        String delimiter = ";";
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Lê o cabeçalho e determina os índices das colunas necessárias
            String headerLine = br.readLine();
            if(headerLine == null) {
                System.out.println("Arquivo vazio!");
                return;
            }
            String[] headers = headerLine.split(delimiter);
            
            int indexGenres = -1;
            int indexRuntime = -1;
            int indexVoteAverage = -1;
            int indexDirector = -1;
            int indexTitle = -1;
            
            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim().toLowerCase();
                if (col.equals("genres")) indexGenres = i;
                if (col.equals("runtime")) indexRuntime = i;
                if (col.equals("vote_average")) indexVoteAverage = i;
                if (col.equals("director")) indexDirector = i;
                if (col.equals("title")) indexTitle = i;
            }
            
            if(indexGenres == -1 || indexRuntime == -1 || indexVoteAverage == -1 || indexDirector == -1) {
                System.out.println("Não foram encontradas todas as colunas necessárias no dataset.");
                return;
            }
            
            // Processa cada linha (filme) do arquivo
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(delimiter);
                
                // --- Fuzzificação do critério Gênero ---
                String genresStr = fields[indexGenres];
                String[] genresArray = genresStr.split(" ");
                int countFav = 0;
                for (String g : genresArray) {
                    if (favoriteGenres.contains(g.trim())) {
                        countFav++;
                    }
                }
                double genreRatio = 0;
                if (genresArray.length > 0) {
                    genreRatio = (countFav / (double) genresArray.length) * 10; // escala de 0 a 10
                }
                double genreHigh = genreVar.getMembershipForLabel("Alto", genreRatio);
                double genreMed = genreVar.getMembershipForLabel("Medio", genreRatio);
                
                // --- Fuzzificação do critério Runtime ---
                double runtimeVal = 0;
                if (fields.length > indexRuntime) {
                    try {
                        runtimeVal = Double.parseDouble(fields[indexRuntime]);
                    } catch (NumberFormatException e) {
                        runtimeVal = 0;
                    }
                }
                double runtimeIdeal = runtimeVar.getMembershipForLabel("Ideal", runtimeVal);
                
                // --- Fuzzificação do critério Vote_Average ---
                double voteVal = 0;
                if (fields.length > indexVoteAverage) {
                    try {
                        voteVal = Double.parseDouble(fields[indexVoteAverage]);
                    } catch (NumberFormatException e) {
                        voteVal = 0;
                    }
                }
                double voteHigh = voteVar.getMembershipForLabel("Alto", voteVal);
                double voteMed = voteVar.getMembershipForLabel("Medio", voteVal);
                double voteLow = voteVar.getMembershipForLabel("Baixo", voteVal);
                
                // --- Fuzzificação do critério Diretor ---
                String director = "";
                if (fields.length > indexDirector) {
                    director = fields[indexDirector].trim();
                }
                double directorFav = favoriteDirectors.contains(director) ? 1.0 : 0.0;
                
                // --- Inferência fuzzy utilizando as regras definidas ---
                double numerator = 0;
                double denominator = 0;
                for (FuzzyRule rule : rules) {
                    double activation = rule.getActivation(genreHigh, genreMed, voteHigh, voteMed, voteLow, runtimeIdeal, directorFav);
                    numerator += activation * rule.outputRepresentative;
                    denominator += activation;
                }
                
                double finalScore = (denominator > 0) ? (numerator / denominator) : 0;
                
                // Recupera o título do filme (se disponível)
                String title = "Título desconhecido";
                if (indexTitle != -1 && fields.length > indexTitle) {
                    title = fields[indexTitle].trim();
                }
                
                // Adiciona o filme e seu score na lista
                movieRankings.add(new MovieRanking(title, finalScore));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Ordena a lista em ordem decrescente de score
        Collections.sort(movieRankings, new Comparator<MovieRanking>() {
            @Override
            public int compare(MovieRanking m1, MovieRanking m2) {
                return Double.compare(m2.score, m1.score);
            }
        });
        
        // Exibe os 10 filmes com melhor score
        System.out.println("\nTop 10 filmes mais bem ranqueados:");
        for (int i = 0; i < Math.min(10, movieRankings.size()); i++) {
            MovieRanking mr = movieRankings.get(i);
            System.out.println((i+1) + ". " + mr.title + " -> Score: " + mr.score);
        }
    }
}
