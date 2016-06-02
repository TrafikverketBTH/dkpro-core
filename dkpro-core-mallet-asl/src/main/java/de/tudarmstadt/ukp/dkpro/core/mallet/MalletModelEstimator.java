/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet;

import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.mallet.internal.AnnotationStringSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.mallet.internal.CharacterStringSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.mallet.internal.StringSequenceGenerator;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.Locale;

/**
 * This abstract class defines parameters and methods that are common for Mallet model estimators.
 * <p>
 * It creates a Mallet {@link InstanceList} from the input documents so that inheriting estimators
 * can create a model, typically implemented by overriding the {@link JCasFileWriter_ImplBase#collectionProcessComplete()}
 * method.
 *
 * @see de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings.WordEmbeddingsEstimator
 * @see de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelEstimator
 * @since 1.9.0
 */
public abstract class MalletModelEstimator
        extends JCasFileWriter_ImplBase
{
    /**
     * The annotation type to use as input tokens for the model estimation.
     * Default: {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token}.
     * For lemmas, for instance, use {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value}
     */
    public static final String PARAM_TOKEN_FEATURE_PATH = "tokenFeaturePath";
    /**
     * The number of threads to use during model estimation.
     * If not set, the number of threads is automatically set by {@link ComponentParameters#computeNumThreads(int)}.
     * <p>
     * Warning: do not set this to more than 1 when using very small (test) data sets on {@link de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings.WordEmbeddingsEstimator}!
     * This might prevent the process from terminating.
     */
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    /**
     * Ignore tokens (or any other annotation type, as specified by {@link #PARAM_TOKEN_FEATURE_PATH})
     * that are shorter than the given value. Default: 3.
     */
    public static final String PARAM_MIN_TOKEN_LENGTH = "minTokenLength";
    /**
     * If specified, the text contained in the given segmentation type annotations are fed as
     * separate units ("documents") to the topic model estimator e.g.
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.sentence}. Text that is not within
     * such annotations is ignored.
     * <p>
     * By default, the full text is used as a document.
     */
    public static final String PARAM_COVERING_ANNOTATION_TYPE = "coveringAnnotationType";
    /**
     * If true (default: false), estimate character embeddings. {@link #PARAM_TOKEN_FEATURE_PATH} is
     * ignored.
     */
    public static final String PARAM_USE_CHARACTERS = "useCharacters";
    /**
     * If set to true (default: false), all tokens are lowercased.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    /**
     * The location of the stopwords file.
     */
    public static final String PARAM_STOPWORDS_FILE = "paramStopwordsFile";
    /**
     * If set, stopwords found in the {@link #PARAM_STOPWORDS_FILE} location are not removed, but
     * replaced by the given string (e.g. {@code STOP}).
     */
    public static final String PARAM_STOPWORDS_REPLACEMENT = "paramStopwordsReplacement";
    private static final Locale LOCALE = Locale.US;
    @ConfigurationParameter(name = PARAM_TOKEN_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String tokenFeaturePath;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue = ComponentParameters.AUTO_NUM_THREADS)
    private int numThreads;
    @ConfigurationParameter(name = PARAM_MIN_TOKEN_LENGTH, mandatory = true, defaultValue = "3")
    private int minTokenLength;
    @ConfigurationParameter(name = PARAM_COVERING_ANNOTATION_TYPE, mandatory = true, defaultValue = "")
    private String coveringAnnotationType;
    @ConfigurationParameter(name = PARAM_USE_CHARACTERS, mandatory = true, defaultValue = "false")
    private boolean useCharacters;
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = true, defaultValue = "")
    private String stopwordsFile;
    @ConfigurationParameter(name = PARAM_STOPWORDS_REPLACEMENT, mandatory = true, defaultValue = "")
    private String stopwordsReplacement;

    private InstanceList instanceList; // contains the Mallet instances
    private StringSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        // locale should be set to US to define the output format of the Mallet models (especially decimal numbers).
        Locale.setDefault(LOCALE);

        numThreads = ComponentParameters.computeNumThreads(numThreads);
        getLogger().info(String.format("Using %d threads.", numThreads));

        /* Mallet instance list and token sequence generator */
        instanceList = new InstanceList(new TokenSequence2FeatureSequence());

        if (useCharacters) {
            sequenceGenerator = new CharacterStringSequenceGenerator();
        }
        else {
            try {
                sequenceGenerator = new AnnotationStringSequenceGenerator.Builder()
                        .minTokenLength(minTokenLength)
                        .stopwordsFile(stopwordsFile)
                        .stopwordsReplacement(stopwordsReplacement)
                        .build();
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }

        sequenceGenerator.setCoveringTypeName(coveringAnnotationType);
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        DocumentMetaData metadata = DocumentMetaData.get(aJCas);
        try {
            /* retrieve token sequences and convert token sequences to instances */
            sequenceGenerator.tokenSequences(aJCas).stream()
                    .map(TokenSequence::new)
                    .map(ts -> new Instance(ts, AnnotationStringSequenceGenerator.NONE_LABEL,
                            metadata.getDocumentId(), metadata.getDocumentUri()))
                    .forEach(instance -> instanceList.addThruPipe(instance));
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    protected int getNumThreads()
    {
        return numThreads;
    }

    public InstanceList getInstanceList()
    {
        return instanceList;
    }
}
