package org.dkpro.core.stagger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class StockholmSegmenterSectionNumbersTest {
	private JCas jcas;
	private AnalysisEngine aed;
    
    @Before
    public void setup() throws Exception 
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("sv");
        aed = createEngine(StockholmSegmenter.class);
        
    }
    
    @Test
    public void testSentencesWithSectionNumbers_01() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 9.4. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 9.4.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Test
    public void testSentencesWithSectionNumbers_02() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 9.4 och 9.5. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 9.4 och 9.5.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
    @Test
    public void testSentencesWithSectionNumbers_03() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 9. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 9.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
    @Test
    public void testSentencesWithSectionNumbers_04() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 10.4. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 10.4.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
    @Test
    public void testSentencesWithSectionNumbers_05() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 10.4.3. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 10.4.3.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
    @Test
    public void testSentencesWithSectionNumbers_06() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 10.11.4. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 10.11.4.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
    @Test
    public void testSentencesWithSectionNumbers_07() throws Exception
    {
		jcas.setDocumentText("Har mätts upp enligt avsnitt 10.11.42. Dämpningen ska mätas var tjugofjärde timme under hela testperioden.");
		aed.process(jcas);
		
		String[] sentences = {"Har mätts upp enligt avsnitt 10.11.42.", "Dämpningen ska mätas var tjugofjärde timme under hela testperioden."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
	@Test
    public void testSentencesWithSectionNumbers_08() throws Exception
    {
		jcas.setDocumentText("Krav för godkännande: Ingen dämpningsförändring.För fiberband får skalkraften inte ändras mer än 30 % av det värde som har mätts upp enligt avsnitt 9.4.1.");
		aed.process(jcas);
		
		String[] sentences = {"Krav för godkännande: Ingen dämpningsförändring.", "För fiberband får skalkraften inte ändras mer än 30 % av det värde som har mätts upp enligt avsnitt 9.4.1."};
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

}
