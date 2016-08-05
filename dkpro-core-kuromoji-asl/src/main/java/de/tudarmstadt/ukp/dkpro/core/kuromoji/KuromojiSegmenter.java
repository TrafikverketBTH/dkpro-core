/*
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.kuromoji;

import java.util.List;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * BreakIterator segmenter.
 */
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class KuromojiSegmenter
    extends SegmenterBase
{
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
    }

    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        int sentenceBegin = 0;
        int sentenceEnd = text.indexOf("。");
        while (sentenceEnd > sentenceBegin) {
            String stext = text.substring(sentenceBegin, sentenceEnd + 1);
            
            processSentence(aJCas, stext, zoneBegin + sentenceBegin);
            
            sentenceBegin = sentenceEnd + 1;
            sentenceEnd = text.indexOf("。", sentenceBegin);
        }
        
        if (sentenceBegin < text.length()) {
            String stext = text.substring(sentenceBegin, text.length());
            processSentence(aJCas, stext, zoneBegin + sentenceBegin);
        }
    }
    
    private Sentence processSentence(JCas aJCas, String text, int zoneBegin)
    {
        String innerText = text;
        boolean addFinalToken = false;
        if (innerText.endsWith("。")) {
            innerText = text.substring(0, text.length() - 1);
            addFinalToken = true;
        }
        
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(innerText);

        Annotation firstToken = null;
        Annotation lastToken = null;
        
        for (Token t : tokens) {
            Annotation ut = createToken(aJCas, t.getPosition() + zoneBegin,
                    t.getPosition() + t.getSurface().length() + zoneBegin);
            
            // Tokenizer reports whitespace as tokens - we don't add whitespace-only tokens.
            if (ut == null) {
                continue;
            }
            
            if (firstToken == null) {
                firstToken = ut;
            }
            
            lastToken = ut;
        }
        
        if (addFinalToken) {
            lastToken = createToken(aJCas, zoneBegin + text.length() - 1,
                    zoneBegin + text.length());
        }

        if (firstToken != null && lastToken != null) {
            return createSentence(aJCas, firstToken.getBegin(), lastToken.getEnd());
        }
        else {
            return null;
        }
    }
}
