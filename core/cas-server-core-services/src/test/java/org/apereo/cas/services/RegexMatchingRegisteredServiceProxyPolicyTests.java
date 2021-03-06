package org.apereo.cas.services;

import lombok.val;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
public class RegexMatchingRegisteredServiceProxyPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexMatchingRegisteredServiceProxyPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeARegexMatchingRegisteredServiceProxyPolicyToJson() throws IOException {
        val policyWritten = new RegexMatchingRegisteredServiceProxyPolicy("pattern");

        MAPPER.writeValue(JSON_FILE, policyWritten);

        val policyRead = MAPPER.readValue(JSON_FILE, RegexMatchingRegisteredServiceProxyPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
