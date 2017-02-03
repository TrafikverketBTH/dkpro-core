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
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class StockholmAbbreviationsSegmenterTest
{
    JCas jcas;
    
    @Before
    public void setup() throws Exception 
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("sv");
        jcas.setDocumentText("Plats för radiokommunikationsantenner, ex. UMTS, GSM, GSM-R samt övriga radiosystem, ska finnas i stora allmänna platser (ex. biljetthallar m.m.) där läckande kabel ej kan monteras.");
        
        AnalysisEngine aed = createEngine(StockholmSegmenter.class);
        aed.process(jcas);
    }
    
    @Test
    public void testAbbreviations()
    {
        String[] sentences = {"Plats för radiokommunikationsantenner, ex. UMTS, GSM, GSM-R samt övriga radiosystem, ska finnas i stora allmänna platser (ex. biljetthallar m.m.) där läckande kabel ej kan monteras."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

}
