package org.dkpro.core.stagger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.stagger.StockholmSegmenter;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class StockholmSegmenterTest
{
    JCas jcas;
    
    @Before
    public void setup() throws Exception 
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("sv");
        jcas.setDocumentText("Det är enkelt (ex. biljetthallar m.m.). Det åckso, men inte så mycket!? SMS:a mig.");
        
        AnalysisEngine aed = createEngine(StockholmSegmenter.class);
        aed.process(jcas);
    }
    
    @Test
    public void testTokens()
    {   
        String[] tokens = { "Det", "är", "enkelt", "(", "ex.", "biljetthallar", "m.m.", ")", ".", 
        		"Det", "åckso", ",", "men", "inte", "så", "mycket", "!?", "SMS:a", "mig", "."};   
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testSentencesWithAbbreviations()
    {
        String[] sentences = {"Det är enkelt (ex. biljetthallar m.m.).", "Det åckso, men inte så mycket!?", "SMS:a mig."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
}
