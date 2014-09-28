/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec_atv01;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author gomes
 */




public class EC_atv01 {
    private final int roll, totalPalavras, numReposicoes, maxIteracoes;
    private final double erroThreshold;
    private final double[] PALAVRA_ALVO = {0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6};
    private final PrintWriter writer;
    private double fitness_total = 0, fitnessSelecionados = 0, fitnessUltimos = 0;
    int cont = 0;
    
    List<Palavra> populacao = new ArrayList<>();
    MersenneTwisterFast twister = new MersenneTwisterFast();

    public EC_atv01(int qtdPalavras, int qtdReposicoes, int maxIteracoes, double erroThreshold) throws FileNotFoundException, UnsupportedEncodingException {
        this.roll = 3;
        this.totalPalavras = qtdPalavras;
        this.numReposicoes = qtdReposicoes;
        this.maxIteracoes = maxIteracoes;
        this.erroThreshold = erroThreshold;
        
        writer = new PrintWriter("CE_atv01.csv", "UTF-8");
        writer.println("fitnessTotal;fitnessParcial;fitnessUltimos;melhorFitness");

        for (int i = 0; i < totalPalavras; i++) {
            Palavra palavra = new Palavra();
            populacao.add(palavra);
        }
        Collections.sort(populacao);
        //mostraPopulacao();
        System.out.println("\nTotal de palavras: " + totalPalavras + "\tNúmero de reposições: " + numReposicoes);
    }

    public void run() {
        cont = 0;
        while (cont < maxIteracoes && populacao.get(0).fitness < erroThreshold) {
            itera();
            cont++;
        }
        
        atualizaFitnessTotal();
        System.out.println("Iterações: " + cont + "("+maxIteracoes + ")\tMelhor fitness: " + populacao.get(0).fitness +"(" + erroThreshold+")");
        
        System.out.println(roll + " melhores:");
        for (int i = 0; i < roll; i++) {
            System.out.println("" + (i+1) + " - " + populacao.get(i).fitness
                    + "\n\t" + Arrays.toString(populacao.get(i).alelos));
        }
        
        
        System.out.println("Fitness da população: " + fitness_total);
        writer.close();
    }

    private void itera() {
        for (int i = 0; i < numReposicoes; i++) {
            populacao.remove(populacao.size() - 1);
        }
        
        for (int i = 0; i < numReposicoes; i++) {
            Palavra palavra = new Palavra();
            populacao.add(palavra);
        }
        Collections.sort(populacao);
        
        atualizaFitnessTotal();
        atualizaFitnessParciais();
        if(cont%100==0 || populacao.get(0).fitness >= erroThreshold){
            writer.println(fitness_total + ";" + fitnessSelecionados + ";" + fitnessUltimos + ";" + populacao.get(0).fitness);
        }
        
        //mostraPopulacao();
    }
    
    private void atualizaFitnessTotal(){
        fitness_total = 0;
        for (Palavra p : populacao) {
            fitness_total += p.fitness;
        }
        fitness_total /= populacao.size();
    }
    
    private void atualizaFitnessParciais(){
        
        fitnessSelecionados = 0;
        int selecionados = populacao.size() - numReposicoes;
        if (selecionados > 0){
            for (int i=0 ; i < selecionados; i++){
                fitnessSelecionados += populacao.get(i).fitness;
            }
            
            fitnessSelecionados /= selecionados;
        }
        
        fitnessUltimos = 0;
        int ultimos = populacao.size()-selecionados;
        if (ultimos > 0) {
            for (int i = ultimos; i < populacao.size(); i++) {
                fitnessUltimos += populacao.get(i).fitness;
            }
            fitnessUltimos /= ultimos;
        }
    }

    private double f(Palavra palavra) {
        double soma = 0;
        for (int indexAlelo = 0; indexAlelo < palavra.alelos.length; indexAlelo++) {
            soma += Math.abs(palavra.alelos[indexAlelo] - PALAVRA_ALVO[indexAlelo]);
        }
        
        return 1 / soma;
    }

    private class Palavra implements Comparable<Palavra> {

        protected double fitness = 0;
        protected double[] alelos = new double[10];

        Palavra() {
            // pseudo-aleatorio
            for (int i = 0; i < 10; i++) {
                alelos[i] = twister.nextDouble();
                //System.out.println(i + ": " + alelos[i]);
            }
        }

        @Override
        public String toString() {
            String s = "Palavra: \n\t";
            for (int i = 0; i < 10; i++) {
                s += alelos[i] + " ";
            }
            s += "\n";

            return s;
        }

        public int compare(Palavra p1, Palavra p2) {
            if (p2 == null) {
                return 0;
            }
            return p1.fitness > p2.fitness ? 1 : -1;
        }

        @Override
        public int compareTo(Palavra p) {
            final int MELHOR = -1, IGUAL = 0, PIOR = 1;

            this.fitness = f(this);
            p.fitness = f(p);

            if (this == p) {
                return IGUAL;
            }

            if (this.fitness > p.fitness) {
                return MELHOR;
            }
            if (this.fitness < p.fitness) {
                return PIOR;
            }

            //all comparisons have yielded equality
            //verify that compareTo is consistent with equals (optional)
            assert this.equals(p) : "compareTo inconsistent with equals.";

            return IGUAL;
        }

    };

    private void mostraPopulacao() {
        int l_cont = 0;
        for (Palavra p : populacao) {
            System.out.println(++l_cont + ": " + p.fitness);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        // TODO code application logic here
        int iPalavras = 60, iReposicoes= 30, iIteracoes = 100000;
        double dThreshold = 2.5;
        /*
        Scanner s = new Scanner(System.in);
        System.out.println("Quantidade de palavras:");
        iPalavras = s.nextInt();
        System.out.println("Número de reposições:");
        iReposicoes = s.nextInt();
        System.out.println("Limite de iterações:");
        iIteracoes = s.nextInt();
        System.out.println("Erro threshold:");
        dThreshold = s.nextDouble();
        */
        EC_atv01 teste1 = new EC_atv01(iPalavras, iReposicoes, iIteracoes, dThreshold);

        teste1.run();
    }

}
