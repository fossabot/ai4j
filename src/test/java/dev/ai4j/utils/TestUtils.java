package dev.ai4j.utils;

import dev.ai4j.model.chat.MessageFromAi;
import dev.ai4j.model.chat.MessageFromHuman;
import dev.ai4j.model.chat.MessageFromSystem;
import dev.ai4j.tokenizer.Tokenizer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static dev.ai4j.model.chat.MessageFromAi.messageFromAi;
import static dev.ai4j.model.chat.MessageFromHuman.messageFromHuman;
import static dev.ai4j.model.chat.MessageFromSystem.messageFromSystem;
import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    private static final int EXTRA_TOKENS_PER_CHAT_MESSAGE = 5;

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 25, 50, 100, 250, 500, 1000})
    void should_create_message_from_system_with_tokens(int numberOfTokens) {
        val messageFromSystem = messageFromSystemWithTokens(numberOfTokens);
        assertThat(messageFromSystem.getNumberOfTokens()).isEqualTo(numberOfTokens);
    }

    public static MessageFromSystem messageFromSystemWithTokens(int numberOfTokens) {
        return messageFromSystem(generateTokens(numberOfTokens - EXTRA_TOKENS_PER_CHAT_MESSAGE));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 25, 50, 100, 250, 500, 1000})
    void should_create_message_from_human_with_tokens(int numberOfTokens) {
        val messageFromHuman = messageFromHumanWithTokens(numberOfTokens);
        assertThat(messageFromHuman.getNumberOfTokens()).isEqualTo(numberOfTokens);
    }

    public static MessageFromHuman messageFromHumanWithTokens(int numberOfTokens) {
        return messageFromHuman(generateTokens(numberOfTokens - EXTRA_TOKENS_PER_CHAT_MESSAGE));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 25, 50, 100, 250, 500, 1000})
    void should_create_message_from_ai_with_tokens(int numberOfTokens) {
        val messageFromAi = messageFromAiWithTokens(numberOfTokens);
        assertThat(messageFromAi.getNumberOfTokens()).isEqualTo(numberOfTokens);
    }

    public static MessageFromAi messageFromAiWithTokens(int numberOfTokens) {
        return messageFromAi(generateTokens(numberOfTokens - EXTRA_TOKENS_PER_CHAT_MESSAGE));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 25, 50, 100, 250, 500, 1000})
    void should_generate_tokens(int numberOfTokens) {
        val tokenizer = new Tokenizer();

        val tokens = generateTokens(numberOfTokens);

        assertThat(tokenizer.countTokens(tokens)).isEqualTo(numberOfTokens);
    }

    public static String generateTokens(int n) {
        val tokenizer = new Tokenizer();
        val text = String.join(" ", repeat("one two", n));
        return tokenizer.decode(tokenizer.encode(text, n));
    }

    @Test
    void should_repeat_n_times() {
        assertThat(repeat("word", 1))
                .hasSize(1)
                .containsExactly("word");

        assertThat(repeat("word", 2))
                .hasSize(2)
                .containsExactly("word", "word");

        assertThat(repeat("word", 3))
                .hasSize(3)
                .containsExactly("word", "word", "word");
    }

    public static List<String> repeat(String s, int n) {
        val result = new ArrayList<String>();
        for (int i = 0; i < n; i++) {
            result.add(s);
        }
        return result;
    }
}
