package com.example.springaiapp.domain.joiners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.lang.NonNull;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

import lombok.Builder;

@Builder
public class BM25DocumentPostProcessor implements DocumentPostProcessor {

    @Builder.Default
    private static final LanguageDetector languageDetector = LanguageDetectorBuilder
            .fromLanguages(Language.ENGLISH, Language.RUSSIAN)
            .build();

    // BM25 parameters
    @Builder.Default
    private final double K = 1.2;
    @Builder.Default
    private final double B = 0.75;
    @Builder.Default
    private final int limit = 10;

    @Override
    @NonNull
    public List<Document> process(Query query, List<Document> documents) {
        return rerank(documents, query.text(), limit);
    }


    private List<Document> rerank(List<Document> corpus, String query, int limit) {

        if (corpus == null || corpus.isEmpty()) {
            return new ArrayList<>();
        }

        // Compute corpus statistics
        CorpusStats stats = computeCorpusStats(corpus);

        // Tokenize query
        List<String> queryTerms = tokenize(query);

        // Score and sort documents
        return corpus.stream()
                .sorted((d1, d2) -> Double.compare(
                        score(queryTerms, d2, stats),
                        score(queryTerms, d1, stats)
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private CorpusStats computeCorpusStats(List<Document> corpus) {
        Map<String, Integer> docFreq = new HashMap<>();
        Map<Document, List<String>> tokenizedDocs = new HashMap<>();
        int totalLength = 0;
        int totalDocs = corpus.size();

        // Process each document
        for (Document doc : corpus) {
            List<String> tokens = tokenize(doc.getText());
            tokenizedDocs.put(doc, tokens);
            totalLength += tokens.size();

            // Update document frequencies
            Set<String> uniqueTerms = new HashSet<>(tokens);
            for (String term : uniqueTerms) {
                docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
            }
        }

        double avgDocLength = (double) totalLength / totalDocs;

        return new CorpusStats(docFreq, tokenizedDocs, avgDocLength, totalDocs);
    }

    private double score(List<String> queryTerms, Document doc, CorpusStats stats) {
        List<String> tokens = stats.tokenizedDocs.get(doc);
        if (tokens == null) {
            return 0.0;
        }

        // Calculate term frequencies for this document
        Map<String, Integer> tfMap = new HashMap<>();
        for (String token : tokens) {
            tfMap.put(token, tfMap.getOrDefault(token, 0) + 1);
        }

        int docLength = tokens.size();
        double score = 0.0;

        // Calculate BM25 score
        for (String term : queryTerms) {
            int tf = tfMap.getOrDefault(term, 0); //просто его count - то есть этого влияет на его вес в документе
            int df = stats.docFreq.getOrDefault(term, 1);

            // BM25 IDF calculation редкость слова - оно поднимает
            double idf = Math.log(1 + (stats.totalDocs - df + 0.5) / (df + 0.5));

            // BM25 term score calculation
            double numerator = tf * (K + 1);
            double denominator = tf + K * (1 - B + B * docLength / stats.avgDocLength);
            score += idf * (numerator / denominator);
        }

        return score;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Analyzer analyzer = detectLanguageAnalyzer(text);

        try (TokenStream stream = analyzer.tokenStream(null, text)) {
            stream.reset();
            while (stream.incrementToken()) {
                tokens.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.end();
        } catch (IOException e) {
            throw new RuntimeException("Tokenization failed", e);
        }

        return tokens;
    }

    private Analyzer detectLanguageAnalyzer(String text) {
        Language lang = languageDetector.detectLanguageOf(text);
        if (lang == Language.ENGLISH) {
            return new EnglishAnalyzer();
        } else if (lang == Language.RUSSIAN) {
            return new RussianAnalyzer();
        } else {
            // Fallback to English analyzer for unsupported languages
            return new EnglishAnalyzer();
        }
    }

    // Inner class to hold corpus statistics
    private class CorpusStats {
        final Map<String, Integer> docFreq;
        final Map<Document, List<String>> tokenizedDocs;
        final double avgDocLength;
        final int totalDocs;

        CorpusStats(Map<String, Integer> docFreq,
                    Map<Document, List<String>> tokenizedDocs,
                    double avgDocLength,
                    int totalDocs) {
            this.docFreq = docFreq;
            this.tokenizedDocs = tokenizedDocs;
            this.avgDocLength = avgDocLength;
            this.totalDocs = totalDocs;
        }
    }
}
