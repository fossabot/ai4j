import com.google.common.collect.ImmutableMap;
import dev.ai4j.model.completion.OpenAiCompletionModel;
import dev.ai4j.model.embedding.Embedding;
import dev.ai4j.model.embedding.OpenAiEmbeddingModel;
import dev.ai4j.prompt.PromptTemplate;
import dev.ai4j.document.Document;
import dev.ai4j.document.loader.PdfFileLoader;
import dev.ai4j.document.splitter.DocumentSplitter;
import dev.ai4j.document.splitter.OverlappingDocumentSplitter;
import dev.ai4j.model.embedding.PineconeDatabase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dev.ai4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.ai4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.util.stream.Collectors.joining;

public class PdfFileOpenAiPineconeExample {

    public static void main(String[] args) {

        // Load file with information you want to "talk" to LLM about.
        // Currently, text and PDF files are supported.

        String absolutePathToPdfFile = System.getProperty("user.dir") + "/examples/large-language-models.pdf";
        PdfFileLoader pdfFileLoader = new PdfFileLoader(absolutePathToPdfFile);
        Document fullPdfDocument = pdfFileLoader.load();


        // Split the file into small chunks of 200 characters each with an overlap of 40 characters.

        DocumentSplitter splitter = new OverlappingDocumentSplitter(200, 40);
        List<Document> pdfDocumentChunks = splitter.split(fullPdfDocument);


        // Convert chunks into embeddings (semantic vectors).

        OpenAiEmbeddingModel openAiEmbeddings = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .build();
        Collection<Embedding> embeddings = openAiEmbeddings.embed(pdfDocumentChunks);


        // Store embeddings into Pinecone (vector database) for further search / retrieval.

        PineconeDatabase pineconeDatabase = PineconeDatabase.builder()
                .apiKey(System.getenv("PINECONE_API_KEY"))
                .environment("northamerica-northeast1-gcp")
                .projectName("19a129b")
                .index("test-s1-1536") // make sure the dimensions of the Pinecone index match the dimensions of the embedding model (1536 for text-embedding-ada-002)
                .build();
        pineconeDatabase.persist(embeddings);


        // Define the question you want to ask LLM.

        String question = "How many parameters does GPT-3 have?";


        // Find related embeddings in Pinecone (by semantic similarity).

        Embedding embeddingForQuestion = openAiEmbeddings.embed(question);
        Collection<Embedding> relatedEmbeddings = pineconeDatabase.findRelated(embeddingForQuestion);

        // Create a prompt for LLM that includes original question and found embeddings.

        PromptTemplate promptTemplate = PromptTemplate.from("Using only the information enclosed in triple angle brackets, answer this question: {question} <<<{embeddings}>>>");
        Map<String, Object> parameters = ImmutableMap.of(
                "question", question,
                "embeddings", relatedEmbeddings.stream()
                        .map(Embedding::getText)
                        .collect(joining("\n\n"))
        );


        // Send formatted prompt to LLM.

        OpenAiCompletionModel openAiLanguageModel = OpenAiCompletionModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                .modelName(GPT_3_5_TURBO)
                .build();
        String answer = openAiLanguageModel.complete(promptTemplate.apply(parameters));


        // See an answer from LLM.

        System.out.println(answer);
    }
}